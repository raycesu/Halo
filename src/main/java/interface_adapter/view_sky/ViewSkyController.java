package interface_adapter.view_sky;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import entity.ObserverLocation;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInputData;
import use_case.view_sky.ViewSkyOutputBoundary;

/**
 * Turns what the user chose and typed into the values the sky use case works in.
 *
 * <p>The place arrives already resolved, as an {@link ObserverLocation} picked from the suggestion
 * list rather than as coordinates typed by hand. That is deliberate: latitude, longitude and time
 * zone are not things a person can be expected to know, and a mistyped one is accepted silently by
 * every service downstream. Only the date and time are still parsed here.
 *
 * <p>Rejections go out through {@link ViewSkyOutputBoundary#prepareFailView}, the same path the
 * use case uses, so the view reads errors from one place regardless of how far a request got.
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
     * Requests the sky at a resolved place and a typed date and time.
     *
     * @param location the place the user picked; null if they never picked one
     * @param date the observation date, ISO {@code yyyy-MM-dd}
     * @param time the observation time, ISO {@code HH:mm}
     */
    public void viewSky(final ObserverLocation location, final String date, final String time) {
        if (location == null) {
            outputBoundary.prepareFailView("Choose a location from the suggestions first.");
        }
        else if (date == null || time == null) {
            outputBoundary.prepareFailView("Enter a valid date (yyyy-MM-dd) and time (HH:mm).");
        }
        else {
            try {
                inputBoundary.execute(new ViewSkyInputData(
                        location.getDisplayName(),
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getZoneId(),
                        LocalDateTime.of(
                                LocalDate.parse(date.trim()),
                                LocalTime.parse(time.trim()))));
            }
            catch (DateTimeException exception) {
                outputBoundary.prepareFailView("Enter a valid date (yyyy-MM-dd) and time (HH:mm).");
            }
        }
    }
}
