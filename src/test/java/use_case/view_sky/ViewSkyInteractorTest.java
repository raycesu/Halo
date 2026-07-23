package use_case.view_sky;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewSkyInteractorTest {

    @Test
    void executeGetsDemoStarsAndPassesOutputToPresenter() {
        final FakeStarCatalogDataAccess fakeDataAccess = new FakeStarCatalogDataAccess();
        final FakeViewSkyOutputBoundary fakeOutputBoundary = new FakeViewSkyOutputBoundary();
        final ViewSkyInteractor interactor =
                new ViewSkyInteractor(fakeDataAccess, fakeOutputBoundary);
        final ViewSkyInputData inputData =
                new ViewSkyInputData("Toronto", "2026-07-24", "18:20");

        interactor.execute(inputData);

        assertTrue(fakeDataAccess.wasCalled);
        assertTrue(fakeOutputBoundary.wasCalled);
        assertEquals("Toronto", fakeOutputBoundary.outputData.getLocation());
        assertEquals("2026-07-24", fakeOutputBoundary.outputData.getDate());
        assertEquals("18:20", fakeOutputBoundary.outputData.getTime());
        assertEquals(
                List.of("Sirius", "Vega", "Polaris"),
                fakeOutputBoundary.outputData.getDemoStarNames());
    }

    private static class FakeStarCatalogDataAccess
            implements StarCatalogDataAccessInterface {

        private boolean wasCalled;

        @Override
        public List<String> getDemoStarNames() {
            wasCalled = true;
            return List.of("Sirius", "Vega", "Polaris");
        }
    }

    private static class FakeViewSkyOutputBoundary implements ViewSkyOutputBoundary {

        private boolean wasCalled;
        private ViewSkyOutputData outputData;

        @Override
        public void prepareSuccessView(final ViewSkyOutputData outputData) {
            wasCalled = true;
            this.outputData = outputData;
        }
    }
}
