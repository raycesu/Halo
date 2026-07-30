package entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The strongest check on this class lives in
 * {@code data_access.UsnoCoordinateAgreementTest}, which compares its output against positions
 * computed independently by the US Naval Observatory. What follows covers the properties and
 * edge cases that a single recorded response cannot reach.
 */
class CoordinateConverterTest {

    private static final ObserverLocation TORONTO = new ObserverLocation(
            "Toronto", 43.6532, -79.3832, ZoneOffset.UTC.normalized());

    private static final ObserverLocation NORTH_POLE = new ObserverLocation(
            "North Pole", 90.0, 0.0, ZoneOffset.UTC.normalized());

    private static final ObserverLocation EQUATOR_GREENWICH = new ObserverLocation(
            "Null Island", 0.0, 0.0, ZoneOffset.UTC.normalized());

    private static final Instant SAMPLE_INSTANT =
            LocalDateTime.of(2026, 7, 30, 23, 0, 0).toInstant(ZoneOffset.UTC);

    @Test
    void julianDateOfUnixEpochIsTheKnownConstant() {
        assertEquals(2440587.5, CoordinateConverter.toJulianDate(Instant.EPOCH), 1e-9);
    }

    @Test
    void julianDateOfJ2000EpochMatchesTheKnownConstant() {
        final Instant j2000 = LocalDateTime.of(2000, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        // 2451545.0 is J2000.0 in Terrestrial Time; against UTC the difference is under a minute,
        // which is 0.0008 days.
        assertEquals(2451545.0, CoordinateConverter.toJulianDate(j2000), 0.001);
    }

    @Test
    void siderealTimeAdvancesSlightlyFasterThanSolarTime() {
        final double now = CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, 0.0);
        final double oneSolarDayLater = CoordinateConverter.localSiderealTimeDegrees(
                SAMPLE_INSTANT.plusSeconds(86400), 0.0);

        // A sidereal day is about 3 minutes 56 seconds shorter, so sidereal time gains almost
        // exactly one degree per solar day.
        final double gain = ((oneSolarDayLater - now) + 360.0) % 360.0;
        assertEquals(0.9856, gain, 0.001);
    }

    @Test
    void longitudeShiftsSiderealTimeDegreeForDegree() {
        final double atGreenwich =
                CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, 0.0);
        final double thirtyDegreesEast =
                CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, 30.0);

        assertEquals(30.0, ((thirtyDegreesEast - atGreenwich) + 360.0) % 360.0, 1e-9);
    }

    @Test
    void objectOnTheMeridianSitsDueSouthForANorthernObserver() {
        // Declination below the observer's latitude, so it culminates to the south.
        final double meridian =
                CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, TORONTO.getLongitude());
        final Star star = starAt(meridian, 20.0);

        CoordinateConverter.applyHorizontalPosition(star, TORONTO, SAMPLE_INSTANT);

        assertEquals(180.0, star.getAzimuth(), 0.01);
        // At culmination altitude is 90 - |latitude - declination|.
        assertEquals(90.0 - (TORONTO.getLatitude() - 20.0), star.getAltitude(), 0.01);
    }

    @Test
    void circumpolarObjectAboveThePoleSitsDueNorth() {
        final double meridian =
                CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, TORONTO.getLongitude());
        // Declination above the observer's latitude culminates to the north instead.
        final Star star = starAt(meridian, 70.0);

        CoordinateConverter.applyHorizontalPosition(star, TORONTO, SAMPLE_INSTANT);

        assertEquals(0.0, star.getAzimuth(), 0.01);
        assertEquals(90.0 - (70.0 - TORONTO.getLatitude()), star.getAltitude(), 0.01);
    }

    @Test
    void celestialPoleSitsAtAnAltitudeEqualToLatitude() {
        final Star polaris = starAt(0.0, 90.0);

        CoordinateConverter.applyHorizontalPosition(polaris, TORONTO, SAMPLE_INSTANT);

        assertEquals(TORONTO.getLatitude(), polaris.getAltitude(), 0.01);
    }

    @Test
    void everythingSharesTheObserversDeclinationAtThePole() {
        final Star star = starAt(123.0, 35.0);

        CoordinateConverter.applyHorizontalPosition(star, NORTH_POLE, SAMPLE_INSTANT);

        // From the pole an object's altitude is simply its declination, whatever the hour angle.
        assertEquals(35.0, star.getAltitude(), 0.01);
    }

    @Test
    void objectAtTheZenithDoesNotProduceNaN() {
        // From the equator at Greenwich, an object on the meridian with zero declination is
        // exactly overhead, the case where rounding could push asin outside its domain.
        final double meridian = CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, 0.0);
        final Star star = starAt(meridian, 0.0);

        CoordinateConverter.applyHorizontalPosition(star, EQUATOR_GREENWICH, SAMPLE_INSTANT);

        assertEquals(90.0, star.getAltitude(), 0.01);
        assertTrue(Double.isFinite(star.getAzimuth()));
    }

    @Test
    void producesAzimuthInRangeAcrossAFullRotation() {
        for (int hourAngle = 0; hourAngle < 360; hourAngle += 5) {
            final Star star = starAt(hourAngle, 12.0);

            CoordinateConverter.applyHorizontalPosition(star, TORONTO, SAMPLE_INSTANT);

            assertTrue(star.getAzimuth() >= 0.0 && star.getAzimuth() < 360.0,
                    "Azimuth out of range at right ascension " + hourAngle);
            assertTrue(star.getAltitude() >= -90.0 && star.getAltitude() <= 90.0,
                    "Altitude out of range at right ascension " + hourAngle);
        }
    }

    @Test
    void anObjectBelowTheHorizonIsReportedAsSuch() {
        final double meridian =
                CoordinateConverter.localSiderealTimeDegrees(SAMPLE_INSTANT, TORONTO.getLongitude());
        // Far southern declination, on the meridian: never rises for a northern observer.
        final Star star = starAt(meridian, -80.0);

        CoordinateConverter.applyHorizontalPosition(star, TORONTO, SAMPLE_INSTANT);

        assertTrue(star.getAltitude() < 0.0);
        assertTrue(!star.isAboveHorizon());
    }

    @Test
    void leavesCatalogueFieldsUntouched() {
        final Star star = new Star("HIP 91262", "Vega", 18.61561111, 38.784, 0.03,
                "Lyra", "A0V", "Bright northern star");

        CoordinateConverter.applyHorizontalPosition(star, TORONTO, SAMPLE_INSTANT);

        assertEquals("HIP 91262", star.getCatalogueId());
        assertEquals("Vega", star.getDisplayName());
        assertEquals(18.61561111, star.getRightAscension(), 1e-9);
        assertEquals(38.784, star.getDeclination(), 1e-9);
        assertEquals(0.03, star.getApparentMagnitude(), 1e-9);
        assertEquals(CelestialBodyType.STAR, star.getType());
    }

    @Test
    void rejectsUnknownEquatorialCoordinates() {
        final Star star = starAt(Double.NaN, 10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> CoordinateConverter.applyHorizontalPosition(star, TORONTO, SAMPLE_INSTANT));
    }

    @Test
    void rejectsMissingArguments() {
        final Star star = starAt(10.0, 10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> CoordinateConverter.applyHorizontalPosition(null, TORONTO, SAMPLE_INSTANT));
        assertThrows(
                IllegalArgumentException.class,
                () -> CoordinateConverter.applyHorizontalPosition(star, null, SAMPLE_INSTANT));
        assertThrows(
                IllegalArgumentException.class,
                () -> CoordinateConverter.applyHorizontalPosition(star, TORONTO, null));
    }

    /** Right ascension is supplied in degrees here and converted, since the sidereal-time
     * helpers work in degrees while {@link Star} carries hours. */
    private Star starAt(final double rightAscensionDegrees, final double declination) {
        return new Star("", "test", rightAscensionDegrees / 15.0, declination, 1.0, "", "", "");
    }
}
