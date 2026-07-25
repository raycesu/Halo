package interface_adapter.check_conditions;

// thin class the View calls (e.g. from a "Check Conditions" button click).
// Takes raw UI input, wraps it into CheckConditionsInputData, calls inputBoundary.checkConditions(...).
// No logic — just translation and delegation.

import java.time.LocalDateTime;

import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInputData;

public class CheckConditionsController {

    private final CheckConditionsInputBoundary inputBoundary;

    public CheckConditionsController(final CheckConditionsInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void checkConditions(
            final double latitude,
            final double longitude,
            final LocalDateTime observationDateTime) {
        final CheckConditionsInputData inputData =
                new CheckConditionsInputData(latitude, longitude, observationDateTime);
        inputBoundary.checkConditions(inputData);
    }
}
