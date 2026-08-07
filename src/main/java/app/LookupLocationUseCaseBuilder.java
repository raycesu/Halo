package app;

import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.lookup_location.LookupLocationPresenter;
import interface_adapter.lookup_location.LookupLocationViewModel;
import use_case.lookup_location.LocationDataAccessInterface;
import use_case.lookup_location.LookupLocationInputBoundary;
import use_case.lookup_location.LookupLocationInteractor;
import use_case.lookup_location.LookupLocationOutputBoundary;

/** Wires the "lookup location" use case: presenter, interactor, and controller. */
final class LookupLocationUseCaseBuilder {

    private LookupLocationUseCaseBuilder() {
    }

    static LookupLocationController build(
            final LocationDataAccessInterface locationDataAccess,
            final LookupLocationViewModel lookupLocationViewModel) {

        final LookupLocationOutputBoundary presenter =
                new LookupLocationPresenter(lookupLocationViewModel);
        final LookupLocationInputBoundary interactor =
                new LookupLocationInteractor(locationDataAccess, presenter);
        return new LookupLocationController(interactor);
    }
}
