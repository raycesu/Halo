package interface_adapter.rank_forecast_days;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankForecastDaysViewModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private List<RankedDayDisplayItem> rankedDays = Collections.emptyList();
    private String errorMessage = "";

    public List<RankedDayDisplayItem> getRankedDays() {
        return rankedDays;
    }

    public void setRankedDays(final List<RankedDayDisplayItem> rankedDays) {
        final List<RankedDayDisplayItem> oldValue = this.rankedDays;
        if (rankedDays == null) {
            this.rankedDays = Collections.emptyList();
        }
        else {
            this.rankedDays = Collections.unmodifiableList(new ArrayList<>(rankedDays));
        }
        support.firePropertyChange("rankedDays", oldValue, this.rankedDays);
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

    public static final class RankedDayDisplayItem {

        private final int rank;
        private final String dateText;
        private final String ratingText;
        private final String ratingColor;
        private final String overallScoreText;

        public RankedDayDisplayItem(
                final int rank,
                final String dateText,
                final String ratingText,
                final String ratingColor,
                final String overallScoreText) {
            this.rank = rank;
            this.dateText = dateText;
            this.ratingText = ratingText;
            this.ratingColor = ratingColor;
            this.overallScoreText = overallScoreText;
        }

        public int getRank() {
            return rank;
        }

        public String getDateText() {
            return dateText;
        }

        public String getRatingText() {
            return ratingText;
        }

        public String getRatingColor() {
            return ratingColor;
        }

        public String getOverallScoreText() {
            return overallScoreText;
        }
    }
}
