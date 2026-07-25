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

    /**
     * Computes the 0-100 overall viewing-quality score for the given weather condition.
     * Exposed separately from {@link #calculateRating(WeatherCondition)} because future
     * features (e.g. ranking multiple nights) need the raw number, not just a bucketed rating.
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
     */
    public static ViewingQualityRating calculateRating(final WeatherCondition condition) {
        return fromScore(calculateOverallScore(condition));
    }

    /**
     * Maps a 0-100 overall score to a rating category using fixed rubric thresholds
     * (not statistical percentiles).
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
            case 0:
                score = 100;
                break;
            case 1:
                score = 90;
                break;
            case 2:
                score = 65;
                break;
            case 3:
                score = 40;
                break;
            case 45:
            case 48:
                score = 20;
                break;
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                score = 20;
                break;
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
                score = 10;
                break;
            case 71:
            case 73:
            case 75:
            case 77:
                score = 10;
                break;
            case 80:
            case 81:
            case 82:
            case 85:
            case 86:
                score = 5;
                break;
            case 95:
            case 96:
            case 99:
                score = 0;
                break;
            default:
                // Unrecognized WMO code: neutral fallback, never crash on an unmapped code.
                score = 50;
                break;
        }
        return score;
    }
}
