package use_case.rank_forecast_days;

import java.time.LocalDate;

import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;

public class RankedDayResult {

    private final LocalDate date;
    private final WeatherCondition condition;
    private final double overallScore;
    private final ViewingQualityRating rating;
    private final int rank;

    public RankedDayResult(
            final LocalDate date,
            final WeatherCondition condition,
            final double overallScore,
            final ViewingQualityRating rating,
            final int rank) {
        this.date = date;
        this.condition = condition;
        this.overallScore = overallScore;
        this.rating = rating;
        this.rank = rank;
    }

    public LocalDate getDate() {
        return date;
    }

    public WeatherCondition getCondition() {
        return condition;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public ViewingQualityRating getRating() {
        return rating;
    }

    public int getRank() {
        return rank;
    }
}
