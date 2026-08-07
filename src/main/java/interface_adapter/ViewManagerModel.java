package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewManagerModel {

    public static final String OBSERVATION_SETUP_VIEW = "observation setup";
    public static final String LOADING_VIEW = "loading";
    public static final String SKY_VIEW = "sky";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String activeView = OBSERVATION_SETUP_VIEW;

    public String getActiveView() {
        return activeView;
    }

    /**
     * Sets the active full-screen view and notifies listeners.
     *
     * @param activeView the name of the view to activate
     */
    public void setActiveView(final String activeView) {
        final String oldActiveView = this.activeView;
        this.activeView = activeView;
        support.firePropertyChange("activeView", oldActiveView, activeView);
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
