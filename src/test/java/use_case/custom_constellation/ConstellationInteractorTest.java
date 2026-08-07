package use_case.custom_constellation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import data_access.InMemoryConstellationDataAccessObject;
import entity.CustomConstellation;
import entity.Star;

public class ConstellationInteractorTest {

    @Test
    public void createsAndSavesConstellation() {
        final InMemoryConstellationDataAccessObject dataAccess = new InMemoryConstellationDataAccessObject();
        final TestPresenter presenter = new TestPresenter();
        final ConstellationInteractor interactor = new ConstellationInteractor(dataAccess, presenter);
        final Star firstStar = createStar("1", "Vega");
        final Star secondStar = createStar("2", "Sirius");
        final Star thirdStar = createStar("3", "Polaris");
        interactor.execute(new ConstellationInputData("My Pattern", List.of(firstStar, secondStar, thirdStar)));assertTrue(presenter.success);
        assertEquals(1, dataAccess.findAll().size());
        final CustomConstellation saved = dataAccess.findAll().get(0);

        assertEquals("My Pattern", saved.getName());
        assertEquals(2, saved.getLines().size());
        assertEquals(firstStar, saved.getLines().get(0).getStartStar());
        assertEquals(secondStar, saved.getLines().get(0).getEndStar());
        assertEquals(secondStar, saved.getLines().get(1).getStartStar());
        assertEquals(thirdStar, saved.getLines().get(1).getEndStar());
    }

    private Star createStar(final String id, final String name) {
        return new Star.Builder()
                .catalogueId(id)
                .displayName(name)
                .apparentMagnitude(1.0)
                .build();
    }

    private static final class TestPresenter implements ConstellationOutputBoundary {
        private boolean success;
        private String errorMessage = "";

        @Override
        public void prepareSuccessView(final ConstellationOutputData outputData) {
            success = true;
        }

        @Override
        public void prepareFailureView(final String errorMessage) {
            success = false;
            this.errorMessage = errorMessage;
        }
    }
}