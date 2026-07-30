package interface_adapter.view_sky;

import java.util.ArrayList;
import java.util.List;

import entity.Star;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.view_sky.ViewSkyOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewSkyPresenterTest {

    private SkyViewModel skyViewModel;
    private ObservationSetupViewModel setupViewModel;
    private ViewSkyPresenter presenter;

    @BeforeEach
    void setUp() {
        skyViewModel = new SkyViewModel();
        setupViewModel = new ObservationSetupViewModel();
        presenter = new ViewSkyPresenter(skyViewModel, setupViewModel);
    }

    @Test
    void presentsSuccessfulSkyAndClearsStaleState() {
        final Star star = testStar();
        final List<Star> stars = new ArrayList<>(List.of(star));
        skyViewModel.setSelectedObject(star);
        skyViewModel.setSelectedObjectDetails("old details");
        skyViewModel.setWarningMessage("old warning");
        skyViewModel.setErrorMessage("old error");
        setupViewModel.setErrorMessage("old setup error");

        presenter.prepareSuccessView(new ViewSkyOutputData(
                "Toronto",
                "2026-07-24",
                "18:20",
                43.6532,
                -79.3832,
                stars));
        stars.clear();

        assertEquals("Toronto", skyViewModel.getDisplayedLocation());
        assertEquals("2026-07-24", skyViewModel.getDisplayedDate());
        assertEquals("18:20", skyViewModel.getDisplayedTime());
        assertEquals(43.6532, skyViewModel.getLatitude(), 1e-9);
        assertEquals(-79.3832, skyViewModel.getLongitude(), 1e-9);
        assertEquals(List.of(star), skyViewModel.getStars());
        assertThrows(UnsupportedOperationException.class,
                () -> skyViewModel.getStars().clear());
        assertNull(skyViewModel.getSelectedObject());
        assertTrue(skyViewModel.getSelectedObjectName().isEmpty());
        assertTrue(skyViewModel.getSelectedObjectDetails().isEmpty());
        assertTrue(skyViewModel.getWarningMessage().isEmpty());
        assertTrue(skyViewModel.getErrorMessage().isEmpty());
        assertTrue(setupViewModel.getErrorMessage().isEmpty());
    }

    @Test
    void presentsFailureOnTheSetupViewModel() {
        presenter.prepareFailView("The observation details are invalid.");

        assertEquals(
                "The observation details are invalid.",
                setupViewModel.getErrorMessage());
        assertTrue(skyViewModel.getErrorMessage().isEmpty());
    }

    @Test
    void presentsWarningWithoutRemovingTheSuccessfulSky() {
        final Star star = testStar();
        presenter.prepareSuccessView(new ViewSkyOutputData(
                "Toronto",
                "2026-07-24",
                "18:20",
                43.6532,
                -79.3832,
                List.of(star)));

        presenter.prepareWarning("Showing catalogue stars only.");

        assertEquals("Showing catalogue stars only.", skyViewModel.getWarningMessage());
        assertEquals(List.of(star), skyViewModel.getStars());
        assertEquals("Toronto", skyViewModel.getDisplayedLocation());
    }

    private Star testStar() {
        return new Star(
                "HIP 32349",
                "Sirius",
                6.7525,
                -16.7161,
                -1.46,
                "Canis Major",
                "A1V",
                "The brightest star in the night sky.");
    }
}
