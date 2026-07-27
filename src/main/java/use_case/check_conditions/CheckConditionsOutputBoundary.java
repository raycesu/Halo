package use_case.check_conditions;

// interface with something like presentConditions(CheckConditionsOutputData outputData).
// The interactor calls this when it's done; it has no idea the Presenter is on the other end.

public interface CheckConditionsOutputBoundary {

    void presentConditions(CheckConditionsOutputData outputData);

    // Called instead of presentConditions when the weather data could not be obtained
    // (see WeatherUnavailableException), so failures reach the UI as a message rather
    // than a crash or partially-built output.
    void presentError(String errorMessage);
}
