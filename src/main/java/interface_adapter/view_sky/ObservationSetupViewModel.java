package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ObservationSetupViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String location = "Toronto";
    private String latitude = "43.6532";
    private String longitude = "-79.3832";
    private String zoneId = "America/Toronto";
    private String date = "2026-07-24";
    private String time = "18:20";
    private String errorMessage = "";

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        final String oldLocation = this.location;
        this.location = location;
        support.firePropertyChange("location", oldLocation, location);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(final String latitude) {
        final String oldLatitude = this.latitude;
        this.latitude = latitude;
        support.firePropertyChange("latitude", oldLatitude, latitude);
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(final String longitude) {
        final String oldLongitude = this.longitude;
        this.longitude = longitude;
        support.firePropertyChange("longitude", oldLongitude, longitude);
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        final String oldZoneId = this.zoneId;
        this.zoneId = zoneId;
        support.firePropertyChange("zoneId", oldZoneId, zoneId);
    }

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        final String oldDate = this.date;
        this.date = date;
        support.firePropertyChange("date", oldDate, date);
    }

    public String getTime() {
        return time;
    }

    public void setTime(final String time) {
        final String oldTime = this.time;
        this.time = time;
        support.firePropertyChange("time", oldTime, time);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        final String oldErrorMessage = this.errorMessage;
        this.errorMessage = errorMessage;
        support.firePropertyChange("errorMessage", oldErrorMessage, errorMessage);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
