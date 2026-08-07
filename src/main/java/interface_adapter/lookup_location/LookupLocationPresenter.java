package interface_adapter.lookup_location;

import java.util.ArrayList;
import java.util.List;

import entity.ObserverLocation;
import use_case.lookup_location.LookupLocationOutputBoundary;
import use_case.lookup_location.LookupLocationOutputData;

public class LookupLocationPresenter implements LookupLocationOutputBoundary {

    private final LookupLocationViewModel viewModel;

    public LookupLocationPresenter(final LookupLocationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(final LookupLocationOutputData outputData) {
        final List<ObserverLocation> entities = outputData.getResults();
        final List<LookupLocationViewModel.LocationSuggestion> suggestions =
                new ArrayList<>(entities.size());
        for (final ObserverLocation entity : entities) {
            suggestions.add(new LookupLocationViewModel.LocationSuggestion(
                    entity.getDisplayName(),
                    entity.getLatitude(),
                    entity.getLongitude(),
                    entity.getZoneId().getId()));
        }
        viewModel.setSuggestions(suggestions);
    }
}
