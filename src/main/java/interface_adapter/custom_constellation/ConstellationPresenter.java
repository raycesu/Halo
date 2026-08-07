package interface_adapter.custom_constellation;

import use_case.custom_constellation.ConstellationOutputBoundary;
import use_case.custom_constellation.ConstellationOutputData;

public class ConstellationPresenter implements ConstellationOutputBoundary {
    private final ConstellationViewModel viewModel;

    public ConstellationPresenter(final ConstellationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(final ConstellationOutputData outputData) {
        viewModel.addConstellation(outputData.getConstellation());
        viewModel.setErrorMessage("");
        viewModel.setSuccessMessage("Constellation saved: " + outputData.getConstellation().getName());
    }

    @Override
    public void prepareFailureView(final String errorMessage) {
        viewModel.setSuccessMessage("");
        viewModel.setErrorMessage(errorMessage);
    }
}
