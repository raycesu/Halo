package app;

import java.util.List;

import astronomy.AltAzCalculator;
import astronomy.JulianDateCalculator;
import astronomy.SiderealTimeCalculator;
import data_access.CsvLocationDataAccessObject;
import data_access.CsvStarCatalogDataAccessObject;
import data_access.InMemoryConstellationDataAccessObject;
import data_access.OpenMeteoWeatherDataAccessObject;
import data_access.UsnoCelestialBodyDataAccessObject;
import entity.ObserverLocation;
import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsPresenter;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationController;
import interface_adapter.custom_constellation.ConstellationPresenter;
import interface_adapter.custom_constellation.ConstellationViewModel;
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
import use_case.rank_forecast_days.RankForecastDaysInputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInteractor;
import use_case.rank_forecast_days.RankForecastDaysOutputBoundary;
import use_case.sky.CelestialBodyDataAccessInterface;
import use_case.weather.WeatherDataAccessInterface;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;
import javax.swing.JFrame;
import use_case.location.LocationDataAccessInterface;
import use_case.view_sky.HorizontalCoordinateCalculator;
import use_case.view_sky.StarCatalogDataAccessInterface;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInteractor;
import use_case.view_sky.ViewSkyOutputBoundary;

public class HaloAppBuilder {

    private static final String initial_view = ViewManagerModel.observation_setup_view;

    /** The place the app opens on, so the first request works before the user picks anything. */
    private static final String default_location = "Toronto";

    public JFrame build() {
        final ViewManager viewManager = buildViewManager();

        final JFrame frame = new JFrame("Halo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(viewManager);
        frame.setSize(900, 600);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    /**
     * Looks up the place the app opens on, or null if the dataset does not contain it, in which
     * case the user simply picks one before the first request.
     */
    private ObserverLocation resolveDefaultLocation(
            final LocationDataAccessInterface locationDataAccess) {
        final List<ObserverLocation> matches = locationDataAccess.findByName(default_location, 1);
        return matches.isEmpty() ? null : matches.get(0);
    }

    ViewManager buildViewManager() {
        final ViewManagerModel viewManagerModel = new ViewManagerModel();
        final LocationDataAccessInterface locationDataAccess = new CsvLocationDataAccessObject();
        final ObservationSetupViewModel observationSetupViewModel =
                new ObservationSetupViewModel();
        observationSetupViewModel.setSelectedLocation(
                resolveDefaultLocation(locationDataAccess));
        final SkyViewModel skyViewModel = new SkyViewModel();
        final CheckConditionsViewModel checkConditionsViewModel = new CheckConditionsViewModel();
        final RankForecastDaysViewModel rankForecastDaysViewModel = new RankForecastDaysViewModel();

        final ViewSkyOutputBoundary viewSkyPresenter =
                new ViewSkyPresenter(
                        skyViewModel,
                        observationSetupViewModel,
                        checkConditionsViewModel,
                        rankForecastDaysViewModel);
        final StarCatalogDataAccessInterface starCatalogDataAccess =
                new CsvStarCatalogDataAccessObject();
        final CelestialBodyDataAccessInterface celestialBodyDataAccess =
                new UsnoCelestialBodyDataAccessObject();
        final HorizontalCoordinateCalculator coordinateCalculator =
                new AltAzCalculator(
                        new JulianDateCalculator(),
                        new SiderealTimeCalculator());
        final ViewSkyInputBoundary viewSkyInteractor =
                new ViewSkyInteractor(
                        starCatalogDataAccess,
                        celestialBodyDataAccess,
                        coordinateCalculator,
                        viewSkyPresenter);
        final ViewSkyController viewSkyController =
                new ViewSkyController(viewSkyInteractor, viewSkyPresenter);

        final CheckConditionsOutputBoundary checkConditionsPresenter =
                new CheckConditionsPresenter(checkConditionsViewModel);
        final WeatherDataAccessInterface weatherDataAccess = new OpenMeteoWeatherDataAccessObject();
        final CheckConditionsInputBoundary checkConditionsInteractor =
                new CheckConditionsInteractor(weatherDataAccess, checkConditionsPresenter);
        final CheckConditionsController checkConditionsController =
                new CheckConditionsController(checkConditionsInteractor);

        final RankForecastDaysOutputBoundary rankForecastDaysPresenter =
                new RankForecastDaysPresenter(rankForecastDaysViewModel);
        final RankForecastDaysInputBoundary rankForecastDaysInteractor =
                new RankForecastDaysInteractor(weatherDataAccess, rankForecastDaysPresenter);
        final RankForecastDaysController rankForecastDaysController =
                new RankForecastDaysController(rankForecastDaysInteractor);
        final ConstellationViewModel constellationViewModel = new ConstellationViewModel();
        final ConstellationOutputBoundary constellationPresenter = new ConstellationPresenter(constellationViewModel);
        final ConstellationDataAccessInterface constellationDataAccess = new InMemoryConstellationDataAccessObject();
        final ConstellationInputBoundary constellationInteractor =
                new ConstellationInteractor(constellationDataAccess, constellationPresenter);
        final ConstellationController constellationController =
                new ConstellationController(constellationInteractor);
        final ViewManager viewManager = new ViewManager(viewManagerModel);
        final ObservationSetupView observationSetupView =
                new ObservationSetupView(
                        observationSetupViewModel,
                        viewSkyController,
                        viewManagerModel,
                        locationDataAccess);
        final LoadingView loadingView = new LoadingView();
        final SkyView skyView =
                new SkyView(
                        skyViewModel,
                        checkConditionsController,
                        checkConditionsViewModel,
                        rankForecastDaysController,
                        rankForecastDaysViewModel,
                        viewManagerModel);
        skyView.configureCustomConstellations(
                constellationController,
                constellationViewModel);

        viewManager.registerView(
                ViewManagerModel.observation_setup_view, observationSetupView);
        viewManager.registerView(ViewManagerModel.loading_view, loadingView);
        viewManager.registerView(ViewManagerModel.sky_view, skyView);

        viewManagerModel.setActiveView(initial_view);
        return viewManager;
    }
}
