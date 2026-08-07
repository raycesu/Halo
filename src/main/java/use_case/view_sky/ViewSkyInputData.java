package use_case.view_sky;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * What the interactor needs to build a sky map: where the observer is, and when.
 *
 * <p>Values arrive already parsed. Turning the text the user typed into a date, a time and a
 * pair of coordinates is the controller's job, so a malformed entry is rejected at the edge of
 * the application rather than deep inside a use case.
 *
 * <p>The observation time is local to {@code zoneId} rather than an instant, because a local
 * date and time is what a user means when they ask for the sky at 11pm. The interactor resolves
 * the two into the instant the data sources need.
 */
public class ViewSkyInputData {

    private final String locationName;
    private final double latitude;
    private final double longitude;
    private final ZoneId zoneId;
    private final LocalDateTime observationDateTime;

    public ViewSkyInputData(
            final String locationName,
            final double latitude,
            final double longitude,
            final ZoneId zoneId,
            final LocalDateTime observationDateTime) {

        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoneId = zoneId;
        this.observationDateTime = observationDateTime;
    }

    public String getLocationName() {
        return locationName;
    }

    /**
     * Alias for {@link #getLocationName()}, matching the name the output data uses.
     *
     * @return the location name
     */
    public String getLocation() {
        return locationName;
    }

    /**
     * The observation date as an ISO string, for passing straight through to the output.
     *
     * @return the observation date, formatted as an ISO-8601 string
     */
    public String getDate() {
        return observationDateTime.toLocalDate().toString();
    }

    /**
     * The observation time as an ISO string, for passing straight through to the output.
     *
     * @return the observation time, formatted as an ISO-8601 string
     */
    public String getTime() {
        return observationDateTime.toLocalTime().toString();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public LocalDateTime getObservationDateTime() {
        return observationDateTime;
    }
}
