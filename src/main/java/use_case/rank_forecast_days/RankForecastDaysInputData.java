package use_case.rank_forecast_days;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankForecastDaysInputData {

    private final double latitude;
    private final double longitude;
    private final List<LocalDate> selectedDates;

    public RankForecastDaysInputData(
            final double latitude,
            final double longitude,
            final List<LocalDate> selectedDates) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (selectedDates == null) {
            this.selectedDates = Collections.emptyList();
        }
        else {
            this.selectedDates = Collections.unmodifiableList(new ArrayList<>(selectedDates));
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<LocalDate> getSelectedDates() {
        return selectedDates;
    }
}
