package app;

import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.custom_constellation.ConstellationController;
import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.rank_forecast_days.RankForecastDaysController;
import interface_adapter.view_sky.ViewSkyController;

/** Groups every controller the views need, so views depend on this one bundle. */
final class ControllerBundle {

    private final ViewSkyController viewSkyController;
    private final CheckConditionsController checkConditionsController;
    private final RankForecastDaysController rankForecastDaysController;
    private final ConstellationController constellationController;
    private final LookupLocationController lookupLocationController;

    ControllerBundle(
            final ViewSkyController viewSkyController,
            final CheckConditionsController checkConditionsController,
            final RankForecastDaysController rankForecastDaysController,
            final ConstellationController constellationController,
            final LookupLocationController lookupLocationController) {
        this.viewSkyController = viewSkyController;
        this.checkConditionsController = checkConditionsController;
        this.rankForecastDaysController = rankForecastDaysController;
        this.constellationController = constellationController;
        this.lookupLocationController = lookupLocationController;
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
}
