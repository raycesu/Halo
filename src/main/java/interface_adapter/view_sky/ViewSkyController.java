package interface_adapter.view_sky;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import entity.ObserverLocation;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInputData;
import use_case.view_sky.ViewSkyOutputBoundary;

/**
 * Turns what the user typed into the parsed values the sky use case works in.
 *
 * <p>Everything that can go wrong with raw text is settled here: an unparseable date, a time in
 * the wrong shape, a place that was never resolved to coordinates. The interactor is then free to
 * assume it has a real location and a real instant, which is why {@link ViewSkyInputData} takes
 * typed values rather than strings.
 *
 * <p>Rejections go out through the same {@link ViewSkyOutputBoundary#prepareFailView} the use case
 * uses, so the view has one place to read an error from regardless of how far the request got.
 */
public class ViewSkyController {

    private final ViewSkyInputBoundary inputBoundary;
    private final ViewSkyOutputBoundary outputBoundary;

    public ViewSkyController(
            final ViewSkyInputBoundary inputBoundary,
            final ViewSkyOutputBoundary outputBoundary) {
        this.inputBoundary = inputBoundary;
        this.outputBoundary = outputBoundary;
    }

    /**
     * Requests the sky at a resolved location and a typed date and time.
     *
     * @param location the place the user picked, already resolved to coordinates and a zone
     * @param date the observation date, ISO {@code yyyy-MM-dd}
     * @param time the observation time, ISO {@code HH:mm}
     */
    public void viewSky(final ObserverLocation location, final String date, final String time) {
        if (location == null) {
            outputBoundary.prepareFailView("Choose a location from the suggestions first.");
            return;
        }

        if (date == null || time == null) {
            outputBoundary.prepareFailView("Enter a date (yyyy-MM-dd) and a time (HH:mm).");
            return;
        }

        final LocalDateTime observationDateTime;
        try {
            observationDateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
        }
        catch (DateTimeParseException exception) {
            outputBoundary.prepareFailView("Enter a valid date (yyyy-MM-dd) and time (HH:mm).");
            return;
        }

        inputBoundary.execute(new ViewSkyInputData(
                location.getDisplayName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getZoneId(),
                observationDateTime));
    }
}
