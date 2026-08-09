package app;

import astronomy.AltAzCalculator;
import astronomy.JulianDateCalculator;
import astronomy.SiderealTimeCalculator;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsPresenter;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationController;
import interface_adapter.custom_constellation.ConstellationPresenter;
import interface_adapter.custom_constellation.ConstellationViewModel;
import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.lookup_location.LookupLocationPresenter;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysController;
import interface_adapter.rank_forecast_days.RankForecastDaysPresenter;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import interface_adapter.view_sky.ViewSkyController;
import interface_adapter.view_sky.ViewSkyPresenter;
import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInteractor;
import use_case.check_conditions.CheckConditionsOutputBoundary;
import use_case.custom_constellation.ConstellationDataAccessInterface;
import use_case.custom_constellation.ConstellationInputBoundary;
import use_case.custom_constellation.ConstellationInteractor;
import use_case.custom_constellation.ConstellationOutputBoundary;
import use_case.lookup_location.LocationDataAccessInterface;
import use_case.lookup_location.LookupLocationInputBoundary;
import use_case.lookup_location.LookupLocationInteractor;
import use_case.lookup_location.LookupLocationOutputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInteractor;
import use_case.rank_forecast_days.RankForecastDaysOutputBoundary;
import use_case.view_sky.HorizontalCoordinateCalculator;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInteractor;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.weather.WeatherDataAccessInterface;

/**
 * Wires every use case: builds each presenter, interactor, and controller, then exposes
 * the controllers so the views can be connected to them.
 */
final class UseCaseBuilder {

    private final ViewSkyController viewSkyController;
    private final CheckConditionsController checkConditionsController;
    private final RankForecastDaysController rankForecastDaysController;
    private final ConstellationController constellationController;
    private final LookupLocationController lookupLocationController;

    UseCaseBuilder(
            final DataAccessBundle dataAccess,
            final SkyViewModel skyViewModel,
            final ObservationSetupViewModel observationSetupViewModel,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysViewModel rankForecastDaysViewModel,
            final ConstellationViewModel constellationViewModel,
            final LookupLocationViewModel lookupLocationViewModel) {

        viewSkyController = buildViewSky(
                dataAccess,
                skyViewModel, observationSetupViewModel,
                checkConditionsViewModel, rankForecastDaysViewModel,
                constellationViewModel);
        checkConditionsController = buildCheckConditions(
                dataAccess.getWeatherDataAccess(), checkConditionsViewModel);
        rankForecastDaysController = buildRankForecastDays(
                dataAccess.getWeatherDataAccess(), rankForecastDaysViewModel);
        constellationController = buildConstellation(
                dataAccess.getConstellationDataAccess(), constellationViewModel);
        lookupLocationController = buildLookupLocation(
                dataAccess.getLocationDataAccess(), lookupLocationViewModel);
    }

    ViewSkyController getViewSkyController() {
        return viewSkyController;
    }

    CheckConditionsController getCheckConditionsController() {
        return checkConditionsController;
    }

    RankForecastDaysController getRankForecastDaysController() {
        return rankForecastDaysController;
    }

    ConstellationController getConstellationController() {
        return constellationController;
    }

    LookupLocationController getLookupLocationController() {
        return lookupLocationController;
    }

    private static ViewSkyController buildViewSky(
            final DataAccessBundle dataAccess,
            final SkyViewModel skyViewModel,
            final ObservationSetupViewModel observationSetupViewModel,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysViewModel rankForecastDaysViewModel,
            final ConstellationViewModel constellationViewModel) {
        final HorizontalCoordinateCalculator coordinateCalculator =
                new AltAzCalculator(new JulianDateCalculator(), new SiderealTimeCalculator());
        final ViewSkyOutputBoundary presenter = new ViewSkyPresenter(
                skyViewModel, observationSetupViewModel,
                checkConditionsViewModel, rankForecastDaysViewModel,
                constellationViewModel);
        final ViewSkyInputBoundary interactor = new ViewSkyInteractor(
                dataAccess.getStarCatalogDataAccess(),
                dataAccess.getStaticConstellationDataAccess(),
                dataAccess.getConstellationDataAccess(),
                dataAccess.getCelestialBodyDataAccess(),
                coordinateCalculator,
                presenter);
        return new ViewSkyController(interactor, presenter);
    }

    private static CheckConditionsController buildCheckConditions(
            final WeatherDataAccessInterface weatherDataAccess,
            final CheckConditionsViewModel checkConditionsViewModel) {
        final CheckConditionsOutputBoundary presenter =
                new CheckConditionsPresenter(checkConditionsViewModel);
        final CheckConditionsInputBoundary interactor =
                new CheckConditionsInteractor(weatherDataAccess, presenter);
        return new CheckConditionsController(interactor);
    }

    private static RankForecastDaysController buildRankForecastDays(
            final WeatherDataAccessInterface weatherDataAccess,
            final RankForecastDaysViewModel rankForecastDaysViewModel) {
        final RankForecastDaysOutputBoundary presenter =
                new RankForecastDaysPresenter(rankForecastDaysViewModel);
        final RankForecastDaysInputBoundary interactor =
                new RankForecastDaysInteractor(weatherDataAccess, presenter);
        return new RankForecastDaysController(interactor);
    }

    private static ConstellationController buildConstellation(
            final ConstellationDataAccessInterface constellationDataAccess,
            final ConstellationViewModel constellationViewModel) {
        final ConstellationOutputBoundary presenter =
                new ConstellationPresenter(constellationViewModel);
        final ConstellationInputBoundary interactor =
                new ConstellationInteractor(constellationDataAccess, presenter);
        return new ConstellationController(interactor);
    }

    private static LookupLocationController buildLookupLocation(
            final LocationDataAccessInterface locationDataAccess,
            final LookupLocationViewModel lookupLocationViewModel) {
        final LookupLocationOutputBoundary presenter =
                new LookupLocationPresenter(lookupLocationViewModel);
        final LookupLocationInputBoundary interactor =
                new LookupLocationInteractor(locationDataAccess, presenter);
        return new LookupLocationController(interactor);
    }
}
