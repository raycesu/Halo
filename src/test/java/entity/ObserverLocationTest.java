package entity;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObserverLocationTest {

    private static final ZoneId TORONTO_ZONE =
            ZoneId.of("America/Toronto");

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final ObserverLocation location = new ObserverLocation(
                "Toronto",
                43.6532,
                -79.3832,
                TORONTO_ZONE
        );

        assertEquals("Toronto", location.getDisplayName());
        assertEquals(43.6532, location.getLatitude(), 1e-9);
        assertEquals(-79.3832, location.getLongitude(), 1e-9);
        assertEquals(TORONTO_ZONE, location.getZoneId());
    }

    @Test
    void acceptsCoordinateBoundaryValues() {
        final ObserverLocation minimum = new ObserverLocation(
                "Minimum",
                -90.0,
                -180.0,
                TORONTO_ZONE
        );
        final ObserverLocation maximum = new ObserverLocation(
                "Maximum",
                90.0,
                180.0,
                TORONTO_ZONE
        );

        assertEquals(-90.0, minimum.getLatitude(), 1e-9);
        assertEquals(-180.0, minimum.getLongitude(), 1e-9);
        assertEquals(90.0, maximum.getLatitude(), 1e-9);
        assertEquals(180.0, maximum.getLongitude(), 1e-9);
    }

    @Test
    void rejectsNullOrBlankDisplayName() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ObserverLocation(
                                null, 0.0, 0.0, TORONTO_ZONE)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ObserverLocation(
                                "", 0.0, 0.0, TORONTO_ZONE)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ObserverLocation(
                                "   ", 0.0, 0.0, TORONTO_ZONE)
                )
        );
    }

    @Test
    void rejectsLatitudeOutsideValidRange() {
        assertAll(
                () -> assertInvalidLatitude(-90.0001),
                () -> assertInvalidLatitude(90.0001)
        );
    }

    @Test
    void rejectsNonFiniteLatitude() {
        assertAll(
                () -> assertInvalidLatitude(Double.NaN),
                () -> assertInvalidLatitude(
                        Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void rejectsLongitudeOutsideValidRange() {
        assertAll(
                () -> assertInvalidLongitude(-180.0001),
                () -> assertInvalidLongitude(180.0001)
        );
    }

    @Test
    void rejectsNonFiniteLongitude() {
        assertAll(
                () -> assertInvalidLongitude(Double.NaN),
                () -> assertInvalidLongitude(
                        Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void rejectsNullZoneId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ObserverLocation(
                        "Toronto", 43.6532, -79.3832, null)
        );
    }

    private void assertInvalidLatitude(final double latitude) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ObserverLocation(
                        "Test", latitude, 0.0, TORONTO_ZONE)
        );
    }

    private void assertInvalidLongitude(final double longitude) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ObserverLocation(
                        "Test", 0.0, longitude, TORONTO_ZONE)
        );
    }
}