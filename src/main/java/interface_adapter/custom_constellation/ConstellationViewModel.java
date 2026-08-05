package interface_adapter.custom_constellation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import entity.CustomConstellation;

public class ConstellationViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<CustomConstellation> constellations = List.of();
    private String errorMessage = "";
    private String successMessage = "";
    public List<CustomConstellation> getConstellations() {
        return constellations;
    }

    public void addConstellation(final CustomConstellation constellation) {
        final List<CustomConstellation> oldValue = constellations;
        final List<CustomConstellation> updated = new ArrayList<>(constellations);
        updated.add(constellation);
        constellations = List.copyOf(updated);
        support.firePropertyChange("constellations", oldValue, constellations);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        final String oldValue = this.errorMessage;
        this.errorMessage = errorMessage;
        support.firePropertyChange("errorMessage", oldValue, errorMessage);
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(final String successMessage) {
        final String oldValue = this.successMessage;
        this.successMessage = successMessage;
        support.firePropertyChange("successMessage", oldValue, successMessage);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
