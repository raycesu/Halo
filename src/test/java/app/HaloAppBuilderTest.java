package app;

import java.awt.Component;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaloAppBuilderTest {

    @Test
    void buildsAllThreeWiredViewsAndStartsOnObservationSetup() throws Exception {
        final AtomicReference<ViewManager> managerReference =
                new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> managerReference.set(
                new HaloAppBuilder().buildViewManager()));

        final Component[] views = managerReference.get().getComponents();

        assertEquals(3, views.length);
        assertTrue(views[0] instanceof ObservationSetupView);
        assertTrue(views[0].isVisible());
        assertTrue(views[1] instanceof LoadingView);
        assertFalse(views[1].isVisible());
        assertTrue(views[2] instanceof SkyView);
        assertFalse(views[2].isVisible());
    }
}
