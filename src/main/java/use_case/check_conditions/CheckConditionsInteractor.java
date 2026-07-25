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

public class CheckConditionsInteractor {
}
