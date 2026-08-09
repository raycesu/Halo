package app;

import java.util.List;

import javax.swing.JFrame;

import entity.ObserverLocation;
import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationViewModel;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import use_case.lookup_location.LocationDataAccessInterface;
import view.ViewManager;

/**
 * Composition root: builds every concrete implementation and wires them into a runnable app.
 *
 * <p>Construction is split into focused builder steps ({@link DataAccessBundle},
 * {@link UseCaseBuilder}, {@link ViewFactory}) so that no single class directly depends on
 * every concrete DAO, interactor, presenter, controller, and view.
 */
public class HaloAppBuilder {

    private static final String INITIAL_VIEW = ViewManagerModel.OBSERVATION_SETUP_VIEW;
    private static final String DEFAULT_LOCATION = "Toronto";
    private static final int INITIAL_FRAME_WIDTH = 900;
    private static final int INITIAL_FRAME_HEIGHT = 600;

    private final ViewManagerModel viewManagerModel = new ViewManagerModel();
    private final ObservationSetupViewModel observationSetupViewModel = new ObservationSetupViewModel();
    private final SkyViewModel skyViewModel = new SkyViewModel();
    private final CheckConditionsViewModel checkConditionsViewModel = new CheckConditionsViewModel();
    private final RankForecastDaysViewModel rankForecastDaysViewModel = new RankForecastDaysViewModel();
    private final ConstellationViewModel constellationViewModel = new ConstellationViewModel();
    private final LookupLocationViewModel lookupLocationViewModel = new LookupLocationViewModel();

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
        final ObserverLocation defaultLocation =
                resolveDefaultLocation(dataAccess.getLocationDataAccess());
        if (defaultLocation != null) {
            observationSetupViewModel.setSelectedLocation(
                    defaultLocation.getDisplayName(),
                    defaultLocation.getLatitude(),
                    defaultLocation.getLongitude(),
                    defaultLocation.getZoneId().getId());
        }

        final UseCaseBuilder useCases = new UseCaseBuilder(
                dataAccess,
                skyViewModel, observationSetupViewModel,
                checkConditionsViewModel, rankForecastDaysViewModel,
                constellationViewModel, lookupLocationViewModel);

        final ViewManager viewManager = ViewFactory.build(
                viewManagerModel,
                observationSetupViewModel, skyViewModel,
                checkConditionsViewModel, rankForecastDaysViewModel,
                constellationViewModel, lookupLocationViewModel,
                useCases);

        viewManagerModel.setActiveView(INITIAL_VIEW);
        return viewManager;
    }
}
