package interface_adapter.check_conditions;

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

    /**
     * Forwards a check-conditions request to the use case.
     *
     * @param displayName the display name of the observer location
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param zoneId the IANA time zone ID string
     * @param observationDateTime the local date and time of the observation
     */
    public void checkConditions(
            final String displayName,
            final double latitude,
            final double longitude,
            final String zoneId,
            final LocalDateTime observationDateTime) {
        final ObserverLocation location = new ObserverLocation(
                displayName, latitude, longitude, ZoneId.of(zoneId));
        final CheckConditionsInputData inputData =
                new CheckConditionsInputData(location, observationDateTime);
        inputBoundary.checkConditions(inputData);
    }
}
