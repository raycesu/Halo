package interface_adapter.view_sky;

import java.util.ArrayList;
import java.util.List;

import entity.Star;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel.RankedDayDisplayItem;
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
    private CheckConditionsViewModel checkConditionsViewModel;
    private RankForecastDaysViewModel rankForecastDaysViewModel;
    private ViewSkyPresenter presenter;

    @BeforeEach
    void setUp() {
        skyViewModel = new SkyViewModel();
        setupViewModel = new ObservationSetupViewModel();
        checkConditionsViewModel = new CheckConditionsViewModel();
        rankForecastDaysViewModel = new RankForecastDaysViewModel();
        presenter = new ViewSkyPresenter(
                skyViewModel, setupViewModel, checkConditionsViewModel, rankForecastDaysViewModel);
    }

    @Test
    void presentsSuccessfulSkyAndClearsStaleState() {
        final Star star = testStar();
        final List<Star> stars = new ArrayList<>(List.of(star));
        final StarDisplayData expectedDisplay = toDisplayData(star);
        skyViewModel.setSelectedObject(expectedDisplay);
        skyViewModel.setSelectedObjectDetails("old details");
        skyViewModel.setWarningMessage("old warning");
        skyViewModel.setErrorMessage("old error");
        setupViewModel.setErrorMessage("old setup error");
        checkConditionsViewModel.setCloudCoverText("40% cloud cover");
        checkConditionsViewModel.setRatingText("Rating: Good");
        checkConditionsViewModel.setRatingColor("#66BB6A");
        checkConditionsViewModel.setErrorMessage("old weather error");
        rankForecastDaysViewModel.setRankedDays(List.of(new RankedDayDisplayItem(
                1, "Mon, Jan 1", "Rating: Good", "#66BB6A", "Score: 80/100")));
        rankForecastDaysViewModel.setErrorMessage("old ranking error");

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
        assertEquals(1, skyViewModel.getStars().size());
        assertEquals("HIP 32349", skyViewModel.getStars().get(0).getCatalogueId());
        assertEquals("Sirius", skyViewModel.getStars().get(0).getDisplayName());
        assertThrows(UnsupportedOperationException.class,
                () -> skyViewModel.getStars().clear());
        assertNull(skyViewModel.getSelectedObject());
        assertTrue(skyViewModel.getSelectedObjectName().isEmpty());
        assertTrue(skyViewModel.getSelectedObjectDetails().isEmpty());
        assertTrue(skyViewModel.getWarningMessage().isEmpty());
        assertTrue(skyViewModel.getErrorMessage().isEmpty());
        assertTrue(setupViewModel.getErrorMessage().isEmpty());

        assertTrue(checkConditionsViewModel.getCloudCoverText().isEmpty());
        assertTrue(checkConditionsViewModel.getRatingText().isEmpty());
        assertTrue(checkConditionsViewModel.getRatingColor().isEmpty());
        assertTrue(checkConditionsViewModel.getErrorMessage().isEmpty());
        assertTrue(rankForecastDaysViewModel.getRankedDays().isEmpty());
        assertTrue(rankForecastDaysViewModel.getErrorMessage().isEmpty());
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
        assertEquals(1, skyViewModel.getStars().size());
        assertEquals("HIP 32349", skyViewModel.getStars().get(0).getCatalogueId());
        assertEquals("Toronto", skyViewModel.getDisplayedLocation());
    }

    private Star testStar() {
        return new Star.Builder()
                .catalogueId("HIP 32349")
                .displayName("Sirius")
                .rightAscension(6.7525)
                .declination(-16.7161)
                .apparentMagnitude(-1.46)
                .constellationRegion("Canis Major")
                .spectralType("A1V")
                .description("The brightest star in the night sky.")
                .build();
    }

    private static StarDisplayData toDisplayData(final Star star) {
        return new StarDisplayData(
                star.getCatalogueId(),
                star.getDisplayName(),
                star.getRightAscension(),
                star.getDeclination(),
                star.getApparentMagnitude(),
                star.getConstellationRegion(),
                star.getSpectralType(),
                star.getDescription(),
                star.getType().name(),
                star.getAltitude(),
                star.getAzimuth(),
                star.isAboveHorizon());
    }
}
