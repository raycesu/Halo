package use_case.rank_forecast_days;

public interface RankForecastDaysInputBoundary {

    /**
     * Ranks the given candidate dates by expected observing conditions.
     *
     * @param inputData the observer location and candidate dates to rank
     */
    void rankForecastDays(RankForecastDaysInputData inputData);
}
