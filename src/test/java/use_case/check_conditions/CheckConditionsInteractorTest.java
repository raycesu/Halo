package use_case.check_conditions;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import entity.ObserverLocation;
import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.weather.WeatherDataAccessInterface;
import use_case.weather.WeatherUnavailableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The weather DAO is faked here, so these tests never make a network call and never depend on
 * what the real forecast happens to say today.
 */
class CheckConditionsInteractorTest {

    private static final ZoneId ZONE = ZoneId.of("America/Toronto");
    private static final double LATITUDE = 43.6532;
    private static final double LONGITUDE = -79.3832;
    private static final ObserverLocation LOCATION =
            new ObserverLocation("Toronto", LATITUDE, LONGITUDE, ZoneId.of("America/Toronto"));

    // "Now" is fixed at 2026-07-30 for every test so past/future comparisons are deterministic.
    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 7, 30, 12, 0).atZone(ZONE).toInstant(), ZONE);

    private FakeWeatherDataAccess weatherDataAccess;
    private FakeCheckConditionsOutputBoundary outputBoundary;

    @BeforeEach
    void setUp() {
        weatherDataAccess = new FakeWeatherDataAccess();
        outputBoundary = new FakeCheckConditionsOutputBoundary();
    }

    @Test
    void presentsComputedConditionsOnSuccess() {
        weatherDataAccess.conditionToReturn = new WeatherCondition(40.0, 9000.0, 15.0, 1);
        final LocalDateTime observationDateTime = LocalDateTime.of(2026, 7, 30, 23, 0);

        interactor().checkConditions(
                new CheckConditionsInputData(LOCATION, observationDateTime));

        assertTrue(outputBoundary.successCalled);
        assertFalse(outputBoundary.errorCalled);
        assertEquals(LATITUDE, weatherDataAccess.requestedLatitude, 1e-9);
        assertEquals(LONGITUDE, weatherDataAccess.requestedLongitude, 1e-9);
        assertEquals(observationDateTime, weatherDataAccess.requestedDateTime);

        final double expectedScore =
                ViewingQualityRating.calculateOverallScore(weatherDataAccess.conditionToReturn);
        assertEquals(40.0, outputBoundary.outputData.getCloudCoverPercent(), 1e-9);
        assertEquals(9000.0, outputBoundary.outputData.getVisibilityMeters(), 1e-9);
        assertEquals(15.0, outputBoundary.outputData.getPrecipitationProbabilityPercent(), 1e-9);
        assertEquals(1, outputBoundary.outputData.getWeatherCode());
        assertEquals(expectedScore, outputBoundary.outputData.getOverallScore(), 1e-9);
        assertEquals(
                ViewingQualityRating.fromScore(expectedScore),
                outputBoundary.outputData.getRating());
    }

    @Test
    void allowsTheCurrentDateThroughToTheDataAccessObject() {
        weatherDataAccess.conditionToReturn = new WeatherCondition(0.0, 20_000.0, 0.0, 0);

        interactor().checkConditions(new CheckConditionsInputData(
                LOCATION, LocalDateTime.of(2026, 7, 30, 0, 0)));

        assertTrue(weatherDataAccess.wasCalled);
        assertTrue(outputBoundary.successCalled);
    }

    @Test
    void presentsAnErrorWhenTheWeatherServiceFails() {
        weatherDataAccess.failureMessage = "Could not reach the weather service.";

        interactor().checkConditions(new CheckConditionsInputData(
                LOCATION, LocalDateTime.of(2026, 7, 30, 23, 0)));

        assertFalse(outputBoundary.successCalled);
        assertTrue(outputBoundary.errorCalled);
        assertEquals("Could not reach the weather service.", outputBoundary.errorMessage);
    }

    @Test
    void rejectsAPastDateWithoutCallingTheDataAccessObject() {
        interactor().checkConditions(new CheckConditionsInputData(
                LOCATION, LocalDateTime.of(2026, 7, 29, 23, 0)));

        assertFalse(weatherDataAccess.wasCalled);
        assertFalse(outputBoundary.successCalled);
        assertTrue(outputBoundary.errorCalled);
        assertEquals(
                CheckConditionsInteractor.PAST_DATE_ERROR_MESSAGE,
                outputBoundary.errorMessage);
        assertNull(outputBoundary.outputData);
    }

    private CheckConditionsInteractor interactor() {
        return new CheckConditionsInteractor(weatherDataAccess, outputBoundary, FIXED_CLOCK);
    }

    private static class FakeWeatherDataAccess implements WeatherDataAccessInterface {

        private WeatherCondition conditionToReturn;
        private String failureMessage;
        private boolean wasCalled;
        private double requestedLatitude;
        private double requestedLongitude;
        private LocalDateTime requestedDateTime;

        @Override
        public WeatherCondition getWeatherCondition(
                final ObserverLocation location, final LocalDateTime dateTime)
                throws WeatherUnavailableException {
            wasCalled = true;
            requestedLatitude = location.getLatitude();
            requestedLongitude = location.getLongitude();
            requestedDateTime = dateTime;

            if (failureMessage != null) {
                throw new WeatherUnavailableException(failureMessage);
            }
            return conditionToReturn;
        }

        @Override
        public List<WeatherCondition> getWeatherConditions(
                final ObserverLocation location, final List<LocalDateTime> dateTimes)
                throws WeatherUnavailableException {
            throw new UnsupportedOperationException(
                    "CheckConditionsInteractor should only call getWeatherCondition.");
        }
    }

    private static class FakeCheckConditionsOutputBoundary
            implements CheckConditionsOutputBoundary {

        private boolean successCalled;
        private boolean errorCalled;
        private CheckConditionsOutputData outputData;
        private String errorMessage;

        @Override
        public void presentConditions(final CheckConditionsOutputData outputData) {
            successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void presentError(final String errorMessage) {
            errorCalled = true;
            this.errorMessage = errorMessage;
        }
    }
}
