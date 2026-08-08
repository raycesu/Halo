package interface_adapter.lookup_location;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class LookupLocationViewModel {

    public static final String SUGGESTIONS_PROPERTY = "suggestions";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<LocationSuggestion> suggestions = List.of();

    public List<LocationSuggestion> getSuggestions() {
        return suggestions;
    }

    /**
     * Replaces the location suggestions and notifies listeners.
     *
     * @param suggestions the new list of suggested locations
     */
    public void setSuggestions(final List<LocationSuggestion> suggestions) {
        final List<LocationSuggestion> old = this.suggestions;
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

    public static final class LocationSuggestion {

        private final String displayName;
        private final double latitude;
        private final double longitude;
        private final String zoneId;

        public LocationSuggestion(
                final String displayName,
                final double latitude,
                final double longitude,
                final String zoneId) {
            this.displayName = displayName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.zoneId = zoneId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getZoneId() {
            return zoneId;
        }
    }
}
