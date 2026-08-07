package entity.weather;

// this is your rubric. Takes a WeatherCondition and produces a rating
// (e.g. an enum POOR / FAIR / GOOD / EXCELLENT or a 0–100 score).
// It should be a pure function you can unit test with fake WeatherCondition objects
// and no network calls. This is also the piece your future "rank the week" use case will reuse.

public enum ViewingQualityRating {

    POOR,
    FAIR,
    GOOD,
    EXCELLENT;

    private static final double CLOUD_COVER_WEIGHT = 0.35;
    private static final double WEATHER_CODE_WEIGHT = 0.30;
    private static final double VISIBILITY_WEIGHT = 0.15;
    private static final double PRECIPITATION_WEIGHT = 0.20;

    private static final double EXCELLENT_THRESHOLD = 75;
    private static final double GOOD_THRESHOLD = 50;
    private static final double FAIR_THRESHOLD = 25;

    // Piecewise-linear anchor points: cloudCoverPercent -> score (lower cloud cover is better).
    private static final double[] CLOUD_COVER_ANCHORS = {0, 10, 30, 60, 100};
    private static final double[] CLOUD_COVER_SCORES = {100, 90, 65, 40, 0};

    // Piecewise-linear anchor points: visibilityMeters -> score (higher visibility is better).
    private static final double[] VISIBILITY_ANCHORS = {0, 2_000, 5_000, 10_000, 20_000};
    private static final double[] VISIBILITY_SCORES = {0, 15, 45, 75, 100};

    // Piecewise-linear anchor points: precipitationProbabilityPercent -> score (lower is better).
    private static final double[] PRECIPITATION_ANCHORS = {0, 10, 30, 50, 100};
    private static final double[] PRECIPITATION_SCORES = {100, 85, 55, 25, 0};

    // WMO weather codes, as returned by Open-Meteo's weather_code field.
    private static final int WMO_CLEAR = 0;
    private static final int WMO_MAINLY_CLEAR = 1;
    private static final int WMO_PARTLY_CLOUDY = 2;
    private static final int WMO_OVERCAST = 3;
    private static final int WMO_FOG = 45;
    private static final int WMO_DEPOSITING_RIME_FOG = 48;
    private static final int WMO_DRIZZLE_LIGHT = 51;
    private static final int WMO_DRIZZLE_MODERATE = 53;
    private static final int WMO_DRIZZLE_DENSE = 55;
    private static final int WMO_FREEZING_DRIZZLE_LIGHT = 56;
    private static final int WMO_FREEZING_DRIZZLE_DENSE = 57;
    private static final int WMO_RAIN_SLIGHT = 61;
    private static final int WMO_RAIN_MODERATE = 63;
    private static final int WMO_RAIN_HEAVY = 65;
    private static final int WMO_FREEZING_RAIN_LIGHT = 66;
    private static final int WMO_FREEZING_RAIN_HEAVY = 67;
    private static final int WMO_SNOW_SLIGHT = 71;
    private static final int WMO_SNOW_MODERATE = 73;
    private static final int WMO_SNOW_HEAVY = 75;
    private static final int WMO_SNOW_GRAINS = 77;
    private static final int WMO_SHOWERS_SLIGHT = 80;
    private static final int WMO_SHOWERS_MODERATE = 81;
    private static final int WMO_SHOWERS_VIOLENT = 82;
    private static final int WMO_SNOW_SHOWERS_SLIGHT = 85;
    private static final int WMO_SNOW_SHOWERS_HEAVY = 86;
    private static final int WMO_THUNDERSTORM = 95;
    private static final int WMO_THUNDERSTORM_SLIGHT_HAIL = 96;
    private static final int WMO_THUNDERSTORM_HEAVY_HAIL = 99;

    // Scores assigned per WMO weather-code family.
    private static final double SCORE_CLEAR = 100;
    private static final double SCORE_MAINLY_CLEAR = 90;
    private static final double SCORE_PARTLY_CLOUDY = 65;
    private static final double SCORE_OVERCAST = 40;
    private static final double SCORE_FOG = 20;
    private static final double SCORE_DRIZZLE = 20;
    private static final double SCORE_RAIN = 10;
    private static final double SCORE_SNOW = 10;
    private static final double SCORE_SHOWERS = 5;
    private static final double SCORE_THUNDERSTORM = 0;
    private static final double SCORE_UNRECOGNIZED_CODE = 50;

    /**
     * Computes the 0-100 overall viewing-quality score for the given weather condition.
     * Exposed separately from {@link #calculateRating(WeatherCondition)} because future
     * features (e.g. ranking multiple nights) need the raw number, not just a bucketed rating.
     *
     * @param condition the weather condition to score
     * @return the overall viewing-quality score, from 0 to 100
     */
    public static double calculateOverallScore(final WeatherCondition condition) {
        final double cloudCoverScore = interpolate(
                condition.getCloudCoverPercent(), CLOUD_COVER_ANCHORS, CLOUD_COVER_SCORES);
        final double visibilityScore = interpolate(
                condition.getVisibilityMeters(), VISIBILITY_ANCHORS, VISIBILITY_SCORES);
        final double precipitationScore = interpolate(
                condition.getPrecipitationProbabilityPercent(), PRECIPITATION_ANCHORS, PRECIPITATION_SCORES);
        final double weatherCodeScore = scoreWeatherCode(condition.getWeatherCode());

        return cloudCoverScore * CLOUD_COVER_WEIGHT
                + weatherCodeScore * WEATHER_CODE_WEIGHT
                + visibilityScore * VISIBILITY_WEIGHT
                + precipitationScore * PRECIPITATION_WEIGHT;
    }

    /**
     * Convenience wrapper: calculates the overall score and buckets it into a rating.
     *
     * @param condition the weather condition to rate
     * @return the bucketed viewing-quality rating
     */
    public static ViewingQualityRating calculateRating(final WeatherCondition condition) {
        return fromScore(calculateOverallScore(condition));
    }

    /**
     * Maps a 0-100 overall score to a rating category using fixed rubric thresholds
     * (not statistical percentiles).
     *
     * @param overallScore the overall viewing-quality score, from 0 to 100
     * @return the bucketed viewing-quality rating
     */
    public static ViewingQualityRating fromScore(final double overallScore) {
        final ViewingQualityRating rating;
        if (overallScore >= EXCELLENT_THRESHOLD) {
            rating = EXCELLENT;
        }
        else if (overallScore >= GOOD_THRESHOLD) {
            rating = GOOD;
        }
        else if (overallScore >= FAIR_THRESHOLD) {
            rating = FAIR;
        }
        else {
            rating = POOR;
        }
        return rating;
    }

    private static double interpolate(final double value, final double[] anchors, final double[] scores) {
        final int lastIndex = anchors.length - 1;
        final double result;
        if (value <= anchors[0]) {
            result = scores[0];
        }
        else if (value >= anchors[lastIndex]) {
            result = scores[lastIndex];
        }
        else {
            result = interpolateBetweenAnchors(value, anchors, scores, lastIndex);
        }
        return result;
    }

    private static double interpolateBetweenAnchors(
            final double value, final double[] anchors, final double[] scores, final int lastIndex) {
        double result = scores[lastIndex];
        for (int i = 0; i < lastIndex; i++) {
            if (value >= anchors[i] && value <= anchors[i + 1]) {
                final double fraction = (value - anchors[i]) / (anchors[i + 1] - anchors[i]);
                result = scores[i] + fraction * (scores[i + 1] - scores[i]);
                break;
            }
        }
        return result;
    }

    private static double scoreWeatherCode(final int weatherCode) {
        final double score;
        switch (weatherCode) {
            case WMO_CLEAR:
                score = SCORE_CLEAR;
                break;
            case WMO_MAINLY_CLEAR:
                score = SCORE_MAINLY_CLEAR;
                break;
            case WMO_PARTLY_CLOUDY:
                score = SCORE_PARTLY_CLOUDY;
                break;
            case WMO_OVERCAST:
                score = SCORE_OVERCAST;
                break;
            case WMO_FOG:
            case WMO_DEPOSITING_RIME_FOG:
                score = SCORE_FOG;
                break;
            default:
                score = scorePrecipitationWeatherCode(weatherCode);
                break;
        }
        return score;
    }

    // WMO codes 51 and above describe precipitation, split out to keep scoreWeatherCode small.
    private static double scorePrecipitationWeatherCode(final int weatherCode) {
        final double score;
        switch (weatherCode) {
            case WMO_DRIZZLE_LIGHT:
            case WMO_DRIZZLE_MODERATE:
            case WMO_DRIZZLE_DENSE:
            case WMO_FREEZING_DRIZZLE_LIGHT:
            case WMO_FREEZING_DRIZZLE_DENSE:
                score = SCORE_DRIZZLE;
                break;
            case WMO_RAIN_SLIGHT:
            case WMO_RAIN_MODERATE:
            case WMO_RAIN_HEAVY:
            case WMO_FREEZING_RAIN_LIGHT:
            case WMO_FREEZING_RAIN_HEAVY:
                score = SCORE_RAIN;
                break;
            case WMO_SNOW_SLIGHT:
            case WMO_SNOW_MODERATE:
            case WMO_SNOW_HEAVY:
            case WMO_SNOW_GRAINS:
                score = SCORE_SNOW;
                break;
            case WMO_SHOWERS_SLIGHT:
            case WMO_SHOWERS_MODERATE:
            case WMO_SHOWERS_VIOLENT:
            case WMO_SNOW_SHOWERS_SLIGHT:
            case WMO_SNOW_SHOWERS_HEAVY:
                score = SCORE_SHOWERS;
                break;
            case WMO_THUNDERSTORM:
            case WMO_THUNDERSTORM_SLIGHT_HAIL:
            case WMO_THUNDERSTORM_HEAVY_HAIL:
                score = SCORE_THUNDERSTORM;
                break;
            default:
                // Unrecognized WMO code: neutral fallback, never crash on an unmapped code.
                score = SCORE_UNRECOGNIZED_CODE;
                break;
        }
        return score;
    }
}
