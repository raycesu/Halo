package app;

import interface_adapter.rank_forecast_days.RankForecastDaysController;
import interface_adapter.rank_forecast_days.RankForecastDaysPresenter;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import use_case.rank_forecast_days.RankForecastDaysInputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInteractor;
import use_case.rank_forecast_days.RankForecastDaysOutputBoundary;
import use_case.weather.WeatherDataAccessInterface;

/** Wires the "rank forecast days" use case: presenter, interactor, and controller. */
final class RankForecastDaysUseCaseBuilder {

    private RankForecastDaysUseCaseBuilder() {
    }

    static RankForecastDaysController build(
            final WeatherDataAccessInterface weatherDataAccess,
            final RankForecastDaysViewModel rankForecastDaysViewModel) {

        final RankForecastDaysOutputBoundary presenter =
                new RankForecastDaysPresenter(rankForecastDaysViewModel);
        final RankForecastDaysInputBoundary interactor =
                new RankForecastDaysInteractor(weatherDataAccess, presenter);
        return new RankForecastDaysController(interactor);
    }
}
