package interface_adapter.lookup_location;

import use_case.lookup_location.LookupLocationOutputBoundary;
import use_case.lookup_location.LookupLocationOutputData;

public class LookupLocationPresenter implements LookupLocationOutputBoundary {

    private final LookupLocationViewModel viewModel;

    public LookupLocationPresenter(final LookupLocationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(final LookupLocationOutputData outputData) {
        viewModel.setSuggestions(outputData.getResults());
    }
}
