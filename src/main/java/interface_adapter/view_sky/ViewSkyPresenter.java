package interface_adapter.view_sky;

import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

public class ViewSkyPresenter implements ViewSkyOutputBoundary {

    private final SkyViewModel skyViewModel;
    private final ObservationSetupViewModel observationSetupViewModel;

    public ViewSkyPresenter(
            final SkyViewModel skyViewModel,
            final ObservationSetupViewModel observationSetupViewModel) {
        this.skyViewModel = skyViewModel;
        this.observationSetupViewModel = observationSetupViewModel;
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
