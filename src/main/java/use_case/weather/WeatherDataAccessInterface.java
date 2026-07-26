package use_case.weather;

// interface with something like WeatherApiResponse getWeather(double lat, double lon, LocalDateTime time).
// Lives here (inner circle) even though it's implemented by an outer-circle class

import java.time.LocalDateTime;

import entity.weather.WeatherCondition;

public interface WeatherDataAccessInterface {

    WeatherCondition getWeatherCondition(double latitude, double longitude, LocalDateTime dateTime)
            throws WeatherUnavailableException;
}
