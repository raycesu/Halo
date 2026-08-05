package use_case.rank_forecast_days;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import entity.ObserverLocation;
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
class RankForecastDaysInteractorTest {

    private static final ZoneId ZONE = ZoneId.of("America/Toronto");
    private static final double LATITUDE = 43.6532;
    private static final double LONGITUDE = -79.3832;
    private static final ObserverLocation LOCATION =
            new ObserverLocation("Toronto", LATITUDE, LONGITUDE, ZoneId.of("America/Toronto"));

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 30);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final LocalDate DAY_AFTER_TOMORROW = TODAY.plusDays(2);

    // "Now" is fixed at 2026-07-30 for every test so past/future comparisons are deterministic.
    private static final Clock FIXED_CLOCK = Clock.fixed(
            TODAY.atTime(12, 0).atZone(ZONE).toInstant(), ZONE);

    private FakeWeatherDataAccess weatherDataAccess;
    private FakeRankForecastDaysOutputBoundary outputBoundary;

    @BeforeEach
    void setUp() {
        weatherDataAccess = new FakeWeatherDataAccess();
        outputBoundary = new FakeRankForecastDaysOutputBoundary();
    }

    @Test
    void presentsAnErrorWhenNoDatesAreSelected() {
        interactor().rankForecastDays(
                new RankForecastDaysInputData(LOCATION, List.of()));

        assertFalse(weatherDataAccess.wasCalled);
        assertFalse(outputBoundary.successCalled);
        assertTrue(outputBoundary.errorCalled);
        assertEquals("Select at least one day to rank.", outputBoundary.errorMessage);
    }

    @Test
    void rejectsASelectionContainingAPastDateWithoutCallingTheDataAccessObject() {
        interactor().rankForecastDays(new RankForecastDaysInputData(
                LOCATION, List.of(TOMORROW, YESTERDAY)));

        assertFalse(weatherDataAccess.wasCalled);
        assertFalse(outputBoundary.successCalled);
        assertTrue(outputBoundary.errorCalled);
        assertEquals(
                RankForecastDaysInteractor.PAST_DATE_ERROR_MESSAGE,
                outputBoundary.errorMessage);
        assertNull(outputBoundary.outputData);
    }

    @Test
    void allowsTheCurrentDateThroughToTheDataAccessObject() {
        weatherDataAccess.conditionsToReturn =
                List.of(new WeatherCondition(0.0, 20_000.0, 0.0, 0));

        interactor().rankForecastDays(
                new RankForecastDaysInputData(LOCATION, List.of(TODAY)));

        assertTrue(weatherDataAccess.wasCalled);
        assertTrue(outputBoundary.successCalled);
    }

    @Test
    void presentsAnErrorWhenTheWeatherServiceFails() {
        weatherDataAccess.failureMessage = "Could not reach the weather service.";

        interactor().rankForecastDays(
                new RankForecastDaysInputData(LOCATION, List.of(TOMORROW)));

        assertFalse(outputBoundary.successCalled);
        assertTrue(outputBoundary.errorCalled);
        assertEquals("Could not reach the weather service.", outputBoundary.errorMessage);
    }

    @Test
    void ranksByScoreDescendingWithTiesBrokenByEarlierDateFirst() {
        // Requested out of calendar order to prove the interactor sorts the results itself.
        final List<LocalDate> selectedDates = List.of(TOMORROW, TODAY, DAY_AFTER_TOMORROW);

        final WeatherCondition bestCondition = new WeatherCondition(0.0, 20_000.0, 0.0, 0);
        final WeatherCondition tiedCondition = new WeatherCondition(50.0, 10_000.0, 10.0, 2);

        // Matches the order the interactor will request: tomorrow, today, day-after-tomorrow.
        weatherDataAccess.conditionsToReturn =
                List.of(tiedCondition, bestCondition, tiedCondition);

        interactor().rankForecastDays(
                new RankForecastDaysInputData(LOCATION, selectedDates));

        assertTrue(outputBoundary.successCalled);
        final List<RankedDayResult> rankedDays = outputBoundary.outputData.getRankedDays();
        assertEquals(3, rankedDays.size());

        // Highest score (today, the best condition) is ranked first.
        assertEquals(TODAY, rankedDays.get(0).getDate());
        assertEquals(1, rankedDays.get(0).getRank());

        // The tie between tomorrow and the day after is broken by earlier date first.
        assertEquals(TOMORROW, rankedDays.get(1).getDate());
        assertEquals(2, rankedDays.get(1).getRank());
        assertEquals(DAY_AFTER_TOMORROW, rankedDays.get(2).getDate());
        assertEquals(3, rankedDays.get(2).getRank());

        assertEquals(
                rankedDays.get(1).getOverallScore(),
                rankedDays.get(2).getOverallScore(),
                1e-9);
        assertTrue(rankedDays.get(0).getOverallScore() > rankedDays.get(1).getOverallScore());
    }

    @Test
    void passesLatitudeAndLongitudeThroughToTheDataAccessObject() {
        weatherDataAccess.conditionsToReturn =
                List.of(new WeatherCondition(0.0, 20_000.0, 0.0, 0));

        interactor().rankForecastDays(
                new RankForecastDaysInputData(LOCATION, List.of(TODAY)));

        assertEquals(LATITUDE, weatherDataAccess.requestedLatitude, 1e-9);
        assertEquals(LONGITUDE, weatherDataAccess.requestedLongitude, 1e-9);
        assertEquals(List.of(TODAY.atTime(23, 0)), weatherDataAccess.requestedDateTimes);
    }

    private RankForecastDaysInteractor interactor() {
        return new RankForecastDaysInteractor(weatherDataAccess, outputBoundary, FIXED_CLOCK);
    }

    private static class FakeWeatherDataAccess implements WeatherDataAccessInterface {

        private List<WeatherCondition> conditionsToReturn;
        private String failureMessage;
        private boolean wasCalled;
        private double requestedLatitude;
        private double requestedLongitude;
        private List<LocalDateTime> requestedDateTimes;

        @Override
        public WeatherCondition getWeatherCondition(
                final ObserverLocation location, final LocalDateTime dateTime)
                throws WeatherUnavailableException {
            throw new UnsupportedOperationException(
                    "RankForecastDaysInteractor should only call getWeatherConditions.");
        }

        @Override
        public List<WeatherCondition> getWeatherConditions(
                final ObserverLocation location, final List<LocalDateTime> dateTimes)
                throws WeatherUnavailableException {
            wasCalled = true;
            requestedLatitude = location.getLatitude();
            requestedLongitude = location.getLongitude();
            requestedDateTimes = dateTimes;

            if (failureMessage != null) {
                throw new WeatherUnavailableException(failureMessage);
            }
            return conditionsToReturn;
        }
    }

    private static class FakeRankForecastDaysOutputBoundary
            implements RankForecastDaysOutputBoundary {

        private boolean successCalled;
        private boolean errorCalled;
        private RankForecastDaysOutputData outputData;
        private String errorMessage;

        @Override
        public void presentRankedDays(final RankForecastDaysOutputData outputData) {
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
