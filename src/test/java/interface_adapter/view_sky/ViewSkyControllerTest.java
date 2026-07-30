package interface_adapter.view_sky;

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

    @Test
    void parsesSetupTextAndCallsTheInteractor() {
        final FakeInputBoundary inputBoundary = new FakeInputBoundary();
        final FakeOutputBoundary outputBoundary = new FakeOutputBoundary();
        final ViewSkyController controller =
                new ViewSkyController(inputBoundary, outputBoundary);

        controller.viewSky(
                "Toronto",
                "43.6532",
                "-79.3832",
                "America/Toronto",
                "2026-07-24",
                "18:20");

        assertNotNull(inputBoundary.inputData);
        assertEquals("Toronto", inputBoundary.inputData.getLocationName());
        assertEquals(43.6532, inputBoundary.inputData.getLatitude(), 1e-9);
        assertEquals(-79.3832, inputBoundary.inputData.getLongitude(), 1e-9);
        assertEquals("America/Toronto", inputBoundary.inputData.getZoneId().getId());
        assertEquals("2026-07-24T18:20",
                inputBoundary.inputData.getObservationDateTime().toString());
        assertFalse(outputBoundary.failCalled);
    }

    @Test
    void reportsMalformedInputWithoutCallingTheInteractor() {
        final FakeInputBoundary inputBoundary = new FakeInputBoundary();
        final FakeOutputBoundary outputBoundary = new FakeOutputBoundary();
        final ViewSkyController controller =
                new ViewSkyController(inputBoundary, outputBoundary);

        controller.viewSky(
                "Toronto",
                "not a latitude",
                "-79.3832",
                "America/Toronto",
                "2026-07-24",
                "18:20");

        assertNull(inputBoundary.inputData);
        assertTrue(outputBoundary.failCalled);
        assertEquals(
                "Enter valid latitude, longitude, time zone, date, and time.",
                outputBoundary.errorMessage);
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
