package entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarTest {
    private static final double DELTA = 1e-9;

    @Test
    void defaultConstructorExposesCatalogueFields() {
        final Star star = createStar();

        assertEquals("Sirius", star.getCatalogueId());
        assertEquals("Sirius", star.getDisplayName());
        assertEquals(6.7525, star.getRightAscension(), DELTA);
        assertEquals(-16.7161, star.getDeclination(), DELTA);
        assertEquals(-1.46, star.getApparentMagnitude(), DELTA);
        assertEquals("CMA", star.getConstellationRegion());
        assertEquals("A1V", star.getSpectralType());
        assertEquals("Brightest star", star.getDescription());
        assertEquals(CelestialBodyType.STAR, star.getType());
    }

    @Test
    void startsWithoutAnObservedPosition() {
        final Star star = createStar();

        assertTrue(Double.isNaN(star.getAltitude()));
        assertTrue(Double.isNaN(star.getAzimuth()));
        assertFalse(star.isAboveHorizon());
    }

    @Test
    void fullConstructorAcceptsAnotherCelestialBodyType() {
        final Star planet = new Star(
                "mars",
                "Mars",
                1.0,
                2.0,
                -2.0,
                "PSC",
                "",
                "The planet Mars",
                CelestialBodyType.PLANET
        );

        assertEquals(CelestialBodyType.PLANET, planet.getType());
    }

    @Test
    void updatesHorizontalPosition() {
        final Star star = createStar();

        star.updateHorizontalPosition(45.0, 180.0);

        assertEquals(45.0, star.getAltitude(), DELTA);
        assertEquals(180.0, star.getAzimuth(), DELTA);
    }

    @Test
    void acceptsHorizontalPositionBoundaryValues() {
        final Star star = createStar();

        star.updateHorizontalPosition(-90.0, 0.0);
        assertEquals(-90.0, star.getAltitude(), DELTA);
        assertEquals(0.0, star.getAzimuth(), DELTA);

        star.updateHorizontalPosition(90.0, 359.999);
        assertEquals(90.0, star.getAltitude(), DELTA);
        assertEquals(359.999, star.getAzimuth(), DELTA);
    }

    @Test
    void determinesWhetherStarIsAboveHorizon() {
        final Star star = createStar();
        star.updateHorizontalPosition(0.0, 20.0);
        assertFalse(star.isAboveHorizon());
        star.updateHorizontalPosition(0.1, 20.0);
        assertTrue(star.isAboveHorizon());
        star.updateHorizontalPosition(-0.1, 20.0);
        assertFalse(star.isAboveHorizon());
    }

    @Test
    void rejectsAltitudeOutsideValidRange() {
        assertAll(
                () -> assertInvalidAltitude(-90.0001),
                () -> assertInvalidAltitude(90.0001)
        );
    }

    @Test
    void rejectsNonFiniteAltitude() {
        assertAll(
                () -> assertInvalidAltitude(Double.NaN),
                () -> assertInvalidAltitude(
                        Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void rejectsAzimuthOutsideValidRange() {
        assertAll(
                () -> assertInvalidAzimuth(-0.0001),
                () -> assertInvalidAzimuth(360.0)
        );
    }

    @Test
    void rejectsNonFiniteAzimuth() {
        assertAll(
                () -> assertInvalidAzimuth(Double.NaN),
                () -> assertInvalidAzimuth(
                        Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void invalidUpdateDoesNotChangeExistingPosition() {
        final Star star = createStar();
        star.updateHorizontalPosition(30.0, 120.0);
        assertThrows(
                IllegalArgumentException.class,
                () -> star.updateHorizontalPosition(91.0, 20.0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> star.updateHorizontalPosition(20.0, 360.0)
        );
        assertEquals(30.0, star.getAltitude(), DELTA);
        assertEquals(120.0, star.getAzimuth(), DELTA);
    }

    @Test
    void copyPreservesCatalogueDataButNotObservedPosition() {
        final Star original = new Star(
                "mars",
                "Mars",
                1.25,
                -5.5,
                -2.0,
                "PSC",
                "",
                "The planet Mars",
                CelestialBodyType.PLANET
        );
        original.updateHorizontalPosition(30.0, 120.0);
        final Star copy = original.copyForObservation();
        assertNotSame(original, copy);
        assertEquals(original.getCatalogueId(), copy.getCatalogueId());
        assertEquals(original.getDisplayName(), copy.getDisplayName());
        assertEquals(
                original.getRightAscension(),
                copy.getRightAscension(),
                DELTA
        );
        assertEquals(
                original.getDeclination(),
                copy.getDeclination(),
                DELTA
        );
        assertEquals(
                original.getApparentMagnitude(),
                copy.getApparentMagnitude(),
                DELTA
        );
        assertEquals(
                original.getConstellationRegion(),
                copy.getConstellationRegion()
        );
        assertEquals(
                original.getSpectralType(),
                copy.getSpectralType()
        );
        assertEquals(
                original.getDescription(),
                copy.getDescription()
        );
        assertEquals(original.getType(), copy.getType());
        assertTrue(Double.isNaN(copy.getAltitude()));
        assertTrue(Double.isNaN(copy.getAzimuth()));
        assertFalse(copy.isAboveHorizon());
    }

    private Star createStar() {
        return new Star(
                "Sirius",
                "Sirius",
                6.7525,
                -16.7161,
                -1.46,
                "CMA",
                "A1V",
                "Brightest star"
        );
    }

    private void assertInvalidAltitude(final double altitude) {
        final Star star = createStar();
        assertThrows(
                IllegalArgumentException.class,
                () -> star.updateHorizontalPosition(altitude, 0.0)
        );
    }

    private void assertInvalidAzimuth(final double azimuth) {
        final Star star = createStar();
        assertThrows(
                IllegalArgumentException.class,
                () -> star.updateHorizontalPosition(0.0, azimuth)
        );
    }
}