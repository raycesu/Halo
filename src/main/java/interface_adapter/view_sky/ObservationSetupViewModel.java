package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ObservationSetupViewModel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String selectedLocationName = "";
    private double selectedLatitude = Double.NaN;
    private double selectedLongitude = Double.NaN;
    private String selectedZoneId = "";

    private String date;
    private String time;
    private String errorMessage = "";

    public ObservationSetupViewModel() {
        final LocalDateTime now = LocalDateTime.now();
        date = now.toLocalDate().toString();
        time = now.format(TIME_FORMAT);
    }

    public String getSelectedLocationName() {
        return selectedLocationName;
    }

    public double getSelectedLatitude() {
        return selectedLatitude;
    }

    public double getSelectedLongitude() {
        return selectedLongitude;
    }

    public String getSelectedZoneId() {
        return selectedZoneId;
    }

    /**
     * Whether a location has been selected.
     *
     * @return true if a location has been set
     */
    public boolean hasSelectedLocation() {
        return !selectedLocationName.isEmpty();
    }

    /**
     * Sets the selected observer location fields and notifies listeners.
     *
     * @param displayName the display name of the location
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param zoneId the IANA time zone ID string
     */
    public void setSelectedLocation(
            final String displayName,
            final double latitude,
            final double longitude,
            final String zoneId) {
        this.selectedLocationName = displayName;
        this.selectedLatitude = latitude;
        this.selectedLongitude = longitude;
        this.selectedZoneId = zoneId;
        support.firePropertyChange("selectedLocation", null, displayName);
    }

    /**
     * Clears the selected location.
     */
    public void clearSelectedLocation() {
        this.selectedLocationName = "";
        this.selectedLatitude = Double.NaN;
        this.selectedLongitude = Double.NaN;
        this.selectedZoneId = "";
        support.firePropertyChange("selectedLocation", null, "");
    }

    /**
     * The chosen place's label, or an empty string when nothing has been chosen.
     *
     * @return the display name of the selected location, or an empty string
     */
    public String getLocation() {
        return selectedLocationName;
    }

    public String getDate() {
        return date;
    }

    /**
     * Sets the date text and notifies listeners.
     *
     * @param date the date text to display
     */
    public void setDate(final String date) {
        final String oldDate = this.date;
        this.date = date;
        support.firePropertyChange("date", oldDate, date);
    }

    public String getTime() {
        return time;
    }

    /**
     * Sets the time text and notifies listeners.
     *
     * @param time the time text to display
     */
    public void setTime(final String time) {
        final String oldTime = this.time;
        this.time = time;
        support.firePropertyChange("time", oldTime, time);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message and notifies listeners.
     *
     * @param errorMessage the error text, or an empty string to clear it
     */
    public void setErrorMessage(final String errorMessage) {
        final String oldErrorMessage = this.errorMessage;
        this.errorMessage = errorMessage;
        support.firePropertyChange("errorMessage", oldErrorMessage, errorMessage);
    }

    /**
     * Registers a listener to be notified of property changes.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters a previously registered property-change listener.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
