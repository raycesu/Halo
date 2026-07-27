package use_case.weather;

import java.time.LocalDateTime;
import java.util.List;

import entity.weather.WeatherCondition;

public interface WeatherDataAccessInterface {

    WeatherCondition getWeatherCondition(double latitude, double longitude, LocalDateTime dateTime)
            throws WeatherUnavailableException;

    /**
     * Fetches weather for multiple datetimes in one underlying request when possible.
     * Returned conditions are ordered to match {@code dateTimes}.
     */
    List<WeatherCondition> getWeatherConditions(
            double latitude, double longitude, List<LocalDateTime> dateTimes)
            throws WeatherUnavailableException;
}
