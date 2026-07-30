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

    public void viewSky(
            final String locationName,
            final String latitude,
            final String longitude,
            final String zoneId,
            final String date,
            final String time) {
        try {
            final ViewSkyInputData inputData = new ViewSkyInputData(
                    locationName.trim(),
                    Double.parseDouble(latitude.trim()),
                    Double.parseDouble(longitude.trim()),
                    ZoneId.of(zoneId.trim()),
                    LocalDateTime.of(
                            LocalDate.parse(date.trim()),
                            LocalTime.parse(time.trim())));

            inputBoundary.execute(inputData);
        }
        catch (NumberFormatException | DateTimeException | NullPointerException exception) {
            outputBoundary.prepareFailView(
                    "Enter valid latitude, longitude, time zone, date, and time.");
        }
    }
}
