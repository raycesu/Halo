package entity;

import java.time.Instant;

/**
 * Converts equatorial coordinates (right ascension and declination, fixed on the celestial
 * sphere) into horizontal coordinates (altitude and azimuth, what an observer actually sees).
 *
 * <p>This is the rule that turns a catalogue entry into a position on the sky map, and it is
 * pure: same inputs, same outputs, no I/O and no dependency on Swing. That makes it testable in
 * isolation, which matters because a sign error here is invisible until the whole map is subtly
 * wrong.
 *
 * <p>Not every star needs this. An ephemeris service may report altitude and azimuth directly,
 * in which case its values are preferable because they include corrections this class does not
 * model. See {@link #applyHorizontalPosition} for the specifics.
 */
public final class CoordinateConverter {

    /** Julian date of the Unix epoch, 1970-01-01T00:00:00Z. */
    private static final double UNIX_EPOCH_JULIAN_DATE = 2440587.5;

    /** Julian date of J2000.0, 2000-01-01T12:00:00 TT. */
    private static final double J2000_JULIAN_DATE = 2451545.0;

    private static final double SECONDS_PER_DAY = 86400.0;
    private static final double DAYS_PER_JULIAN_CENTURY = 36525.0;
    private static final double DEGREES_IN_CIRCLE = 360.0;

    /** Right ascension is carried in hours; the sky turns 15 degrees per hour. */
    private static final double DEGREES_PER_HOUR = 15.0;

    // Coefficients of the IAU 1982 expression for Greenwich mean sidereal time, in degrees.
    private static final double GMST_CONSTANT = 280.46061837;
    private static final double GMST_PER_DAY = 360.98564736629;
    private static final double GMST_SQUARE_TERM = 0.000387933;
    private static final double GMST_CUBE_DIVISOR = 38710000.0;

    private CoordinateConverter() {
    }

    /**
     * Computes where an object appears and records it on the star, leaving every catalogue field
     * untouched.
     *
     * <p>The result is geometric: it does not model atmospheric refraction, which lifts objects
     * near the horizon by up to about half a degree, nor parallax or semidiameter. For catalogue
     * stars that is well within the accuracy a sky map needs. For the Sun, Moon and planets,
     * prefer the values an ephemeris service supplies, which already carry those corrections.
     *
     * @param star the object to position; its right ascension (in hours) and declination (in
     *     degrees) must be known
     * @param location the observer's position on Earth
     * @param instant the moment of observation
     * @throws IllegalArgumentException if any argument is null, or the star's equatorial
     *     coordinates are not finite
     */
    public static void applyHorizontalPosition(
            final Star star, final ObserverLocation location, final Instant instant) {

        if (star == null || location == null || instant == null) {
            throw new IllegalArgumentException("Star, location and instant are all required.");
        }
        if (!Double.isFinite(star.getRightAscension()) || !Double.isFinite(star.getDeclination())) {
            throw new IllegalArgumentException(
                    "Cannot position " + star.getDisplayName()
                            + " because its equatorial coordinates are unknown.");
        }

        final double localSiderealTime =
                localSiderealTimeDegrees(instant, location.getLongitude());

        // The hour angle is how far the object has moved past the observer's meridian. Right
        // ascension is in hours, so it is scaled to degrees before the subtraction.
        final double hourAngle = Math.toRadians(wrapIntoCircle(
                localSiderealTime - star.getRightAscension() * DEGREES_PER_HOUR));
        final double declination = Math.toRadians(star.getDeclination());
        final double latitude = Math.toRadians(location.getLatitude());

        final double sineAltitude =
                Math.sin(declination) * Math.sin(latitude)
                        + Math.cos(declination) * Math.cos(latitude) * Math.cos(hourAngle);
        final double altitude = Math.asin(clampToUnitRange(sineAltitude));

        // Measured clockwise from true north, which is the convention the sky map draws in.
        final double azimuth = Math.atan2(
                -Math.cos(declination) * Math.sin(hourAngle),
                Math.sin(declination) * Math.cos(latitude)
                        - Math.cos(declination) * Math.sin(latitude) * Math.cos(hourAngle));

        star.updateHorizontalPosition(
                Math.toDegrees(altitude),
                wrapIntoCircle(Math.toDegrees(azimuth)));
    }

    /**
     * Local mean sidereal time in degrees: the right ascension currently crossing the observer's
     * meridian. East longitudes are positive.
     *
     * <p>This is mean rather than apparent sidereal time; the difference (the equation of the
     * equinoxes) stays under about 0.005 degrees, far below what a sky map can show.
     */
    public static double localSiderealTimeDegrees(final Instant instant, final double longitude) {
        return wrapIntoCircle(greenwichMeanSiderealTimeDegrees(instant) + longitude);
    }

    private static double greenwichMeanSiderealTimeDegrees(final Instant instant) {
        final double julianDate = toJulianDate(instant);
        final double daysSinceJ2000 = julianDate - J2000_JULIAN_DATE;
        final double centuriesSinceJ2000 = daysSinceJ2000 / DAYS_PER_JULIAN_CENTURY;

        return wrapIntoCircle(
                GMST_CONSTANT
                        + GMST_PER_DAY * daysSinceJ2000
                        + GMST_SQUARE_TERM * centuriesSinceJ2000 * centuriesSinceJ2000
                        - centuriesSinceJ2000 * centuriesSinceJ2000 * centuriesSinceJ2000
                                / GMST_CUBE_DIVISOR);
    }

    /**
     * Converts an instant to a Julian date. Uses the epoch second directly so the sub-second
     * component is preserved, since sidereal time advances about 15 arcseconds per second.
     */
    public static double toJulianDate(final Instant instant) {
        final double epochSeconds =
                instant.getEpochSecond() + instant.getNano() / 1_000_000_000.0;
        return UNIX_EPOCH_JULIAN_DATE + epochSeconds / SECONDS_PER_DAY;
    }

    /**
     * Guards against rounding pushing the argument of {@link Math#asin} just outside [-1, 1],
     * which would otherwise produce NaN for an object at the exact zenith.
     */
    private static double clampToUnitRange(final double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }

    private static double wrapIntoCircle(final double degrees) {
        final double wrapped = degrees % DEGREES_IN_CIRCLE;
        final double result;
        if (wrapped < 0.0) {
            result = wrapped + DEGREES_IN_CIRCLE;
        }
        else {
            result = wrapped;
        }
        return result;
    }
}
