package use_case.rank_forecast_days;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankForecastDaysInputDataTest {

    @Test
    void exposesLatitudeAndLongitude() {
        final RankForecastDaysInputData inputData = new RankForecastDaysInputData(
                43.6532, -79.3832, List.of(LocalDate.of(2026, 7, 30)));

        assertEquals(43.6532, inputData.getLatitude(), 1e-9);
        assertEquals(-79.3832, inputData.getLongitude(), 1e-9);
    }

    @Test
    void treatsANullDateListAsEmpty() {
        final RankForecastDaysInputData inputData =
                new RankForecastDaysInputData(0.0, 0.0, null);

        assertTrue(inputData.getSelectedDates().isEmpty());
    }

    @Test
    void copiesTheSuppliedDatesSoLaterMutationDoesNotLeak() {
        final List<LocalDate> dates = new ArrayList<>(
                List.of(LocalDate.of(2026, 7, 30), LocalDate.of(2026, 7, 31)));

        final RankForecastDaysInputData inputData =
                new RankForecastDaysInputData(0.0, 0.0, dates);
        dates.clear();

        assertEquals(
                List.of(LocalDate.of(2026, 7, 30), LocalDate.of(2026, 7, 31)),
                inputData.getSelectedDates());
    }

    @Test
    void returnsAnUnmodifiableList() {
        final RankForecastDaysInputData inputData = new RankForecastDaysInputData(
                0.0, 0.0, List.of(LocalDate.of(2026, 7, 30)));

        assertThrows(
                UnsupportedOperationException.class,
                () -> inputData.getSelectedDates().clear());
    }
}
