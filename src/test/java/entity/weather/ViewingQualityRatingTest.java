package entity.weather;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exercises {@link ViewingQualityRating} directly against fake {@link WeatherCondition} values
 * (no network calls), covering every anchor segment of the piecewise-linear scoring curves,
 * every distinct weather-code score group (including the unmapped-code fallback), and every
 * branch of the score-to-rating threshold ladder.
 */
class ViewingQualityRatingTest {

    private static final double DELTA = 1e-9;

    // Holding visibility at its top anchor, precipitation at its bottom anchor, and weather code
    // 0 pins those three contributions at a known 100 each, so each case below isolates exactly
    // one cloud-cover interpolation branch.
    private static final double NEUTRAL_VISIBILITY = 20_000;
    private static final double NEUTRAL_PRECIPITATION = 0;
    private static final int NEUTRAL_WEATHER_CODE = 0;

    @Test
    void clampsCloudCoverBelowTheLowestAnchorToTheBestScore() {
        final double overallScore = scoreWithCloudCover(-10.0);

        // cloudCoverScore(100)*0.35 + weatherCodeScore(100)*0.30 + visibilityScore(100)*0.15
        // + precipitationScore(100)*0.20 = 100
        assertEquals(100.0, overallScore, DELTA);
    }

    @Test
    void clampsCloudCoverAboveTheHighestAnchorToTheWorstScore() {
        final WeatherCondition condition = new WeatherCondition(150.0, 0.0, 100.0, 99);

        final double overallScore = ViewingQualityRating.calculateOverallScore(condition);

        // Every component is pinned to its worst score here: cloud cover above range (0),
        // visibility at/below its lowest anchor (0), precipitation at/above its highest anchor
        // (0), and weather code 99 (the last thunderstorm group, score 0).
        assertEquals(0.0, overallScore, DELTA);
    }

    @Test
    void interpolatesCloudCoverBetweenTheFirstTwoAnchors() {
        // anchors 0 -> 10, scores 100 -> 90, at the right edge (fraction = 1.0)
        assertEquals(65 + 90 * 0.35, scoreWithCloudCover(10.0), DELTA);
    }

    @Test
    void interpolatesCloudCoverBetweenTheSecondPairOfAnchors() {
        // anchors 10 -> 30, scores 90 -> 65, halfway (fraction = 0.5) -> 77.5
        assertEquals(65 + 77.5 * 0.35, scoreWithCloudCover(20.0), DELTA);
    }

    @Test
    void interpolatesCloudCoverBetweenTheThirdPairOfAnchors() {
        // anchors 30 -> 60, scores 65 -> 40, halfway (fraction = 0.5) -> 52.5
        assertEquals(65 + 52.5 * 0.35, scoreWithCloudCover(45.0), DELTA);
    }

    @Test
    void interpolatesCloudCoverBetweenTheLastPairOfAnchors() {
        // anchors 60 -> 100, scores 40 -> 0, halfway (fraction = 0.5) -> 20
        assertEquals(65 + 20 * 0.35, scoreWithCloudCover(80.0), DELTA);
    }

    @Test
    void computesARealisticMixedConditionScore() {
        // Cloud cover strictly inside its last segment, visibility strictly inside a middle
        // segment, precipitation strictly inside a middle segment, and a "mostly clear" code.
        final WeatherCondition condition = new WeatherCondition(70.0, 7_500.0, 20.0, 1);

        final double overallScore = ViewingQualityRating.calculateOverallScore(condition);

        // cloudCoverScore: anchors 60->100 scores 40->0, fraction 0.25 -> 30
        // visibilityScore: anchors 5000->10000 scores 45->75, fraction 0.5 -> 60
        // precipitationScore: anchors 10->30 scores 85->55, fraction 0.5 -> 70
        // weatherCodeScore: code 1 -> 90
        final double expected = 30 * 0.35 + 90 * 0.30 + 60 * 0.15 + 70 * 0.20;
        assertEquals(expected, overallScore, DELTA);
    }

    @Test
    void scoresEveryDistinctWeatherCodeGroupAndFallsBackForUnknownCodes() {
        // Holding cloud cover, visibility, and precipitation at values that each score 100
        // isolates the weather-code contribution: overallScore = weatherCodeScore*0.30 + 70.
        final List<int[]> codesAndExpectedScores = List.of(
                new int[]{0, 100},
                new int[]{1, 90},
                new int[]{2, 65},
                new int[]{3, 40},
                new int[]{45, 20},
                new int[]{48, 20},
                new int[]{51, 20},
                new int[]{53, 20},
                new int[]{55, 20},
                new int[]{56, 20},
                new int[]{57, 20},
                new int[]{61, 10},
                new int[]{63, 10},
                new int[]{65, 10},
                new int[]{66, 10},
                new int[]{67, 10},
                new int[]{71, 10},
                new int[]{73, 10},
                new int[]{75, 10},
                new int[]{77, 10},
                new int[]{80, 5},
                new int[]{81, 5},
                new int[]{82, 5},
                new int[]{85, 5},
                new int[]{86, 5},
                new int[]{95, 0},
                new int[]{96, 0},
                new int[]{99, 0},
                new int[]{7, 50});

        for (final int[] codeAndExpectedScore : codesAndExpectedScores) {
            final int weatherCode = codeAndExpectedScore[0];
            final double expectedWeatherCodeScore = codeAndExpectedScore[1];

            final WeatherCondition condition = new WeatherCondition(
                    0.0, NEUTRAL_VISIBILITY, NEUTRAL_PRECIPITATION, weatherCode);

            final double expectedOverallScore = expectedWeatherCodeScore * 0.30 + 70;
            assertEquals(
                    expectedOverallScore,
                    ViewingQualityRating.calculateOverallScore(condition),
                    DELTA,
                    "weather code " + weatherCode);
        }
    }

    @Test
    void fromScoreReturnsExcellentAtAndAboveItsThreshold() {
        assertEquals(ViewingQualityRating.EXCELLENT, ViewingQualityRating.fromScore(75.0));
        assertEquals(ViewingQualityRating.EXCELLENT, ViewingQualityRating.fromScore(100.0));
    }

    @Test
    void fromScoreReturnsGoodJustBelowTheExcellentThreshold() {
        assertEquals(ViewingQualityRating.GOOD, ViewingQualityRating.fromScore(74.999));
    }

    @Test
    void fromScoreReturnsGoodAtItsThreshold() {
        assertEquals(ViewingQualityRating.GOOD, ViewingQualityRating.fromScore(50.0));
    }

    @Test
    void fromScoreReturnsFairJustBelowTheGoodThreshold() {
        assertEquals(ViewingQualityRating.FAIR, ViewingQualityRating.fromScore(49.999));
    }

    @Test
    void fromScoreReturnsFairAtItsThreshold() {
        assertEquals(ViewingQualityRating.FAIR, ViewingQualityRating.fromScore(25.0));
    }

    @Test
    void fromScoreReturnsPoorJustBelowTheFairThreshold() {
        assertEquals(ViewingQualityRating.POOR, ViewingQualityRating.fromScore(24.999));
    }

    @Test
    void fromScoreReturnsPoorAtTheBottomOfTheScale() {
        assertEquals(ViewingQualityRating.POOR, ViewingQualityRating.fromScore(0.0));
    }

    @Test
    void calculateRatingDelegatesToCalculateOverallScoreAndFromScore() {
        // Every component pinned to 100 (see clampsCloudCoverBelowTheLowestAnchorToTheBestScore)
        // yields an overall score of 100, which fromScore buckets as EXCELLENT.
        final WeatherCondition condition = new WeatherCondition(
                -10.0, NEUTRAL_VISIBILITY, NEUTRAL_PRECIPITATION, NEUTRAL_WEATHER_CODE);

        assertEquals(ViewingQualityRating.EXCELLENT, ViewingQualityRating.calculateRating(condition));
    }

    private static double scoreWithCloudCover(final double cloudCoverPercent) {
        final WeatherCondition condition = new WeatherCondition(
                cloudCoverPercent, NEUTRAL_VISIBILITY, NEUTRAL_PRECIPITATION, NEUTRAL_WEATHER_CODE);
        return ViewingQualityRating.calculateOverallScore(condition);
    }
}
