package use_case.rank_forecast_days;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import entity.ObserverLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankForecastDaysInputDataTest {

    private static ObserverLocation toronto() {
        return new ObserverLocation("Toronto", 43.6532, -79.3832, ZoneId.of("America/Toronto"));
    }

    @Test
    void exposesTheObservationLocation() {
        final ObserverLocation location = toronto();
        final RankForecastDaysInputData inputData = new RankForecastDaysInputData(
                location, List.of(LocalDate.of(2026, 7, 30)));

        assertSame(location, inputData.getLocation());
        assertEquals(43.6532, inputData.getLocation().getLatitude(), 1e-9);
        assertEquals(-79.3832, inputData.getLocation().getLongitude(), 1e-9);
    }

    @Test
    void treatsANullDateListAsEmpty() {
        final RankForecastDaysInputData inputData =
                new RankForecastDaysInputData(toronto(), null);

        assertTrue(inputData.getSelectedDates().isEmpty());
    }

    @Test
    void copiesTheSuppliedDatesSoLaterMutationDoesNotLeak() {
        final List<LocalDate> dates = new ArrayList<>(
                List.of(LocalDate.of(2026, 7, 30), LocalDate.of(2026, 7, 31)));

        final RankForecastDaysInputData inputData =
                new RankForecastDaysInputData(toronto(), dates);
        dates.clear();

        assertEquals(
                List.of(LocalDate.of(2026, 7, 30), LocalDate.of(2026, 7, 31)),
                inputData.getSelectedDates());
    }

    @Test
    void returnsAnUnmodifiableList() {
        final RankForecastDaysInputData inputData = new RankForecastDaysInputData(
                toronto(), List.of(LocalDate.of(2026, 7, 30)));

        assertThrows(
                UnsupportedOperationException.class,
                () -> inputData.getSelectedDates().clear());
    }
}
