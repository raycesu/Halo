package use_case.rank_forecast_days;

public interface RankForecastDaysOutputBoundary {

    void presentRankedDays(RankForecastDaysOutputData outputData);

    void presentError(String errorMessage);
}
