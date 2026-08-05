package use_case.rank_forecast_days;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankForecastDaysOutputDataTest {

    @Test
    void treatsANullRankedDayListAsEmpty() {
        final RankForecastDaysOutputData outputData = new RankForecastDaysOutputData(null);

        assertTrue(outputData.getRankedDays().isEmpty());
    }

    @Test
    void copiesTheSuppliedListSoLaterMutationDoesNotLeak() {
        final List<RankedDayResult> rankedDays = new ArrayList<>(List.of(sampleResult()));

        final RankForecastDaysOutputData outputData = new RankForecastDaysOutputData(rankedDays);
        rankedDays.clear();

        assertEquals(1, outputData.getRankedDays().size());
    }

    @Test
    void returnsAnUnmodifiableList() {
        final RankForecastDaysOutputData outputData =
                new RankForecastDaysOutputData(List.of(sampleResult()));

        assertThrows(
                UnsupportedOperationException.class,
                () -> outputData.getRankedDays().clear());
    }

    private RankedDayResult sampleResult() {
        return new RankedDayResult(
                LocalDate.of(2026, 7, 30),
                new WeatherCondition(10.0, 15_000.0, 5.0, 0),
                90.0,
                ViewingQualityRating.EXCELLENT,
                1);
    }
}
