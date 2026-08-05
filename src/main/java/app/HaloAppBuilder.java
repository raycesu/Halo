package app;

import java.util.List;

import javax.swing.JFrame;

import data_access.CsvLocationDataAccessObject;
import data_access.CsvStarCatalogDataAccessObject;
import data_access.OpenMeteoWeatherDataAccessObject;
import data_access.UsnoCelestialBodyDataAccessObject;
import entity.ObserverLocation;
import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsPresenter;
import interface_adapter.check_conditions.CheckConditionsViewModel;
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
import use_case.location.LocationDataAccessInterface;
import use_case.rank_forecast_days.RankForecastDaysInputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInteractor;
import use_case.rank_forecast_days.RankForecastDaysOutputBoundary;
import use_case.sky.CelestialBodyDataAccessInterface;
import use_case.view_sky.StarCatalogDataAccessInterface;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInteractor;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.weather.WeatherDataAccessInterface;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;

public class HaloAppBuilder {

    private static final String initial_view = ViewManagerModel.sky_view;

    /** The place the app opens on, so the first request works before the user picks anything. */
    private static final String default_location = "Toronto";

    public JFrame build() {
        final ViewManagerModel viewManagerModel = new ViewManagerModel();
        final ObservationSetupViewModel observationSetupViewModel =
                new ObservationSetupViewModel();
        final SkyViewModel skyViewModel = new SkyViewModel();
        final CheckConditionsViewModel checkConditionsViewModel = new CheckConditionsViewModel();

        final CheckConditionsOutputBoundary checkConditionsPresenter =
                new CheckConditionsPresenter(checkConditionsViewModel);
        final WeatherDataAccessInterface weatherDataAccess = new OpenMeteoWeatherDataAccessObject();
        final CheckConditionsInputBoundary checkConditionsInteractor =
                new CheckConditionsInteractor(weatherDataAccess, checkConditionsPresenter);
        final CheckConditionsController checkConditionsController =
                new CheckConditionsController(checkConditionsInteractor);

        final RankForecastDaysViewModel rankForecastDaysViewModel = new RankForecastDaysViewModel();
        final RankForecastDaysOutputBoundary rankForecastDaysPresenter =
                new RankForecastDaysPresenter(rankForecastDaysViewModel);
        final RankForecastDaysInputBoundary rankForecastDaysInteractor =
                new RankForecastDaysInteractor(weatherDataAccess, rankForecastDaysPresenter);
        final RankForecastDaysController rankForecastDaysController =
                new RankForecastDaysController(rankForecastDaysInteractor);

        final LocationDataAccessInterface locationDataAccess = new CsvLocationDataAccessObject();
        final StarCatalogDataAccessInterface starCatalogDataAccess =
                new CsvStarCatalogDataAccessObject();
        final CelestialBodyDataAccessInterface celestialBodyDataAccess =
                new UsnoCelestialBodyDataAccessObject();

        final ViewSkyOutputBoundary viewSkyPresenter = new ViewSkyPresenter(skyViewModel);
        final ViewSkyInputBoundary viewSkyInteractor = new ViewSkyInteractor(
                starCatalogDataAccess, celestialBodyDataAccess, viewSkyPresenter);
        final ViewSkyController viewSkyController =
                new ViewSkyController(viewSkyInteractor, viewSkyPresenter);

        final ViewManager viewManager = new ViewManager(viewManagerModel);
        final ObservationSetupView observationSetupView =
                new ObservationSetupView(observationSetupViewModel);
        final LoadingView loadingView = new LoadingView();
        final SkyView skyView =
                new SkyView(
                        skyViewModel,
                        viewSkyController,
                        locationDataAccess,
                        resolveDefaultLocation(locationDataAccess),
                        checkConditionsController,
                        checkConditionsViewModel,
                        rankForecastDaysController,
                        rankForecastDaysViewModel);

        viewManager.registerView(
                ViewManagerModel.observation_setup_view, observationSetupView);
        viewManager.registerView(ViewManagerModel.loading_view, loadingView);
        viewManager.registerView(ViewManagerModel.sky_view, skyView);

        final JFrame frame = new JFrame("Halo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(viewManager);
        if (ViewManagerModel.sky_view.equals(initial_view)) {
            frame.setSize(1350, 800);
        }
        else {
            frame.setSize(900, 600);
        }
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        viewManagerModel.setActiveView(initial_view);
        return frame;
    }

    /**
     * Looks up the place the app opens on, or null if the dataset does not contain it, in which
     * case the user simply picks one before the first request.
     */
    private ObserverLocation resolveDefaultLocation(
            final LocationDataAccessInterface locationDataAccess) {
        final List<ObserverLocation> matches =
                locationDataAccess.findByName(default_location, 1);
        return matches.isEmpty() ? null : matches.get(0);
    }
}
