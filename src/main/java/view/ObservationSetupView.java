package view;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import entity.ObserverLocation;
import interface_adapter.ViewManagerModel;
import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.ViewSkyController;

public class ObservationSetupView extends JPanel
        implements ActionListener, PropertyChangeListener {

    private static final int SETUP_VIEW_WIDTH = 900;
    private static final int SETUP_VIEW_HEIGHT = 600;
    private static final int SKY_VIEW_WIDTH = 1350;
    private static final int SKY_VIEW_HEIGHT = 800;

    private final ObservationSetupViewModel viewModel;
    private final ViewSkyController viewSkyController;
    private final ViewManagerModel viewManagerModel;
    private final ObservationSetupFormPanel formPanel;
    private SwingWorker<Void, Void> viewSkyWorker;

    public ObservationSetupView(
            final ObservationSetupViewModel viewModel,
            final ViewSkyController viewSkyController,
            final ViewManagerModel viewManagerModel,
            final LookupLocationController lookupLocationController,
            final LookupLocationViewModel lookupLocationViewModel) {
        this.viewModel = viewModel;
        this.viewSkyController = viewSkyController;
        this.viewManagerModel = viewManagerModel;

        formPanel = new ObservationSetupFormPanel(
                viewModel, lookupLocationController, lookupLocationViewModel);

        setLayout(SwingStyle.border());
        add(formPanel, BorderLayout.CENTER);

        formPanel.getViewSkyButton().addActionListener(this);
        viewModel.addPropertyChangeListener(this);
    }

    /**
     * The location input, exposed so a caller can seed or read the chosen place.
     *
     * @return the location autocomplete field
     */
    public CityAutocompleteField getLocationField() {
        return formPanel.getLocationField();
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final boolean workerBusy = viewSkyWorker != null && !viewSkyWorker.isDone();
        if (event.getSource() == formPanel.getViewSkyButton() && !workerBusy) {
            submitObservation();
        }
    }

    private void submitObservation() {
        final ObserverLocation location = formPanel.getLocationField().getSelectedLocation();
        final String date = formPanel.getDateField().getText();
        final String time = formPanel.getTimeField().getText();

        // A location that was never chosen from the list is reported here rather than sent on,
        // since there are no coordinates to send.
        if (location == null) {
            viewModel.setErrorMessage("Choose a location from the suggestions first.");
        }
        else {
            viewModel.setSelectedLocation(location);
            viewModel.setDate(date);
            viewModel.setTime(time);
            viewModel.setErrorMessage("");

            formPanel.getViewSkyButton().setEnabled(false);
            viewManagerModel.setActiveView(ViewManagerModel.LOADING_VIEW);

            viewSkyWorker = new ViewSkyWorker(location, date, time);
            viewSkyWorker.execute();
        }
    }

    private void resizeWindow(final int width, final int height) {
        final Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.setSize(width, height);
            window.setLocationRelativeTo(null);
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final Runnable updateFields = this::applyViewModelToFields;

        if (SwingUtilities.isEventDispatchThread()) {
            updateFields.run();
        }
        else {
            SwingUtilities.invokeLater(updateFields);
        }
    }

    private void applyViewModelToFields() {
        // The location field is left alone: it owns the resolved choice, and rewriting its
        // text from the view model would discard the coordinates that came with it.
        formPanel.getDateField().setText(viewModel.getDate());
        formPanel.getTimeField().setText(viewModel.getTime());
        formPanel.getErrorLabel().setText(viewModel.getErrorMessage());
        revalidate();
        repaint();
    }

    /** Runs the sky use case off the event dispatch thread, then reports the outcome back on it. */
    private final class ViewSkyWorker extends SwingWorker<Void, Void> {

        private final ObserverLocation location;
        private final String date;
        private final String time;

        ViewSkyWorker(final ObserverLocation location, final String date, final String time) {
            this.location = location;
            this.date = date;
            this.time = time;
        }

        @Override
        protected Void doInBackground() {
            viewSkyController.viewSky(location, date, time);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                viewModel.setErrorMessage("Sky loading was interrupted.");
            }
            catch (ExecutionException exception) {
                viewModel.setErrorMessage("Could not load the sky.");
            }

            formPanel.getViewSkyButton().setEnabled(true);
            if (viewModel.getErrorMessage().isBlank()) {
                viewManagerModel.setActiveView(ViewManagerModel.SKY_VIEW);
                resizeWindow(SKY_VIEW_WIDTH, SKY_VIEW_HEIGHT);
            }
            else {
                viewManagerModel.setActiveView(ViewManagerModel.OBSERVATION_SETUP_VIEW);
                resizeWindow(SETUP_VIEW_WIDTH, SETUP_VIEW_HEIGHT);
            }
        }
    }
}
