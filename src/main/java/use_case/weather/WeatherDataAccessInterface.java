package use_case.weather;

import java.time.LocalDateTime;
import java.util.List;

import entity.ObserverLocation;
import entity.weather.WeatherCondition;

public interface WeatherDataAccessInterface {

    /**
     * Fetches the weather at one place and moment.
     *
     * <p>The moment is a {@link LocalDateTime} read in the location's own zone, because a local
     * date and time is what a user means when they ask about tonight at 11pm. Taking the place as
     * an {@link ObserverLocation} rather than a pair of coordinates is what makes that resolvable:
     * the location carries the zone with it.
     *
     * @param location the observer's location
     * @param dateTime the local date and time to fetch weather for
     * @return the weather condition at the given place and moment
     * @throws WeatherUnavailableException if the weather could not be obtained
     */
    WeatherCondition getWeatherCondition(ObserverLocation location, LocalDateTime dateTime)
            throws WeatherUnavailableException;

    /**
     * Fetches weather for multiple datetimes in one underlying request when possible.
     * Returned conditions are ordered to match {@code dateTimes}.
     *
     * @param location the observer's location
     * @param dateTimes the local dates and times to fetch weather for
     * @return the weather conditions, in the same order as {@code dateTimes}
     * @throws WeatherUnavailableException if the weather could not be obtained
     */
    List<WeatherCondition> getWeatherConditions(
            ObserverLocation location, List<LocalDateTime> dateTimes)
            throws WeatherUnavailableException;
}
