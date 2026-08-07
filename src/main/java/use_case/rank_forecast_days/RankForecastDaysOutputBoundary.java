package use_case.rank_forecast_days;

public interface RankForecastDaysOutputBoundary {

    /**
     * Presents the ranked forecast days.
     *
     * @param outputData the ranked-day results
     */
    void presentRankedDays(RankForecastDaysOutputData outputData);

    /**
     * Presents an error in place of the ranked results.
     *
     * @param errorMessage a message describing why ranking could not be completed
     */
    void presentError(String errorMessage);
}
