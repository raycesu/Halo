package use_case.check_conditions;

// implements CheckConditionsInputBoundary. This is the orchestrator:
// Takes CheckConditionsInputData
// Calls WeatherDataAccessInterface.getWeather(location, datetime) to get data
// Builds a WeatherCondition from the response
 //Runs it through ViewingQualityRating
// Packages everything into CheckConditionsOutputData
// Calls outputBoundary.presentConditions(outputData)
// It depends only on interfaces (WeatherDataAccessInterface,
// CheckConditionsOutputBoundary) — never on the concrete DAO or Presenter classes.

import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;
import use_case.weather.WeatherDataAccessInterface;
import use_case.weather.WeatherUnavailableException;

public class CheckConditionsInteractor implements CheckConditionsInputBoundary {

    private final WeatherDataAccessInterface weatherDataAccess;
    private final CheckConditionsOutputBoundary outputBoundary;

    public CheckConditionsInteractor(
            final WeatherDataAccessInterface weatherDataAccess,
            final CheckConditionsOutputBoundary outputBoundary) {
        this.weatherDataAccess = weatherDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void checkConditions(final CheckConditionsInputData inputData) {
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
