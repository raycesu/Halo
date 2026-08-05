package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;

import entity.ObserverLocation;

public class ObservationSetupViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The place the user picked from the suggestion list, or null if they have not picked one.
     *
     * <p>Replaces the separate latitude, longitude and time zone entries this screen used to ask
     * for. Those are not things a person knows offhand, and a mistyped one is accepted silently by
     * every service downstream, so the place is resolved once and then carried whole.
     */
    private ObserverLocation selectedLocation;

    // Today in the machine's own zone, so the app opens on a date the forecast actually covers.
    private String date = LocalDate.now().toString();
    private String time = "18:20";
    private String errorMessage = "";

    public ObserverLocation getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(final ObserverLocation selectedLocation) {
        final ObserverLocation oldLocation = this.selectedLocation;
        this.selectedLocation = selectedLocation;
        support.firePropertyChange("selectedLocation", oldLocation, selectedLocation);
    }

    /** The chosen place's label, or an empty string when nothing has been chosen. */
    public String getLocation() {
        final String location;
        if (selectedLocation == null) {
            location = "";
        }
        else {
            location = selectedLocation.getDisplayName();
        }
        return location;
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
