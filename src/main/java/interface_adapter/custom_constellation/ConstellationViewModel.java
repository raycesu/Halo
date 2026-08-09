package interface_adapter.custom_constellation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class ConstellationViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<ConstellationDisplayData> constellations = List.of();
    private String errorMessage = "";
    private String successMessage = "";

    public List<ConstellationDisplayData> getConstellations() {
        return constellations;
    }

    /**
     * Replaces the displayed constellations after their stars are repositioned.
     *
     * @param updatedConstellations custom constellations for the current observation
     */
    public void setConstellations(
            final List<ConstellationDisplayData> updatedConstellations) {
        final List<ConstellationDisplayData> oldValue = constellations;
        constellations = List.copyOf(updatedConstellations);
        support.firePropertyChange("constellations", oldValue, constellations);
    }

    /**
     * Appends a constellation to the displayed list and notifies listeners.
     *
     * @param constellation the constellation to add
     */
    public void addConstellation(final ConstellationDisplayData constellation) {
        final List<ConstellationDisplayData> oldValue = constellations;
        final List<ConstellationDisplayData> updated = new ArrayList<>(constellations);
        updated.add(constellation);
        constellations = List.copyOf(updated);
        support.firePropertyChange("constellations", oldValue, constellations);
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
        final String oldValue = this.errorMessage;
        this.errorMessage = errorMessage;
        support.firePropertyChange("errorMessage", oldValue, errorMessage);
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    /**
     * Sets the success message and notifies listeners.
     *
     * @param successMessage the success text, or an empty string to clear it
     */
    public void setSuccessMessage(final String successMessage) {
        final String oldValue = this.successMessage;
        this.successMessage = successMessage;
        support.firePropertyChange("successMessage", oldValue, successMessage);
    }

    /**
     * Registers a listener to be notified of property changes.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
