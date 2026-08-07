package interface_adapter.lookup_location;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import entity.ObserverLocation;

public class LookupLocationViewModel {

    public static final String SUGGESTIONS_PROPERTY = "suggestions";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<ObserverLocation> suggestions = List.of();

    public List<ObserverLocation> getSuggestions() {
        return suggestions;
    }

    /**
     * Replaces the location suggestions and notifies listeners.
     *
     * @param suggestions the new list of suggested locations
     */
    public void setSuggestions(final List<ObserverLocation> suggestions) {
        final List<ObserverLocation> old = this.suggestions;
        this.suggestions = suggestions;
        support.firePropertyChange(SUGGESTIONS_PROPERTY, old, suggestions);
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
