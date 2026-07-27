package use_case.check_conditions;

// the result: the raw weather numbers plus the computed ViewingQualityRating,
// formatted as plain data ready to hand to a Presenter (no UI objects in here).

import entity.weather.ViewingQualityRating;

public class CheckConditionsOutputData {

    private final double cloudCoverPercent;
    private final double visibilityMeters;
    private final double precipitationProbabilityPercent;
    private final int weatherCode;
    private final double overallScore;
    private final ViewingQualityRating rating;

    public CheckConditionsOutputData(
            final double cloudCoverPercent,
            final double visibilityMeters,
            final double precipitationProbabilityPercent,
            final int weatherCode,
            final double overallScore,
            final ViewingQualityRating rating) {
        this.cloudCoverPercent = cloudCoverPercent;
        this.visibilityMeters = visibilityMeters;
        this.precipitationProbabilityPercent = precipitationProbabilityPercent;
        this.weatherCode = weatherCode;
        this.overallScore = overallScore;
        this.rating = rating;
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

    public double getOverallScore() {
        return overallScore;
    }

    public ViewingQualityRating getRating() {
        return rating;
    }
}
