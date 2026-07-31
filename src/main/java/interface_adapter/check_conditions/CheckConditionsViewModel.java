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

    public void setCloudCoverText(final String cloudCoverText) {
        final String oldValue = this.cloudCoverText;
        this.cloudCoverText = cloudCoverText;
        support.firePropertyChange("cloudCoverText", oldValue, cloudCoverText);
    }

    public String getVisibilityText() {
        return visibilityText;
    }

    public void setVisibilityText(final String visibilityText) {
        final String oldValue = this.visibilityText;
        this.visibilityText = visibilityText;
        support.firePropertyChange("visibilityText", oldValue, visibilityText);
    }

    public String getPrecipitationText() {
        return precipitationText;
    }

    public void setPrecipitationText(final String precipitationText) {
        final String oldValue = this.precipitationText;
        this.precipitationText = precipitationText;
        support.firePropertyChange("precipitationText", oldValue, precipitationText);
    }

    public String getWeatherCodeText() {
        return weatherCodeText;
    }

    public void setWeatherCodeText(final String weatherCodeText) {
        final String oldValue = this.weatherCodeText;
        this.weatherCodeText = weatherCodeText;
        support.firePropertyChange("weatherCodeText", oldValue, weatherCodeText);
    }

    public String getOverallScoreText() {
        return overallScoreText;
    }

    public void setOverallScoreText(final String overallScoreText) {
        final String oldValue = this.overallScoreText;
        this.overallScoreText = overallScoreText;
        support.firePropertyChange("overallScoreText", oldValue, overallScoreText);
    }

    public String getRatingText() {
        return ratingText;
    }

    public void setRatingText(final String ratingText) {
        final String oldValue = this.ratingText;
        this.ratingText = ratingText;
        support.firePropertyChange("ratingText", oldValue, ratingText);
    }

    public String getRatingColor() {
        return ratingColor;
    }

    public void setRatingColor(final String ratingColor) {
        final String oldValue = this.ratingColor;
        this.ratingColor = ratingColor;
        support.firePropertyChange("ratingColor", oldValue, ratingColor);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        final String oldValue = this.errorMessage;
        this.errorMessage = errorMessage;
        support.firePropertyChange("errorMessage", oldValue, errorMessage);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

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
