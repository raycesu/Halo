package view;

import java.awt.Component;
import java.awt.Container;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import interface_adapter.ViewManagerModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import interface_adapter.view_sky.ViewSkyController;
import interface_adapter.view_sky.ViewSkyPresenter;
import org.junit.jupiter.api.Test;
import use_case.view_sky.ViewSkyInputBoundary;
import use_case.view_sky.ViewSkyInputData;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationSetupViewTest {

    private static final long TIMEOUT_SECONDS = 3;

    @Test
    void validSubmissionShowsLoadingPassesInputsAndThenShowsSky() throws Exception {
        final TestContext context = createContext(false);
        final CountDownLatch skyShown =
                activeViewLatch(context.viewManagerModel, ViewManagerModel.sky_view);

        SwingUtilities.invokeAndWait(() -> {
            textField(context.view, "locationField").setText("Istanbul");
            textField(context.view, "latitudeField").setText("41.0082");
            textField(context.view, "longitudeField").setText("28.9784");
            textField(context.view, "zoneIdField").setText("Europe/Istanbul");
            textField(context.view, "dateField").setText("2026-08-12");
            textField(context.view, "timeField").setText("22:15");
            button(context.view, "View Sky").doClick();
        });

        assertTrue(context.useCase.started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(ViewManagerModel.loading_view,
                context.viewManagerModel.getActiveView());
        assertFalse(button(context.view, "View Sky").isEnabled());

        final ViewSkyInputData inputData = context.useCase.inputData;
        assertNotNull(inputData);
        assertEquals("Istanbul", inputData.getLocationName());
        assertEquals(41.0082, inputData.getLatitude(), 1e-9);
        assertEquals(28.9784, inputData.getLongitude(), 1e-9);
        assertEquals(ZoneId.of("Europe/Istanbul"), inputData.getZoneId());
        assertEquals("2026-08-12T22:15",
                inputData.getObservationDateTime().toString());

        context.useCase.release.countDown();

        assertTrue(skyShown.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(ViewManagerModel.sky_view,
                context.viewManagerModel.getActiveView());
        assertEquals("Istanbul", context.setupViewModel.getLocation());
        assertEquals("41.0082", context.setupViewModel.getLatitude());
        assertEquals("28.9784", context.setupViewModel.getLongitude());
        assertEquals("Europe/Istanbul", context.setupViewModel.getZoneId());
        assertEquals("Istanbul", context.skyViewModel.getDisplayedLocation());
    }

    @Test
    void failedSubmissionReturnsToSetupAndDisplaysTheError() throws Exception {
        final TestContext context = createContext(true);
        final CountDownLatch setupShown =
                activeViewLatch(context.viewManagerModel,
                        ViewManagerModel.observation_setup_view);

        SwingUtilities.invokeAndWait(() ->
                button(context.view, "View Sky").doClick());

        assertTrue(context.useCase.started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(ViewManagerModel.loading_view,
                context.viewManagerModel.getActiveView());
        context.useCase.release.countDown();

        assertTrue(setupShown.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(ViewManagerModel.observation_setup_view,
                context.viewManagerModel.getActiveView());
        assertEquals("The observation could not be loaded.",
                context.setupViewModel.getErrorMessage());
        assertTrue(button(context.view, "View Sky").isEnabled());
    }

    private TestContext createContext(final boolean fail) throws Exception {
        final ObservationSetupViewModel setupViewModel =
                new ObservationSetupViewModel();
        final SkyViewModel skyViewModel = new SkyViewModel();
        final ViewManagerModel viewManagerModel = new ViewManagerModel();
        final ViewSkyOutputBoundary presenter =
                new ViewSkyPresenter(skyViewModel, setupViewModel);
        final BlockingViewSkyUseCase useCase =
                new BlockingViewSkyUseCase(presenter, fail);
        final ViewSkyController controller =
                new ViewSkyController(useCase, presenter);
        final AtomicReference<ObservationSetupView> viewReference =
                new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> viewReference.set(
                new ObservationSetupView(
                        setupViewModel,
                        controller,
                        viewManagerModel)));

        return new TestContext(
                viewReference.get(),
                setupViewModel,
                skyViewModel,
                viewManagerModel,
                useCase);
    }

    private CountDownLatch activeViewLatch(
            final ViewManagerModel viewManagerModel,
            final String expectedView) {
        final CountDownLatch latch = new CountDownLatch(1);
        viewManagerModel.addPropertyChangeListener(event -> {
            if ("activeView".equals(event.getPropertyName())
                    && expectedView.equals(event.getNewValue())) {
                latch.countDown();
            }
        });
        return latch;
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

    private static class BlockingViewSkyUseCase
            implements ViewSkyInputBoundary {

        private final ViewSkyOutputBoundary outputBoundary;
        private final boolean fail;
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private ViewSkyInputData inputData;

        BlockingViewSkyUseCase(
                final ViewSkyOutputBoundary outputBoundary,
                final boolean fail) {
            this.outputBoundary = outputBoundary;
            this.fail = fail;
        }

        @Override
        public void execute(final ViewSkyInputData inputData) {
            this.inputData = inputData;
            started.countDown();
            try {
                release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }

            if (fail) {
                outputBoundary.prepareFailView(
                        "The observation could not be loaded.");
            }
            else {
                outputBoundary.prepareSuccessView(new ViewSkyOutputData(
                        inputData.getLocationName(),
                        inputData.getDate(),
                        inputData.getTime(),
                        inputData.getLatitude(),
                        inputData.getLongitude(),
                        List.of()));
            }
        }
    }

    private static class TestContext {

        private final ObservationSetupView view;
        private final ObservationSetupViewModel setupViewModel;
        private final SkyViewModel skyViewModel;
        private final ViewManagerModel viewManagerModel;
        private final BlockingViewSkyUseCase useCase;

        TestContext(
                final ObservationSetupView view,
                final ObservationSetupViewModel setupViewModel,
                final SkyViewModel skyViewModel,
                final ViewManagerModel viewManagerModel,
                final BlockingViewSkyUseCase useCase) {
            this.view = view;
            this.setupViewModel = setupViewModel;
            this.skyViewModel = skyViewModel;
            this.viewManagerModel = viewManagerModel;
            this.useCase = useCase;
        }
    }
}
