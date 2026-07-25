package use_case.check_conditions;

// checked exception thrown by WeatherDataAccessInterface implementations when weather data
// cannot be obtained or does not cover the requested time (network failure, non-200 response,
// malformed response, or a datetime outside the forecast range). Lets the interactor translate
// a failure into a clean presentError(...) call instead of letting a null-pointer or
// index-out-of-bounds exception bubble up to the UI.

public class WeatherUnavailableException extends Exception {

    public WeatherUnavailableException(final String message) {
        super(message);
    }

    public WeatherUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
