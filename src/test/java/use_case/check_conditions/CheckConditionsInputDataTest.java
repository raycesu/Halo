package use_case.check_conditions;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import entity.ObserverLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CheckConditionsInputDataTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final LocalDateTime observationDateTime = LocalDateTime.of(2026, 7, 30, 23, 0);
        final ObserverLocation location = new ObserverLocation(
                "Toronto", 43.6532, -79.3832, ZoneId.of("America/Toronto"));

        final CheckConditionsInputData inputData =
                new CheckConditionsInputData(location, observationDateTime);

        assertSame(location, inputData.getLocation());
        assertEquals(43.6532, inputData.getLocation().getLatitude(), 1e-9);
        assertEquals(-79.3832, inputData.getLocation().getLongitude(), 1e-9);
        assertEquals(observationDateTime, inputData.getObservationDateTime());
    }

    /** The zone travels with the place, which is what lets the weather request name it. */
    @Test
    void carriesTheTimeZoneAlongsideTheCoordinates() {
        final CheckConditionsInputData inputData = new CheckConditionsInputData(
                new ObserverLocation("Tokyo", 35.6895, 139.6917, ZoneId.of("Asia/Tokyo")),
                LocalDateTime.of(2026, 7, 30, 23, 0));

        assertEquals(ZoneId.of("Asia/Tokyo"), inputData.getLocation().getZoneId());
    }
}
