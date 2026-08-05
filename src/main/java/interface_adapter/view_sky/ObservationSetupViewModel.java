package interface_adapter.view_sky;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;

public class ObservationSetupViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String location = "Toronto";

    // Today in the machine's own zone, so the app opens on a date the forecast actually covers.
    private String date = LocalDate.now().toString();
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
