package app;

import java.util.List;

import javax.swing.JFrame;

import entity.ObserverLocation;
import interface_adapter.ViewManagerModel;
import use_case.lookup_location.LocationDataAccessInterface;
import view.ViewManager;

/**
 * Composition root: builds every concrete implementation and wires them into a runnable app.
 *
 * <p>The actual construction work is delegated to a handful of focused builder classes
 * ({@link DataAccessBundle}, {@link ViewModelBundle}, {@link UseCaseBuilder},
 * {@link ViewFactory}) so that no single class has to directly depend on every concrete DAO,
 * interactor, presenter, controller, and view in the app. This class remains the single place
 * {@link Main} calls, and the only place that assembles the final object graph.
 */
public class HaloAppBuilder {

    private static final String INITIAL_VIEW = ViewManagerModel.OBSERVATION_SETUP_VIEW;

    /** The place the app opens on, so the first request works before the user picks anything. */
    private static final String DEFAULT_LOCATION = "Toronto";

    private static final int INITIAL_FRAME_WIDTH = 900;
    private static final int INITIAL_FRAME_HEIGHT = 600;

    /**
     * Builds the fully wired application window.
     *
     * @return a ready-to-show {@link JFrame} containing the app's {@link ViewManager}
     */
    public JFrame build() {
        final ViewManager viewManager = buildViewManager();

        final JFrame frame = new JFrame("Halo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(viewManager);
        frame.setSize(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    /**
     * Looks up the place the app opens on, or null if the dataset does not contain it, in which
     * case the user simply picks one before the first request.
     *
     * @param locationDataAccess the location catalogue to search
     * @return the default observer location, or null if it is not in the catalogue
     */
    private ObserverLocation resolveDefaultLocation(
            final LocationDataAccessInterface locationDataAccess) {
        final List<ObserverLocation> matches = locationDataAccess.findByName(DEFAULT_LOCATION, 1);

        final ObserverLocation defaultLocation;
        if (matches.isEmpty()) {
            defaultLocation = null;
        }
        else {
            defaultLocation = matches.get(0);
        }
        return defaultLocation;
    }

    ViewManager buildViewManager() {
        final DataAccessBundle dataAccess = new DataAccessBundle();
        final ViewModelBundle viewModels = new ViewModelBundle();
        viewModels.getObservationSetupViewModel().setSelectedLocation(
                resolveDefaultLocation(dataAccess.getLocationDataAccess()));

        final ControllerBundle controllers = UseCaseBuilder.build(dataAccess, viewModels);
        final ViewManager viewManager = ViewFactory.build(viewModels, controllers);

        viewModels.getViewManagerModel().setActiveView(INITIAL_VIEW);
        return viewManager;
    }
}
