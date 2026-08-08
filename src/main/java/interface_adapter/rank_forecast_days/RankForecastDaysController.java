package interface_adapter.rank_forecast_days;

import java.time.LocalDate;
import java.time.ZoneId;
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
     * @param displayName the display name of the observer location
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param zoneId the IANA time zone ID string
     * @param selectedDates the candidate dates to rank
     */
    public void rankForecastDays(
            final String displayName,
            final double latitude,
            final double longitude,
            final String zoneId,
            final List<LocalDate> selectedDates) {
        final ObserverLocation location = new ObserverLocation(
                displayName, latitude, longitude, ZoneId.of(zoneId));
        final RankForecastDaysInputData inputData =
                new RankForecastDaysInputData(location, selectedDates);
        inputBoundary.rankForecastDays(inputData);
    }
}
