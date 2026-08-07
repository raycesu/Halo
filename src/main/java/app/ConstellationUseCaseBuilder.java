package app;

import interface_adapter.custom_constellation.ConstellationController;
import interface_adapter.custom_constellation.ConstellationPresenter;
import interface_adapter.custom_constellation.ConstellationViewModel;
import use_case.custom_constellation.ConstellationDataAccessInterface;
import use_case.custom_constellation.ConstellationInputBoundary;
import use_case.custom_constellation.ConstellationInteractor;
import use_case.custom_constellation.ConstellationOutputBoundary;

/** Wires the custom-constellation use case: presenter, interactor, and controller. */
final class ConstellationUseCaseBuilder {

    private ConstellationUseCaseBuilder() {
    }

    static ConstellationController build(
            final ConstellationDataAccessInterface constellationDataAccess,
            final ConstellationViewModel constellationViewModel) {

        final ConstellationOutputBoundary presenter =
                new ConstellationPresenter(constellationViewModel);
        final ConstellationInputBoundary interactor =
                new ConstellationInteractor(constellationDataAccess, presenter);
        return new ConstellationController(interactor);
    }
}
