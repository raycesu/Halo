package interface_adapter.view_sky;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInputData;
import use_case.view_sky.ViewSkyOutputBoundary;

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
     * @param locationName the display name of the location, or null if none was picked
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param zoneId the IANA time zone ID string
     * @param date the observation date, ISO {@code yyyy-MM-dd}
     * @param time the observation time, ISO {@code HH:mm}
     */
    public void viewSky(
            final String locationName,
            final double latitude,
            final double longitude,
            final String zoneId,
            final String date,
            final String time) {
        if (locationName == null || locationName.isBlank()) {
            outputBoundary.prepareFailView("Choose a location from the suggestions first.");
        }
        else if (date == null || time == null) {
            outputBoundary.prepareFailView("Enter a valid date (yyyy-MM-dd) and time (HH:mm).");
        }
        else {
            try {
                inputBoundary.execute(new ViewSkyInputData(
                        locationName,
                        latitude,
                        longitude,
                        ZoneId.of(zoneId),
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
