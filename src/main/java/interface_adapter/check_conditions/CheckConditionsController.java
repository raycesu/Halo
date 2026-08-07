package interface_adapter.check_conditions;

// thin class the View calls (e.g. from a "Check Conditions" button click).
// Takes raw UI input, wraps it into CheckConditionsInputData, calls inputBoundary.checkConditions(...).
// No logic — just translation and delegation.

import java.time.LocalDateTime;

import entity.ObserverLocation;
import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInputData;

public class CheckConditionsController {

    private final CheckConditionsInputBoundary inputBoundary;

    public CheckConditionsController(final CheckConditionsInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    /**
     * Forwards a check-conditions request to the use case.
     *
     * @param location the observer location to check conditions for
     * @param observationDateTime the local date and time of the observation
     */
    public void checkConditions(
            final ObserverLocation location,
            final LocalDateTime observationDateTime) {
        final CheckConditionsInputData inputData =
                new CheckConditionsInputData(location, observationDateTime);
        inputBoundary.checkConditions(inputData);
    }
}
