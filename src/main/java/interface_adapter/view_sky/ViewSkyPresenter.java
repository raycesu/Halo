package interface_adapter.view_sky;

import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

public class ViewSkyPresenter implements ViewSkyOutputBoundary {

    private final SkyViewModel skyViewModel;
    private final ObservationSetupViewModel observationSetupViewModel;
    private final CheckConditionsViewModel checkConditionsViewModel;
    private final RankForecastDaysViewModel rankForecastDaysViewModel;

    public ViewSkyPresenter(
            final SkyViewModel skyViewModel,
            final ObservationSetupViewModel observationSetupViewModel,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysViewModel rankForecastDaysViewModel) {
        this.skyViewModel = skyViewModel;
        this.observationSetupViewModel = observationSetupViewModel;
        this.checkConditionsViewModel = checkConditionsViewModel;
        this.rankForecastDaysViewModel = rankForecastDaysViewModel;
    }

    @Override
    public void prepareSuccessView(final ViewSkyOutputData outputData) {
        skyViewModel.setDisplayedLocation(outputData.getLocation());
        skyViewModel.setDisplayedDate(outputData.getDate());
        skyViewModel.setDisplayedTime(outputData.getTime());
        skyViewModel.setLatitude(outputData.getLatitude());
        skyViewModel.setLongitude(outputData.getLongitude());
        skyViewModel.setStars(outputData.getStars());
        skyViewModel.setSelectedObject(null);
        skyViewModel.setSelectedObjectDetails("");
        skyViewModel.setWarningMessage("");
        skyViewModel.setErrorMessage("");
        observationSetupViewModel.setErrorMessage("");

        // A new observation must not keep showing weather/forecast results from whatever
        // location, date, or time was previously selected.
        checkConditionsViewModel.reset();
        rankForecastDaysViewModel.reset();
    }

    @Override
    public void prepareFailView(final String errorMessage) {
        observationSetupViewModel.setErrorMessage(errorMessage);
    }

    @Override
    public void prepareWarning(final String warningMessage) {
        skyViewModel.setWarningMessage(warningMessage);
    }
}
