package interface_adapter.view_sky;

import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entity.ObserverLocation;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInputData;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewSkyControllerTest {

    private static final double COORDINATE_TOLERANCE = 1.0e-9;

    private RecordingInteractor interactor;
    private RecordingOutputBoundary outputBoundary;
    private ViewSkyController controller;

    @BeforeEach
    void setUp() {
        interactor = new RecordingInteractor();
        outputBoundary = new RecordingOutputBoundary();
        controller = new ViewSkyController(interactor, outputBoundary);
    }

    @Test
    void passesAParsedRequestToTheInteractor() {
        final ObserverLocation toronto = new ObserverLocation(
                "Toronto, Ontario, Canada", 43.7064, -79.3986, ZoneId.of("America/Toronto"));

        controller.viewSky(toronto, "2026-07-30", "23:15");

        assertNotNull(interactor.received, "The interactor should have been called.");
        assertEquals("Toronto, Ontario, Canada", interactor.received.getLocationName());
        assertEquals(43.7064, interactor.received.getLatitude(), COORDINATE_TOLERANCE);
        assertEquals(-79.3986, interactor.received.getLongitude(), COORDINATE_TOLERANCE);
        assertEquals(ZoneId.of("America/Toronto"), interactor.received.getZoneId());
        assertEquals("2026-07-30", interactor.received.getDate());
        assertEquals("23:15", interactor.received.getTime());
        assertNull(outputBoundary.errorMessage, "A valid request should not report an error.");
    }

    /**
     * The reason the autocomplete hands back a location rather than a string: without one there is
     * nothing to geocode against, and guessing would query the wrong place silently.
     */
    @Test
    void refusesToRunWithoutAResolvedLocation() {
        controller.viewSky(null, "2026-07-30", "23:15");

        assertNull(interactor.received, "The use case should never run without a location.");
        assertNotNull(outputBoundary.errorMessage);
        assertTrue(outputBoundary.errorMessage.toLowerCase().contains("location"));
    }

    @Test
    void rejectsAnUnparseableDateBeforeReachingTheUseCase() {
        controller.viewSky(anyLocation(), "30-07-2026", "23:15");

        assertNull(interactor.received);
        assertNotNull(outputBoundary.errorMessage);
    }

    @Test
    void rejectsAnUnparseableTimeBeforeReachingTheUseCase() {
        controller.viewSky(anyLocation(), "2026-07-30", "11:15pm");

        assertNull(interactor.received);
        assertNotNull(outputBoundary.errorMessage);
    }

    @Test
    void rejectsMissingDateAndTime() {
        controller.viewSky(anyLocation(), null, null);

        assertNull(interactor.received);
        assertNotNull(outputBoundary.errorMessage);
    }

    private ObserverLocation anyLocation() {
        return new ObserverLocation("Toronto", 43.7064, -79.3986, ZoneId.of("America/Toronto"));
    }

    private static final class RecordingInteractor implements ViewSkyInputBoundary {

        private ViewSkyInputData received;

        @Override
        public void execute(final ViewSkyInputData inputData) {
            received = inputData;
        }
    }

    private static final class RecordingOutputBoundary implements ViewSkyOutputBoundary {

        private String errorMessage;

        @Override
        public void prepareSuccessView(final ViewSkyOutputData outputData) {
            // Not exercised here: the controller never presents a result itself.
        }

        @Override
        public void prepareFailView(final String message) {
            errorMessage = message;
        }

        @Override
        public void prepareWarning(final String message) {
            // Not exercised here: warnings only come from the use case.
        }
    }
}
