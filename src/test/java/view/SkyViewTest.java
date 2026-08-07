package view;

import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import entity.Star;
import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysController;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.SkyViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.check_conditions.CheckConditionsInputBoundary;
import use_case.check_conditions.CheckConditionsInputData;
import use_case.rank_forecast_days.RankForecastDaysInputBoundary;
import use_case.rank_forecast_days.RankForecastDaysInputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyViewTest {

    private static final long TIMEOUT_SECONDS = 3;
    private static final double LATITUDE = 41.0082;
    private static final double LONGITUDE = 28.9784;

    private SkyViewModel skyViewModel;
    private ViewManagerModel viewManagerModel;
    private CapturingCheckConditions checkConditions;
    private CapturingRankForecastDays rankForecastDays;
    private SkyView view;
    private Star sirius;

    @BeforeEach
    void setUp() throws Exception {
        skyViewModel = new SkyViewModel();
        skyViewModel.setDisplayedLocation("Istanbul");
        skyViewModel.setDisplayedDate("2026-08-12");
        skyViewModel.setDisplayedTime("22:15");
        skyViewModel.setLatitude(LATITUDE);
        skyViewModel.setLongitude(LONGITUDE);

        skyViewModel.setTimeZoneId("Europe/Istanbul");

        sirius = new Star(
                "HIP 32349",
                "Sirius",
                6.7525,
                -16.7161,
                -1.46,
                "Canis Major",
                "A1V",
                "The brightest star in the night sky.");
        sirius.updateHorizontalPosition(45.0, 120.0);
        skyViewModel.setStars(List.of(sirius));

        viewManagerModel = new ViewManagerModel();
        viewManagerModel.setActiveView(ViewManagerModel.sky_view);
        checkConditions = new CapturingCheckConditions();
        rankForecastDays = new CapturingRankForecastDays();

        final AtomicReference<SkyView> viewReference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> viewReference.set(new SkyView(
                skyViewModel,
                new CheckConditionsController(checkConditions),
                new CheckConditionsViewModel(),
                new RankForecastDaysController(rankForecastDays),
                new RankForecastDaysViewModel(),
                viewManagerModel)));
        view = viewReference.get();
    }

    @Test
    void searchSelectsAnObjectWithACaseInsensitiveExactMatch() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            textField(view, "searchField").setText("  sIrIuS ");
            button(view, "Search").doClick();
        });

        assertSame(sirius, skyViewModel.getSelectedObject());
        assertEquals("Sirius", skyViewModel.getSelectedObjectName());
        assertTrue(skyViewModel.getSelectedObjectDetails().contains("HIP 32349"));
        assertTrue(skyViewModel.getErrorMessage().isEmpty());
        assertSame(sirius, mapPanel(view).getSelectedObject());
    }

    @Test
    void failedSearchShowsAUsefulMessage() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            textField(view, "searchField").setText("Vega");
            button(view, "Search").doClick();
        });

        assertTrue(skyViewModel.getErrorMessage().contains("Vega"));
    }

    @Test
    void changeObservationReturnsToSetupWithoutChangingDisplayedValues() throws Exception {
        SwingUtilities.invokeAndWait(() ->
                button(view, "Change Observation").doClick());

        assertEquals(
                ViewManagerModel.observation_setup_view,
                viewManagerModel.getActiveView());
        assertEquals("Istanbul", skyViewModel.getDisplayedLocation());
        assertEquals("2026-08-12", skyViewModel.getDisplayedDate());
        assertEquals("22:15", skyViewModel.getDisplayedTime());
    }

    @Test
    void displayedObservationFieldsAreReadOnly() {
        assertFalse(textField(view, "locationDisplayField").isEditable());
        assertFalse(textField(view, "dateDisplayField").isEditable());
        assertFalse(textField(view, "timeDisplayField").isEditable());
    }

    @Test
    void weatherAndRankingUseCoordinatesFromSkyViewModel() throws Exception {
        final AtomicReference<JDialog> forecastDialogReference =
                new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            button(view, "Check Conditions").doClick();
            forecastDialogReference.set(view.getForecastRankingDialogForTesting());
            button(forecastDialogReference.get(), "Rank Nights").doClick();
        });

        assertTrue(checkConditions.called.await(
                TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertTrue(rankForecastDays.called.await(
                TIMEOUT_SECONDS, TimeUnit.SECONDS));

        assertEquals(LATITUDE, checkConditions.inputData.getLocation().getLatitude(), 1e-9);
        assertEquals(LONGITUDE, checkConditions.inputData.getLocation().getLongitude(), 1e-9);
        assertEquals(LATITUDE, rankForecastDays.inputData.getLocation().getLatitude(), 1e-9);
        assertEquals(LONGITUDE, rankForecastDays.inputData.getLocation().getLongitude(), 1e-9);
        final LocalDate today = LocalDate.now();
        assertEquals(
                List.of(today, today.plusDays(1), today.plusDays(2)),
                rankForecastDays.inputData.getSelectedDates());
    }

    private JTextField textField(
            final Container container,
            final String name) {
        final Component component = namedComponent(container, name);
        return (JTextField) component;
    }

    private JButton button(
            final Container container,
            final String text) {
        for (final Component component : container.getComponents()) {
            if (component instanceof JButton
                    && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                final JButton nested = findButton((Container) component, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        throw new IllegalStateException("Button not found: " + text);
    }

    private JButton findButton(
            final Container container,
            final String text) {
        for (final Component component : container.getComponents()) {
            if (component instanceof JButton
                    && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                final JButton nested = findButton((Container) component, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private SkyMapPanel mapPanel(final Container container) {
        for (final Component component : container.getComponents()) {
            if (component instanceof SkyMapPanel) {
                return (SkyMapPanel) component;
            }
            if (component instanceof Container) {
                final SkyMapPanel nested =
                        findMapPanel((Container) component);
                if (nested != null) {
                    return nested;
                }
            }
        }
        throw new IllegalStateException("Sky map panel not found.");
    }

    private SkyMapPanel findMapPanel(final Container container) {
        for (final Component component : container.getComponents()) {
            if (component instanceof SkyMapPanel) {
                return (SkyMapPanel) component;
            }
            if (component instanceof Container) {
                final SkyMapPanel nested =
                        findMapPanel((Container) component);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private Component namedComponent(
            final Container container,
            final String name) {
        for (final Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                final Component nested =
                        findNamedComponent((Container) component, name);
                if (nested != null) {
                    return nested;
                }
            }
        }
        throw new IllegalStateException("Component not found: " + name);
    }

    private Component findNamedComponent(
            final Container container,
            final String name) {
        for (final Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                final Component nested =
                        findNamedComponent((Container) component, name);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static class CapturingCheckConditions
            implements CheckConditionsInputBoundary {

        private final CountDownLatch called = new CountDownLatch(1);
        private CheckConditionsInputData inputData;

        @Override
        public void checkConditions(final CheckConditionsInputData inputData) {
            this.inputData = inputData;
            called.countDown();
        }
    }

    private static class CapturingRankForecastDays
            implements RankForecastDaysInputBoundary {

        private final CountDownLatch called = new CountDownLatch(1);
        private RankForecastDaysInputData inputData;

        @Override
        public void rankForecastDays(final RankForecastDaysInputData inputData) {
            this.inputData = inputData;
            called.countDown();
        }
    }
}
