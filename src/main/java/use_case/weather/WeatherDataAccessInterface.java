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
     */
    WeatherCondition getWeatherCondition(ObserverLocation location, LocalDateTime dateTime)
            throws WeatherUnavailableException;

    /**
     * Fetches weather for multiple datetimes in one underlying request when possible.
     * Returned conditions are ordered to match {@code dateTimes}.
     */
    List<WeatherCondition> getWeatherConditions(
            ObserverLocation location, List<LocalDateTime> dateTimes)
            throws WeatherUnavailableException;
}
