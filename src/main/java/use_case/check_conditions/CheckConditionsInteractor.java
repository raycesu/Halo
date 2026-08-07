package use_case.check_conditions;

// implements CheckConditionsInputBoundary. This is the orchestrator:
// Takes CheckConditionsInputData
// Calls WeatherDataAccessInterface.getWeather(location, datetime) to get data
// Builds a WeatherCondition from the response
// Runs it through ViewingQualityRating
// Packages everything into CheckConditionsOutputData
// Calls outputBoundary.presentConditions(outputData)
// It depends only on interfaces (WeatherDataAccessInterface,
// CheckConditionsOutputBoundary) — never on the concrete DAO or Presenter classes.

import java.time.Clock;
import java.time.LocalDate;

import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;
import use_case.weather.WeatherDataAccessInterface;
import use_case.weather.WeatherUnavailableException;

public class CheckConditionsInteractor implements CheckConditionsInputBoundary {

    // The Open-Meteo forecast API has no data before today, so a request for a past date is
    // rejected here rather than being sent to the DAO.
    static final String PAST_DATE_ERROR_MESSAGE =
            "Weather conditions are not available for past dates.";

    private final WeatherDataAccessInterface weatherDataAccess;
    private final CheckConditionsOutputBoundary outputBoundary;
    private final Clock clock;

    public CheckConditionsInteractor(
            final WeatherDataAccessInterface weatherDataAccess,
            final CheckConditionsOutputBoundary outputBoundary) {
        this(weatherDataAccess, outputBoundary, Clock.systemDefaultZone());
    }

    public CheckConditionsInteractor(
            final WeatherDataAccessInterface weatherDataAccess,
            final CheckConditionsOutputBoundary outputBoundary,
            final Clock clock) {
        this.weatherDataAccess = weatherDataAccess;
        this.outputBoundary = outputBoundary;
        this.clock = clock;
    }

    @Override
    public void checkConditions(final CheckConditionsInputData inputData) {
        final LocalDate observationDate = inputData.getObservationDateTime().toLocalDate();
        if (observationDate.isBefore(LocalDate.now(clock))) {
            outputBoundary.presentError(PAST_DATE_ERROR_MESSAGE);
        }
        else {
            try {
                final WeatherCondition condition = weatherDataAccess.getWeatherCondition(
                        inputData.getLocation(),
                        inputData.getObservationDateTime());

                final double overallScore = ViewingQualityRating.calculateOverallScore(condition);
                final ViewingQualityRating rating = ViewingQualityRating.fromScore(overallScore);

                final CheckConditionsOutputData outputData = new CheckConditionsOutputData(
                        condition.getCloudCoverPercent(),
                        condition.getVisibilityMeters(),
                        condition.getPrecipitationProbabilityPercent(),
                        condition.getWeatherCode(),
                        overallScore,
                        rating);

                outputBoundary.presentConditions(outputData);
            }
            catch (WeatherUnavailableException exception) {
                outputBoundary.presentError(exception.getMessage());
            }
        }
    }
}
