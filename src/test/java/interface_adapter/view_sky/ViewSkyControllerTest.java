package interface_adapter.view_sky;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInputData;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewSkyControllerTest {

    private static final double COORDINATE_TOLERANCE = 1.0e-9;
    private static final String TORONTO_NAME = "Toronto, Ontario, Canada";
    private static final double TORONTO_LAT = 43.6532;
    private static final double TORONTO_LNG = -79.3832;
    private static final String TORONTO_ZONE = "America/Toronto";

    private FakeInputBoundary inputBoundary;
    private FakeOutputBoundary outputBoundary;
    private ViewSkyController controller;

    @BeforeEach
    void setUp() {
        inputBoundary = new FakeInputBoundary();
        outputBoundary = new FakeOutputBoundary();
        controller = new ViewSkyController(inputBoundary, outputBoundary);
    }

    @Test
    void passesTheResolvedPlaceAndParsedTimeToTheInteractor() {
        controller.viewSky(TORONTO_NAME, TORONTO_LAT, TORONTO_LNG, TORONTO_ZONE,
                "2026-07-24", "18:20");

        assertNotNull(inputBoundary.inputData);
        assertEquals("Toronto, Ontario, Canada", inputBoundary.inputData.getLocationName());
        assertEquals(43.6532, inputBoundary.inputData.getLatitude(), COORDINATE_TOLERANCE);
        assertEquals(-79.3832, inputBoundary.inputData.getLongitude(), COORDINATE_TOLERANCE);
        assertEquals("America/Toronto", inputBoundary.inputData.getZoneId().getId());
        assertEquals("2026-07-24T18:20",
                inputBoundary.inputData.getObservationDateTime().toString());
        assertFalse(outputBoundary.failCalled);
    }

    @Test
    void toleratesSurroundingSpaceInTheDateAndTime() {
        controller.viewSky(TORONTO_NAME, TORONTO_LAT, TORONTO_LNG, TORONTO_ZONE,
                "  2026-07-24 ", " 18:20  ");

        assertNotNull(inputBoundary.inputData);
        assertFalse(outputBoundary.failCalled);
    }

    @Test
    void refusesToRunWithoutAResolvedLocation() {
        controller.viewSky(null, TORONTO_LAT, TORONTO_LNG, TORONTO_ZONE,
                "2026-07-24", "18:20");

        assertNull(inputBoundary.inputData);
        assertTrue(outputBoundary.failCalled);
        assertTrue(outputBoundary.errorMessage.toLowerCase().contains("location"));
    }

    @Test
    void reportsAnUnparseableDateWithoutCallingTheInteractor() {
        controller.viewSky(TORONTO_NAME, TORONTO_LAT, TORONTO_LNG, TORONTO_ZONE,
                "24-07-2026", "18:20");

        assertNull(inputBoundary.inputData);
        assertTrue(outputBoundary.failCalled);
        assertEquals(
                "Enter a valid date (yyyy-MM-dd) and time (HH:mm).",
                outputBoundary.errorMessage);
    }

    @Test
    void reportsAnUnparseableTimeWithoutCallingTheInteractor() {
        controller.viewSky(TORONTO_NAME, TORONTO_LAT, TORONTO_LNG, TORONTO_ZONE,
                "2026-07-24", "6:20pm");

        assertNull(inputBoundary.inputData);
        assertTrue(outputBoundary.failCalled);
    }

    @Test
    void reportsMissingDateAndTimeWithoutCallingTheInteractor() {
        controller.viewSky(TORONTO_NAME, TORONTO_LAT, TORONTO_LNG, TORONTO_ZONE,
                null, null);

        assertNull(inputBoundary.inputData);
        assertTrue(outputBoundary.failCalled);
    }

    private static class FakeInputBoundary implements ViewSkyInputBoundary {

        private ViewSkyInputData inputData;

        @Override
        public void execute(final ViewSkyInputData inputData) {
            this.inputData = inputData;
        }
    }

    private static class FakeOutputBoundary implements ViewSkyOutputBoundary {

        private boolean failCalled;
        private String errorMessage;

        @Override
        public void prepareSuccessView(final ViewSkyOutputData outputData) {
        }

        @Override
        public void prepareFailView(final String errorMessage) {
            failCalled = true;
            this.errorMessage = errorMessage;
        }

        @Override
        public void prepareWarning(final String warningMessage) {
        }
    }
}
