package app;

import interface_adapter.ViewManagerModel;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;

/** Builds the three top-level views, wires them to their controllers/ViewModels, and registers them. */
final class ViewFactory {

    private ViewFactory() {
    }

    static ViewManager build(
            final ViewModelBundle viewModels,
            final ControllerBundle controllers) {

        final ObservationSetupView observationSetupView =
                new ObservationSetupView(
                        viewModels.getObservationSetupViewModel(),
                        controllers.getViewSkyController(),
                        viewModels.getViewManagerModel(),
                        controllers.getLookupLocationController(),
                        viewModels.getLookupLocationViewModel());
        final LoadingView loadingView = new LoadingView();
        final SkyView skyView =
                new SkyView(
                        viewModels.getSkyViewModel(),
                        controllers.getCheckConditionsController(),
                        viewModels.getCheckConditionsViewModel(),
                        controllers.getRankForecastDaysController(),
                        viewModels.getRankForecastDaysViewModel(),
                        viewModels.getViewManagerModel());
        skyView.configureCustomConstellations(
                controllers.getConstellationController(),
                viewModels.getConstellationViewModel());

        final ViewManager viewManager = new ViewManager(viewModels.getViewManagerModel());
        viewManager.registerView(ViewManagerModel.OBSERVATION_SETUP_VIEW, observationSetupView);
        viewManager.registerView(ViewManagerModel.LOADING_VIEW, loadingView);
        viewManager.registerView(ViewManagerModel.SKY_VIEW, skyView);
        return viewManager;
    }
}
