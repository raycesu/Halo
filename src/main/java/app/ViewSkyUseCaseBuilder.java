package app;

import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import interface_adapter.view_sky.ViewSkyController;
import interface_adapter.view_sky.ViewSkyPresenter;
import use_case.view_sky.CelestialBodyDataAccessInterface;
import use_case.view_sky.HorizontalCoordinateCalculator;
import use_case.view_sky.StarCatalogDataAccessInterface;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInteractor;
import use_case.view_sky.ViewSkyOutputBoundary;

/** Wires the "view the sky" use case: presenter, interactor, and controller. */
final class ViewSkyUseCaseBuilder {

    private ViewSkyUseCaseBuilder() {
    }

    static ViewSkyController build(
            final StarCatalogDataAccessInterface starCatalogDataAccess,
            final CelestialBodyDataAccessInterface celestialBodyDataAccess,
            final HorizontalCoordinateCalculator coordinateCalculator,
            final SkyViewModel skyViewModel,
            final ObservationSetupViewModel observationSetupViewModel,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysViewModel rankForecastDaysViewModel) {

        final ViewSkyOutputBoundary presenter = new ViewSkyPresenter(
                skyViewModel,
                observationSetupViewModel,
                checkConditionsViewModel,
                rankForecastDaysViewModel);
        final ViewSkyInputBoundary interactor = new ViewSkyInteractor(
                starCatalogDataAccess, celestialBodyDataAccess, coordinateCalculator, presenter);
        return new ViewSkyController(interactor, presenter);
    }
}
