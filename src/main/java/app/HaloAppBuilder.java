package app;

import javax.swing.JFrame;

import data_access.OpenMeteoWeatherDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsPresenter;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInteractor;
import use_case.check_conditions.CheckConditionsOutputBoundary;
import use_case.check_conditions.WeatherDataAccessInterface;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;

public class HaloAppBuilder {

    private static final String initial_view = ViewManagerModel.sky_view;

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

        final ViewManager viewManager = new ViewManager(viewManagerModel);
        final ObservationSetupView observationSetupView =
                new ObservationSetupView(observationSetupViewModel);
        final LoadingView loadingView = new LoadingView();
        final SkyView skyView =
                new SkyView(skyViewModel, checkConditionsController, checkConditionsViewModel);

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
}
