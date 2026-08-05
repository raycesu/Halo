package use_case.check_conditions;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckConditionsInputDataTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final LocalDateTime observationDateTime = LocalDateTime.of(2026, 7, 30, 23, 0);

        final CheckConditionsInputData inputData =
                new CheckConditionsInputData(43.6532, -79.3832, observationDateTime);

        assertEquals(43.6532, inputData.getLatitude(), 1e-9);
        assertEquals(-79.3832, inputData.getLongitude(), 1e-9);
        assertEquals(observationDateTime, inputData.getObservationDateTime());
    }
}
