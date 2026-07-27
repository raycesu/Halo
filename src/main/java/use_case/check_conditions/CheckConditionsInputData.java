package use_case.check_conditions;

// carries what the interactor needs to run: location (lat/lon, or city string — whatever view_sky gives you)
// , and the date/time being checked. Plain data, no logic.

import java.time.LocalDateTime;

public class CheckConditionsInputData {

    private final double latitude;
    private final double longitude;
    private final LocalDateTime observationDateTime;

    public CheckConditionsInputData(
            final double latitude,
            final double longitude,
            final LocalDateTime observationDateTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.observationDateTime = observationDateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LocalDateTime getObservationDateTime() {
        return observationDateTime;
    }
}
