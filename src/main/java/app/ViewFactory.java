package app;

import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationViewModel;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;

/** Builds the three top-level views, wires them to their controllers/ViewModels, and registers them. */
final class ViewFactory {

    private ViewFactory() {
    }

    static ViewManager build(
            final ViewManagerModel viewManagerModel,
            final ObservationSetupViewModel observationSetupViewModel,
            final SkyViewModel skyViewModel,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysViewModel rankForecastDaysViewModel,
            final ConstellationViewModel constellationViewModel,
            final LookupLocationViewModel lookupLocationViewModel,
            final UseCaseBuilder useCases) {

        final ObservationSetupView observationSetupView =
                new ObservationSetupView(
                        observationSetupViewModel,
                        useCases.getViewSkyController(),
                        viewManagerModel,
                        useCases.getLookupLocationController(),
                        lookupLocationViewModel);
        final LoadingView loadingView = new LoadingView();
        final SkyView skyView =
                new SkyView(
                        skyViewModel,
                        useCases.getCheckConditionsController(),
                        checkConditionsViewModel,
                        useCases.getRankForecastDaysController(),
                        rankForecastDaysViewModel,
                        viewManagerModel);
        skyView.configureCustomConstellations(
                useCases.getConstellationController(),
                constellationViewModel);

        final ViewManager viewManager = new ViewManager(viewManagerModel);
        viewManager.registerView(ViewManagerModel.OBSERVATION_SETUP_VIEW, observationSetupView);
        viewManager.registerView(ViewManagerModel.LOADING_VIEW, loadingView);
        viewManager.registerView(ViewManagerModel.SKY_VIEW, skyView);
        return viewManager;
    }
}
