package app;

import use_case.view_sky.HorizontalCoordinateCalculator;

/**
 * Wires every use case by delegating to the per-feature use-case builders, then groups the
 * resulting controllers into one bundle for the views to depend on.
 */
final class UseCaseBuilder {

    private UseCaseBuilder() {
    }

    static ControllerBundle build(
            final DataAccessBundle dataAccess,
            final ViewModelBundle viewModels) {

        final HorizontalCoordinateCalculator coordinateCalculator = AstronomyCalculatorFactory.build();

        final ControllerBundle controllers = new ControllerBundle(
                ViewSkyUseCaseBuilder.build(
                        dataAccess.getStarCatalogDataAccess(),
                        dataAccess.getCelestialBodyDataAccess(),
                        coordinateCalculator,
                        viewModels.getSkyViewModel(),
                        viewModels.getObservationSetupViewModel(),
                        viewModels.getCheckConditionsViewModel(),
                        viewModels.getRankForecastDaysViewModel()),
                CheckConditionsUseCaseBuilder.build(
                        dataAccess.getWeatherDataAccess(), viewModels.getCheckConditionsViewModel()),
                RankForecastDaysUseCaseBuilder.build(
                        dataAccess.getWeatherDataAccess(), viewModels.getRankForecastDaysViewModel()),
                ConstellationUseCaseBuilder.build(
                        dataAccess.getConstellationDataAccess(), viewModels.getConstellationViewModel()),
                LookupLocationUseCaseBuilder.build(
                        dataAccess.getLocationDataAccess(), viewModels.getLookupLocationViewModel()));

        return controllers;
    }
}
