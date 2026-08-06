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

    public void setSuggestions(final List<ObserverLocation> suggestions) {
        final List<ObserverLocation> old = this.suggestions;
        this.suggestions = suggestions;
        support.firePropertyChange(SUGGESTIONS_PROPERTY, old, suggestions);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
