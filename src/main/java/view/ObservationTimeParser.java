package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Parses the currently displayed observation date/time text into a single instant. */
final class ObservationTimeParser {

    private ObservationTimeParser() {
    }

    /**
     * Parses the given date and time text into a single instant.
     *
     * @param dateText an ISO-8601 date (yyyy-MM-dd)
     * @param timeText a 24-hour time (HH:mm)
     * @return the parsed instant, or null if either string is not a valid date/time
     */
    static LocalDateTime parse(final String dateText, final String timeText) {
        LocalDateTime result = null;
        try {
            result = LocalDateTime.of(LocalDate.parse(dateText), LocalTime.parse(timeText));
        }
        catch (DateTimeParseException exception) {
            // result stays null; the caller reports this as an invalid date/time.
        }
        return result;
    }
}
