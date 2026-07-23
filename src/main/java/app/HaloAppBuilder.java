package app;

import javax.swing.JFrame;

import interface_adapter.ViewManagerModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.SkyViewModel;
import view.LoadingView;
import view.ObservationSetupView;
import view.SkyView;
import view.ViewManager;

public class HaloAppBuilder {

    private static final String initial_view = ViewManagerModel.observation_setup_view;

    public JFrame build() {
        final ViewManagerModel viewManagerModel = new ViewManagerModel();
        final ObservationSetupViewModel observationSetupViewModel =
                new ObservationSetupViewModel();
        final SkyViewModel skyViewModel = new SkyViewModel();

        final ViewManager viewManager = new ViewManager(viewManagerModel);
        final ObservationSetupView observationSetupView =
                new ObservationSetupView(observationSetupViewModel);
        final LoadingView loadingView = new LoadingView();
        final SkyView skyView = new SkyView(skyViewModel);

        viewManager.registerView(
                ViewManagerModel.observation_setup_view, observationSetupView);
        viewManager.registerView(ViewManagerModel.loading_view, loadingView);
        viewManager.registerView(ViewManagerModel.sky_view, skyView);

        final JFrame frame = new JFrame("Halo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(viewManager);
        if (ViewManagerModel.sky_view.equals(initial_view)) {
            frame.setSize(1350, 800);
        }
        else {
            frame.setSize(900, 600);
        }
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        viewManagerModel.setActiveView(initial_view);
        return frame;
    }
}
