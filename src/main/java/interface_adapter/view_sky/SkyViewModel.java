package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import entity.Star;

public class SkyViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String displayedLocation = "";
    private String displayedDate = "";
    private String displayedTime = "";
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
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

    public void setDisplayedLocation(final String displayedLocation) {
        final String oldLocation = this.displayedLocation;
        this.displayedLocation = displayedLocation;
        support.firePropertyChange("displayedLocation", oldLocation, displayedLocation);
    }

    public String getDisplayedDate() {
        return displayedDate;
    }

    public void setDisplayedDate(final String displayedDate) {
        final String oldDate = this.displayedDate;
        this.displayedDate = displayedDate;
        support.firePropertyChange("displayedDate", oldDate, displayedDate);
    }

    public String getDisplayedTime() {
        return displayedTime;
    }

    public void setDisplayedTime(final String displayedTime) {
        final String oldTime = this.displayedTime;
        this.displayedTime = displayedTime;
        support.firePropertyChange("displayedTime", oldTime, displayedTime);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        final double oldLatitude = this.latitude;
        this.latitude = latitude;
        support.firePropertyChange("latitude", oldLatitude, latitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        final double oldLongitude = this.longitude;
        this.longitude = longitude;
        support.firePropertyChange("longitude", oldLongitude, longitude);
    }

    public List<Star> getStars() {
        return stars;
    }

    public void setStars(final List<Star> stars) {
        final List<Star> oldStars = this.stars;
        this.stars = List.copyOf(stars);
        support.firePropertyChange("stars", oldStars, this.stars);
    }

    public Star getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(final Star selectedObject) {
        final Star oldObject = this.selectedObject;
        this.selectedObject = selectedObject;
        support.firePropertyChange("selectedObject", oldObject, selectedObject);

        final String name = selectedObject == null ? "" : selectedObject.getDisplayName();
        setSelectedObjectName(name);
    }

    public String getSelectedObjectName() {
        return selectedObjectName;
    }

    public void setSelectedObjectName(final String selectedObjectName) {
        final String oldObjectName = this.selectedObjectName;
        this.selectedObjectName = selectedObjectName;
        support.firePropertyChange("selectedObjectName", oldObjectName, selectedObjectName);
    }

    public String getSelectedObjectDetails() {
        return selectedObjectDetails;
    }

    public void setSelectedObjectDetails(final String selectedObjectDetails) {
        final String oldObjectDetails = this.selectedObjectDetails;
        this.selectedObjectDetails = selectedObjectDetails;
        support.firePropertyChange("selectedObjectDetails", oldObjectDetails, selectedObjectDetails);
    }

    public String getWeatherOrObservabilityText() {
        return weatherOrObservabilityText;
    }

    public void setWeatherOrObservabilityText(final String weatherOrObservabilityText) {
        final String oldText = this.weatherOrObservabilityText;
        this.weatherOrObservabilityText = weatherOrObservabilityText;
        support.firePropertyChange("weatherOrObservabilityText", oldText, weatherOrObservabilityText);
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(final String warningMessage) {
        final String oldWarningMessage = this.warningMessage;
        this.warningMessage = warningMessage;
        support.firePropertyChange("warningMessage", oldWarningMessage, warningMessage);
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
