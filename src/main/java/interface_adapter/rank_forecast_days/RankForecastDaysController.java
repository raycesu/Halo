package interface_adapter.rank_forecast_days;

import java.time.LocalDate;
import java.util.List;

import entity.ObserverLocation;
import use_case.rank_forecast_days.RankForecastDaysInputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInputData;

public class RankForecastDaysController {

    private final RankForecastDaysInputBoundary inputBoundary;

    public RankForecastDaysController(final RankForecastDaysInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    /**
     * Forwards a forecast-ranking request to the use case.
     *
     * @param location the observer location to rank forecast days for
     * @param selectedDates the candidate dates to rank
     */
    public void rankForecastDays(
            final ObserverLocation location,
            final List<LocalDate> selectedDates) {
        final RankForecastDaysInputData inputData =
                new RankForecastDaysInputData(location, selectedDates);
        inputBoundary.rankForecastDays(inputData);
    }
}
