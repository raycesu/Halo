package entity.weather;

// a plain data holder for the four numbers you care about: cloud cover %, visibility (distance),
// precipitation probability %, and weather_code. No logic, no API
// knowledge — just fields + getters. This is the "shape" everything else passes around.

public class WeatherCondition {

    private final double cloudCoverPercent;
    private final double visibilityMeters;
    private final double precipitationProbabilityPercent;
    private final int weatherCode;

    public WeatherCondition(
            final double cloudCoverPercent,
            final double visibilityMeters,
            final double precipitationProbabilityPercent,
            final int weatherCode) {
        this.cloudCoverPercent = cloudCoverPercent;
        this.visibilityMeters = visibilityMeters;
        this.precipitationProbabilityPercent = precipitationProbabilityPercent;
        this.weatherCode = weatherCode;
    }

    public double getCloudCoverPercent() {
        return cloudCoverPercent;
    }

    public double getVisibilityMeters() {
        return visibilityMeters;
    }

    public double getPrecipitationProbabilityPercent() {
        return precipitationProbabilityPercent;
    }

    public int getWeatherCode() {
        return weatherCode;
    }
}
