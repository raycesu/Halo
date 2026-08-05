package use_case.weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class WeatherUnavailableExceptionTest {

    @Test
    void carriesAMessageWithoutACause() {
        final WeatherUnavailableException exception =
                new WeatherUnavailableException("Could not reach the weather service.");

        assertEquals("Could not reach the weather service.", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void carriesAMessageAndACause() {
        final Throwable cause = new java.io.IOException("timed out");

        final WeatherUnavailableException exception =
                new WeatherUnavailableException("Weather request was interrupted.", cause);

        assertEquals("Weather request was interrupted.", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
