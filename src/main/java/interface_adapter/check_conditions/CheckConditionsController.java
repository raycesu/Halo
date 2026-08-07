package interface_adapter.check_conditions;

// thin class the View calls (e.g. from a "Check Conditions" button click).
// Takes raw UI input, wraps it into CheckConditionsInputData, calls inputBoundary.checkConditions(...).
// No logic — just translation and delegation.

import java.time.LocalDateTime;
import java.time.ZoneId;

import entity.ObserverLocation;
import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInputData;

public class CheckConditionsController {

    private final CheckConditionsInputBoundary inputBoundary;

    public CheckConditionsController(final CheckConditionsInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void checkConditions(
            final String locationName,
            final double latitude,
            final double longitude,
            final String timeZoneId,
            final LocalDateTime observationDateTime) {
        final ObserverLocation location =
                new ObserverLocation(locationName, latitude, longitude, ZoneId.of(timeZoneId));
        final CheckConditionsInputData inputData =
                new CheckConditionsInputData(location, observationDateTime);
        inputBoundary.checkConditions(inputData);
    }
}
