package interface_adapter.check_conditions;

// holds the state the View binds to/observes
// (cloud cover text, rating label, maybe a color for a status indicator).

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CheckConditionsViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String cloudCoverText = "";
    private String visibilityText = "";
    private String precipitationText = "";
    private String weatherCodeText = "";
    private String overallScoreText = "";
    private String ratingText = "";
    private String ratingColor = "";
    private String errorMessage = "";

    public String getCloudCoverText() {
        return cloudCoverText;
    }

    /**
     * Sets the cloud-cover display text and notifies listeners.
     *
     * @param cloudCoverText the formatted cloud-cover text
     */
    public void setCloudCoverText(final String cloudCoverText) {
        final String oldValue = this.cloudCoverText;
        this.cloudCoverText = cloudCoverText;
        support.firePropertyChange("cloudCoverText", oldValue, cloudCoverText);
    }

    public String getVisibilityText() {
        return visibilityText;
    }

    /**
     * Sets the visibility display text and notifies listeners.
     *
     * @param visibilityText the formatted visibility text
     */
    public void setVisibilityText(final String visibilityText) {
        final String oldValue = this.visibilityText;
        this.visibilityText = visibilityText;
        support.firePropertyChange("visibilityText", oldValue, visibilityText);
    }

    public String getPrecipitationText() {
        return precipitationText;
    }

    /**
     * Sets the precipitation display text and notifies listeners.
     *
     * @param precipitationText the formatted precipitation text
     */
    public void setPrecipitationText(final String precipitationText) {
        final String oldValue = this.precipitationText;
        this.precipitationText = precipitationText;
        support.firePropertyChange("precipitationText", oldValue, precipitationText);
    }

    public String getWeatherCodeText() {
        return weatherCodeText;
    }

    /**
     * Sets the weather-code display text and notifies listeners.
     *
     * @param weatherCodeText the formatted weather-code text
     */
    public void setWeatherCodeText(final String weatherCodeText) {
        final String oldValue = this.weatherCodeText;
        this.weatherCodeText = weatherCodeText;
        support.firePropertyChange("weatherCodeText", oldValue, weatherCodeText);
    }

    public String getOverallScoreText() {
        return overallScoreText;
    }

    /**
     * Sets the overall-score display text and notifies listeners.
     *
     * @param overallScoreText the formatted overall-score text
     */
    public void setOverallScoreText(final String overallScoreText) {
        final String oldValue = this.overallScoreText;
        this.overallScoreText = overallScoreText;
        support.firePropertyChange("overallScoreText", oldValue, overallScoreText);
    }

    public String getRatingText() {
        return ratingText;
    }

    /**
     * Sets the observability-rating display text and notifies listeners.
     *
     * @param ratingText the formatted rating text
     */
    public void setRatingText(final String ratingText) {
        final String oldValue = this.ratingText;
        this.ratingText = ratingText;
        support.firePropertyChange("ratingText", oldValue, ratingText);
    }

    public String getRatingColor() {
        return ratingColor;
    }

    /**
     * Sets the rating status colour and notifies listeners.
     *
     * @param ratingColor the colour identifier for the rating indicator
     */
    public void setRatingColor(final String ratingColor) {
        final String oldValue = this.ratingColor;
        this.ratingColor = ratingColor;
        support.firePropertyChange("ratingColor", oldValue, ratingColor);
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

    /**
     * Clears every displayed field back to its initial empty state. Called whenever a new
     * observation is generated so weather results from a previous location/date/time do not
     * keep showing on screen.
     */
    public void reset() {
        setCloudCoverText("");
        setVisibilityText("");
        setPrecipitationText("");
        setWeatherCodeText("");
        setOverallScoreText("");
        setRatingText("");
        setRatingColor("");
        setErrorMessage("");
    }
}
