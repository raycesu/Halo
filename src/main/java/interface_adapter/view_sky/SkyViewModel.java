package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import entity.ObserverLocation;
import entity.Star;

public class SkyViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String displayedLocation = "";
    private String displayedDate = "";
    private String displayedTime = "";
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;

    /** The observed place, kept whole so weather requests carry its time zone as well. */
    private ObserverLocation observerLocation;
    private List<Star> stars = List.of();
    private Star selectedObject;
    private String selectedObjectName = "";
    private String selectedObjectDetails = "";
    private String weatherOrObservabilityText = "";
    private String warningMessage = "";
    private String errorMessage = "";

    public String getDisplayedLocation() {
        return displayedLocation;
    }

    /**
     * Sets the displayed location text and notifies listeners.
     *
     * @param displayedLocation the location text to display
     */
    public void setDisplayedLocation(final String displayedLocation) {
        final String oldLocation = this.displayedLocation;
        this.displayedLocation = displayedLocation;
        support.firePropertyChange("displayedLocation", oldLocation, displayedLocation);
    }

    public String getDisplayedDate() {
        return displayedDate;
    }

    /**
     * Sets the displayed date text and notifies listeners.
     *
     * @param displayedDate the date text to display
     */
    public void setDisplayedDate(final String displayedDate) {
        final String oldDate = this.displayedDate;
        this.displayedDate = displayedDate;
        support.firePropertyChange("displayedDate", oldDate, displayedDate);
    }

    public String getDisplayedTime() {
        return displayedTime;
    }

    /**
     * Sets the displayed time text and notifies listeners.
     *
     * @param displayedTime the time text to display
     */
    public void setDisplayedTime(final String displayedTime) {
        final String oldTime = this.displayedTime;
        this.displayedTime = displayedTime;
        support.firePropertyChange("displayedTime", oldTime, displayedTime);
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the observer's latitude and notifies listeners.
     *
     * @param latitude the latitude in decimal degrees
     */
    public void setLatitude(final double latitude) {
        final double oldLatitude = this.latitude;
        this.latitude = latitude;
        support.firePropertyChange("latitude", oldLatitude, latitude);
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the observer's longitude and notifies listeners.
     *
     * @param longitude the longitude in decimal degrees
     */
    public void setLongitude(final double longitude) {
        final double oldLongitude = this.longitude;
        this.longitude = longitude;
        support.firePropertyChange("longitude", oldLongitude, longitude);
    }

    public ObserverLocation getObserverLocation() {
        return observerLocation;
    }

    /**
     * Sets the observer's location and notifies listeners.
     *
     * @param observerLocation the observer's location
     */
    public void setObserverLocation(final ObserverLocation observerLocation) {
        final ObserverLocation oldLocation = this.observerLocation;
        this.observerLocation = observerLocation;
        support.firePropertyChange("observerLocation", oldLocation, observerLocation);
    }

    public List<Star> getStars() {
        return stars;
    }

    /**
     * Replaces the displayed star snapshot and notifies listeners.
     *
     * @param stars the completed star list for the current observation
     */
    public void setStars(final List<Star> stars) {
        final List<Star> oldStars = this.stars;
        this.stars = List.copyOf(stars);
        support.firePropertyChange("stars", oldStars, this.stars);
    }

    public Star getSelectedObject() {
        return selectedObject;
    }

    /**
     * Sets the currently selected object and notifies listeners.
     *
     * @param selectedObject the newly selected star, or null when nothing is selected
     */
    public void setSelectedObject(final Star selectedObject) {
        final Star oldObject = this.selectedObject;
        this.selectedObject = selectedObject;
        support.firePropertyChange("selectedObject", oldObject, selectedObject);

        final String name;
        if (selectedObject == null) {
            name = "";
        }
        else {
            name = selectedObject.getDisplayName();
        }
        setSelectedObjectName(name);
    }

    public String getSelectedObjectName() {
        return selectedObjectName;
    }

    /**
     * Sets the display name of the selected object and notifies listeners.
     *
     * @param selectedObjectName the display name to show
     */
    public void setSelectedObjectName(final String selectedObjectName) {
        final String oldObjectName = this.selectedObjectName;
        this.selectedObjectName = selectedObjectName;
        support.firePropertyChange("selectedObjectName", oldObjectName, selectedObjectName);
    }

    public String getSelectedObjectDetails() {
        return selectedObjectDetails;
    }

    /**
     * Sets the presentation text describing the selected object and notifies listeners.
     *
     * @param selectedObjectDetails the formatted details text
     */
    public void setSelectedObjectDetails(final String selectedObjectDetails) {
        final String oldObjectDetails = this.selectedObjectDetails;
        this.selectedObjectDetails = selectedObjectDetails;
        support.firePropertyChange("selectedObjectDetails", oldObjectDetails, selectedObjectDetails);
    }

    public String getWeatherOrObservabilityText() {
        return weatherOrObservabilityText;
    }

    /**
     * Sets the presentation text describing weather or observability and notifies listeners.
     *
     * @param weatherOrObservabilityText the formatted weather/observability text
     */
    public void setWeatherOrObservabilityText(final String weatherOrObservabilityText) {
        final String oldText = this.weatherOrObservabilityText;
        this.weatherOrObservabilityText = weatherOrObservabilityText;
        support.firePropertyChange("weatherOrObservabilityText", oldText, weatherOrObservabilityText);
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    /**
     * Sets the warning message and notifies listeners.
     *
     * @param warningMessage the warning text, or an empty string to clear it
     */
    public void setWarningMessage(final String warningMessage) {
        final String oldWarningMessage = this.warningMessage;
        this.warningMessage = warningMessage;
        support.firePropertyChange("warningMessage", oldWarningMessage, warningMessage);
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
