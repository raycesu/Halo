package interface_adapter.view_sky;

import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

/**
 * Moves a finished sky into the view model the map reads from.
 *
 * <p>Holds no logic of its own beyond deciding what the user is told. The three paths differ in
 * what survives: a success replaces the map and clears any old message, a warning keeps the map
 * that was just presented and adds a note, and a failure leaves the previous map alone rather
 * than blanking the screen over a mistyped date.
 */
public class ViewSkyPresenter implements ViewSkyOutputBoundary {

    private final SkyViewModel viewModel;

    public ViewSkyPresenter(final SkyViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(final ViewSkyOutputData outputData) {
        viewModel.setDisplayedLocation(outputData.getLocation());
        viewModel.setDisplayedDate(outputData.getDate());
        viewModel.setDisplayedTime(outputData.getTime());
        viewModel.setErrorMessage("");

        // Set last: this is what the map listens for, so everything it might read alongside the
        // stars is already in place by the time the repaint is triggered.
        viewModel.setStars(outputData.getStars());
    }

    @Override
    public void prepareFailView(final String errorMessage) {
        viewModel.setErrorMessage(errorMessage);
    }

    @Override
    public void prepareWarning(final String warningMessage) {
        viewModel.setErrorMessage(warningMessage);
    }
}
