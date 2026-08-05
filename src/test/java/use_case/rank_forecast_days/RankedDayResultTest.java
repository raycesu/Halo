package use_case.rank_forecast_days;

import java.time.LocalDate;

import entity.weather.ViewingQualityRating;
import entity.weather.WeatherCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RankedDayResultTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final LocalDate date = LocalDate.of(2026, 7, 30);
        final WeatherCondition condition = new WeatherCondition(10.0, 15_000.0, 5.0, 0);

        final RankedDayResult result =
                new RankedDayResult(date, condition, 92.5, ViewingQualityRating.EXCELLENT, 1);

        assertEquals(date, result.getDate());
        assertSame(condition, result.getCondition());
        assertEquals(92.5, result.getOverallScore(), 1e-9);
        assertEquals(ViewingQualityRating.EXCELLENT, result.getRating());
        assertEquals(1, result.getRank());
    }
}
