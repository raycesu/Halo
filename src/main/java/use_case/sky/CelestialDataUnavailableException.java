package use_case.sky;

/**
 * Thrown by {@link CelestialBodyDataAccessInterface} implementations when position data cannot
 * be obtained or understood: a network failure, a non-200 response, an error reported by the
 * service, or a response whose shape does not match what was expected.
 *
 * <p>Mirrors {@code use_case.weather.WeatherUnavailableException}: it lets an interactor turn a
 * failure into a clean call on its output boundary instead of letting a low-level exception
 * bubble up into the UI.
 */
public class CelestialDataUnavailableException extends Exception {

    public CelestialDataUnavailableException(final String message) {
        super(message);
    }

    public CelestialDataUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
