package use_case.view_sky;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewSkyInputDataTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final ZoneId zoneId =
                ZoneId.of("America/Toronto");
        final LocalDateTime observationDateTime =
                LocalDateTime.of(2026, 8, 7, 23, 30);
        final ViewSkyInputData inputData =
                new ViewSkyInputData(
                        "Toronto",
                        43.6532,
                        -79.3832,
                        zoneId,
                        observationDateTime
                );

        assertEquals("Toronto", inputData.getLocationName());
        assertEquals("Toronto", inputData.getLocation());
        assertEquals(43.6532, inputData.getLatitude(), 1e-9);
        assertEquals(-79.3832, inputData.getLongitude(), 1e-9);
        assertEquals(zoneId, inputData.getZoneId());
        assertEquals(observationDateTime, inputData.getObservationDateTime());
        assertEquals("2026-08-07", inputData.getDate());
        assertEquals("23:30", inputData.getTime());
    }
}
