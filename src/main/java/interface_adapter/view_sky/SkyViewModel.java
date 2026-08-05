package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.List;

import entity.Star;

public class SkyViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /** What the map draws: the objects above the horizon, brightest first. */
    private List<Star> stars = List.of();

    private String displayedLocation = "Toronto";

    // Today in the machine's own zone, so the app opens on a date the forecast actually covers.
    private String displayedDate = LocalDate.now().toString();
    private String displayedTime = "18:20";
    private boolean sidebarVisible = true;
    private String selectedObjectName = "Sirius";
    private String selectedObjectDetails =
            "Catalogue ID: HIP 32349\n"
            + "RA: 06h 45m 09s\n"
            + "DEC: -16\u00b0 42' 58\"\n"
            + "Altitude: 31.4\u00b0\n"
            + "Azimuth: 152.7\u00b0\n"
            + "Magnitude: -1.46\n\n"
            + "Description: Sirius is the brightest star in Earth's night sky.";
    private String weatherOrObservabilityText =
            "Conditions: Clear\n"
            + "Temperature: 19\u00b0C\n"
            + "Cloud cover: 12%\n"
            + "Humidity: 58%\n"
            + "Wind: 9 km/h\n"
            + "Observability: Good";
    private String errorMessage = "";

    public List<Star> getStars() {
        return stars;
    }

    public void setStars(final List<Star> stars) {
        final List<Star> oldStars = this.stars;
        this.stars = List.copyOf(stars);
        support.firePropertyChange("stars", oldStars, this.stars);
    }

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

    public boolean isSidebarVisible() {
        return sidebarVisible;
    }

    public void setSidebarVisible(final boolean sidebarVisible) {
        final boolean oldSidebarVisible = this.sidebarVisible;
        this.sidebarVisible = sidebarVisible;
        support.firePropertyChange("sidebarVisible", oldSidebarVisible, sidebarVisible);
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
