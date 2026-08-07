package app;

import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationViewModel;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;

/**
 * Groups every ViewModel the app shares between presenters and views, constructed once here
 * instead of separately by each wiring class.
 */
final class ViewModelBundle {

    private final ViewManagerModel viewManagerModel = new ViewManagerModel();
    private final ObservationSetupViewModel observationSetupViewModel = new ObservationSetupViewModel();
    private final SkyViewModel skyViewModel = new SkyViewModel();
    private final CheckConditionsViewModel checkConditionsViewModel = new CheckConditionsViewModel();
    private final RankForecastDaysViewModel rankForecastDaysViewModel = new RankForecastDaysViewModel();
    private final ConstellationViewModel constellationViewModel = new ConstellationViewModel();
    private final LookupLocationViewModel lookupLocationViewModel = new LookupLocationViewModel();

    ViewManagerModel getViewManagerModel() {
        return viewManagerModel;
    }

    ObservationSetupViewModel getObservationSetupViewModel() {
        return observationSetupViewModel;
    }

    SkyViewModel getSkyViewModel() {
        return skyViewModel;
    }

    CheckConditionsViewModel getCheckConditionsViewModel() {
        return checkConditionsViewModel;
    }

    RankForecastDaysViewModel getRankForecastDaysViewModel() {
        return rankForecastDaysViewModel;
    }

    ConstellationViewModel getConstellationViewModel() {
        return constellationViewModel;
    }

    LookupLocationViewModel getLookupLocationViewModel() {
        return lookupLocationViewModel;
    }
}
