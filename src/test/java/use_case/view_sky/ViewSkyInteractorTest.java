package use_case.view_sky;

import java.util.List;

import entity.Star;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewSkyInteractorTest {

    @Test
    void executeGetsStarsAndPassesOutputToPresenter() {
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
        assertEquals(3, fakeOutputBoundary.outputData.getStars().size());
        assertEquals("Sirius",
                fakeOutputBoundary.outputData.getStars().get(0).getDisplayName());
        assertEquals("Vega",
                fakeOutputBoundary.outputData.getStars().get(1).getDisplayName());
        assertEquals("Polaris",
                fakeOutputBoundary.outputData.getStars().get(2).getDisplayName());
    }

    private static class FakeStarCatalogDataAccess
            implements StarCatalogDataAccessInterface {

        private boolean wasCalled;

        @Override
        public List<Star> findAll() {
            wasCalled = true;
            return List.of(
                    new Star("HR2491", "Sirius", 6.75247222, -16.71611111,
                            -1.46, "CMa", "A1Vm", ""),
                    new Star("HR7001", "Vega", 18.61561111, 38.78361111,
                            0.03, "Lyr", "A0Va", ""),
                    new Star("HR424", "Polaris", 2.53069444, 89.26416667,
                            2.02, "UMi", "F7:Ib-IIv", ""));
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
