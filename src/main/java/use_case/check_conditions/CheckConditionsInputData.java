package use_case.check_conditions;

// carries what the interactor needs to run: the observation location and the date/time being
// checked. Plain data, no logic.

import java.time.LocalDateTime;

import entity.ObserverLocation;

public class CheckConditionsInputData {

    private final ObserverLocation location;
    private final LocalDateTime observationDateTime;

    public CheckConditionsInputData(
            final ObserverLocation location,
            final LocalDateTime observationDateTime) {
        this.location = location;
        this.observationDateTime = observationDateTime;
    }

    public ObserverLocation getLocation() {
        return location;
    }

    public LocalDateTime getObservationDateTime() {
        return observationDateTime;
    }
}
