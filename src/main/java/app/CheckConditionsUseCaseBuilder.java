package app;

import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsPresenter;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInteractor;
import use_case.check_conditions.CheckConditionsOutputBoundary;
import use_case.weather.WeatherDataAccessInterface;

/** Wires the "check conditions" use case: presenter, interactor, and controller. */
final class CheckConditionsUseCaseBuilder {

    private CheckConditionsUseCaseBuilder() {
    }

    static CheckConditionsController build(
            final WeatherDataAccessInterface weatherDataAccess,
            final CheckConditionsViewModel checkConditionsViewModel) {

        final CheckConditionsOutputBoundary presenter =
                new CheckConditionsPresenter(checkConditionsViewModel);
        final CheckConditionsInputBoundary interactor =
                new CheckConditionsInteractor(weatherDataAccess, presenter);
        return new CheckConditionsController(interactor);
    }
}
