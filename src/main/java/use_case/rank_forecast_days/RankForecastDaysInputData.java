package use_case.rank_forecast_days;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.ObserverLocation;

public class RankForecastDaysInputData {

    private final ObserverLocation location;
    private final List<LocalDate> selectedDates;

    public RankForecastDaysInputData(
            final ObserverLocation location,
            final List<LocalDate> selectedDates) {
        this.location = location;
        if (selectedDates == null) {
            this.selectedDates = Collections.emptyList();
        }
        else {
            this.selectedDates = Collections.unmodifiableList(new ArrayList<>(selectedDates));
        }
    }

    public ObserverLocation getLocation() {
        return location;
    }

    public List<LocalDate> getSelectedDates() {
        return selectedDates;
    }
}
