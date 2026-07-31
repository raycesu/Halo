package use_case.rank_forecast_days;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;
import use_case.weather.WeatherDataAccessInterface;
import use_case.weather.WeatherUnavailableException;

public class RankForecastDaysInteractor implements RankForecastDaysInputBoundary {

    // Fixed nighttime proxy for ranking: each selected calendar day is evaluated at 23:00
    // local civil time rather than true astronomical darkness for that location/date.
    private static final int OBSERVATION_HOUR = 23;

    // The Open-Meteo forecast API has no data before today, so a request that includes a past
    // date is rejected here rather than being sent to the DAO.
    static final String PAST_DATE_ERROR_MESSAGE =
            "Forecast ranking is not available for past dates.";

    private final WeatherDataAccessInterface weatherDataAccess;
    private final RankForecastDaysOutputBoundary outputBoundary;
    private final Clock clock;

    public RankForecastDaysInteractor(
            final WeatherDataAccessInterface weatherDataAccess,
            final RankForecastDaysOutputBoundary outputBoundary) {
        this(weatherDataAccess, outputBoundary, Clock.systemDefaultZone());
    }

    public RankForecastDaysInteractor(
            final WeatherDataAccessInterface weatherDataAccess,
            final RankForecastDaysOutputBoundary outputBoundary,
            final Clock clock) {
        this.weatherDataAccess = weatherDataAccess;
        this.outputBoundary = outputBoundary;
        this.clock = clock;
    }

    @Override
    public void rankForecastDays(final RankForecastDaysInputData inputData) {
        final List<LocalDate> selectedDates = inputData.getSelectedDates();
        if (selectedDates.isEmpty()) {
            outputBoundary.presentError("Select at least one day to rank.");
            return;
        }

        final LocalDate today = LocalDate.now(clock);
        for (final LocalDate date : selectedDates) {
            if (date.isBefore(today)) {
                outputBoundary.presentError(PAST_DATE_ERROR_MESSAGE);
                return;
            }
        }

        try {
            final List<LocalDateTime> dateTimes = new ArrayList<>(selectedDates.size());
            for (final LocalDate date : selectedDates) {
                dateTimes.add(date.atTime(OBSERVATION_HOUR, 0));
            }

            final List<WeatherCondition> conditions = weatherDataAccess.getWeatherConditions(
                    inputData.getLatitude(),
                    inputData.getLongitude(),
                    dateTimes);

            final List<ScoredDay> scoredDays = new ArrayList<>(selectedDates.size());
            for (int i = 0; i < selectedDates.size(); i++) {
                final WeatherCondition condition = conditions.get(i);
                final double overallScore = ViewingQualityRating.calculateOverallScore(condition);
                scoredDays.add(new ScoredDay(
                        selectedDates.get(i),
                        condition,
                        overallScore,
                        ViewingQualityRating.fromScore(overallScore)));
            }

            scoredDays.sort(Comparator
                    .comparingDouble(ScoredDay::getOverallScore).reversed()
                    .thenComparing(ScoredDay::getDate));

            final List<RankedDayResult> rankedDays = new ArrayList<>(scoredDays.size());
            for (int i = 0; i < scoredDays.size(); i++) {
                final ScoredDay scoredDay = scoredDays.get(i);
                rankedDays.add(new RankedDayResult(
                        scoredDay.getDate(),
                        scoredDay.getCondition(),
                        scoredDay.getOverallScore(),
                        scoredDay.getRating(),
                        i + 1));
            }

            outputBoundary.presentRankedDays(new RankForecastDaysOutputData(rankedDays));
        }
        catch (WeatherUnavailableException exception) {
            outputBoundary.presentError(exception.getMessage());
        }
    }

    private static final class ScoredDay {

        private final LocalDate date;
        private final WeatherCondition condition;
        private final double overallScore;
        private final ViewingQualityRating rating;

        private ScoredDay(
                final LocalDate date,
                final WeatherCondition condition,
                final double overallScore,
                final ViewingQualityRating rating) {
            this.date = date;
            this.condition = condition;
            this.overallScore = overallScore;
            this.rating = rating;
        }

        private LocalDate getDate() {
            return date;
        }

        private WeatherCondition getCondition() {
            return condition;
        }

        private double getOverallScore() {
            return overallScore;
        }

        private ViewingQualityRating getRating() {
            return rating;
        }
    }
}
