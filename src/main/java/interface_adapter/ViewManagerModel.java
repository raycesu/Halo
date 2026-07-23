package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewManagerModel {

    public static final String observation_setup_view = "observation setup";
    public static final String loading_view = "loading";
    public static final String sky_view = "sky";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String activeView = observation_setup_view;

    public String getActiveView() {
        return activeView;
    }

    public void setActiveView(final String activeView) {
        final String oldActiveView = this.activeView;
        this.activeView = activeView;
        support.firePropertyChange("activeView", oldActiveView, activeView);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
