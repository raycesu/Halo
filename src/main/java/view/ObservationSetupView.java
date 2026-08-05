package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import entity.ObserverLocation;
import interface_adapter.ViewManagerModel;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.ViewSkyController;
import use_case.location.LocationDataAccessInterface;

public class ObservationSetupView extends JPanel
        implements ActionListener, PropertyChangeListener {

    private final ObservationSetupViewModel viewModel;
    private final ViewSkyController viewSkyController;
    private final ViewManagerModel viewManagerModel;
    private final CityAutocompleteField locationField;
    private final JTextField dateField;
    private final JTextField timeField;
    private final JButton viewSkyButton = new JButton("View Sky");
    private final JLabel errorLabel;
    private SwingWorker<Void, Void> viewSkyWorker;

    public ObservationSetupView(
            final ObservationSetupViewModel viewModel,
            final ViewSkyController viewSkyController,
            final ViewManagerModel viewManagerModel,
            final LocationDataAccessInterface locationDataAccess) {
        this.viewModel = viewModel;
        this.viewSkyController = viewSkyController;
        this.viewManagerModel = viewManagerModel;

        locationField = new CityAutocompleteField(locationDataAccess);
        locationField.setName("locationField");
        locationField.getTextField().setName("locationTextField");
        locationField.setSelectedLocation(viewModel.getSelectedLocation());

        dateField = createField("dateField", viewModel.getDate());
        timeField = createField("timeField", viewModel.getTime());
        errorLabel = new JLabel(viewModel.getErrorMessage());
        viewSkyButton.setName("viewSkyButton");

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        final JLabel titleLabel = new JLabel("HALO", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 52));
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 20, 15, 20));
        add(titleLabel, BorderLayout.NORTH);

        final JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 8, 6, 8);
        constraints.anchor = GridBagConstraints.WEST;

        addFieldRow(formPanel, constraints, 0, "Location:", locationField);
        addFieldRow(formPanel, constraints, 1, "Date (yyyy-MM-dd):", dateField);
        addFieldRow(formPanel, constraints, 2, "Time (HH:mm):", timeField);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.0;
        formPanel.add(viewSkyButton, constraints);

        errorLabel.setForeground(new Color(180, 30, 30));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(errorLabel, constraints);

        final JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.add(formPanel);
        add(formWrapper, BorderLayout.CENTER);

        viewSkyButton.addActionListener(this);
        viewModel.addPropertyChangeListener(this);
    }

    /** The location input, exposed so a caller can seed or read the chosen place. */
    public CityAutocompleteField getLocationField() {
        return locationField;
    }

    private JTextField createField(final String name, final String value) {
        final JTextField field = new JTextField(value, 20);
        field.setName(name);
        return field;
    }

    private void addFieldRow(
            final JPanel formPanel,
            final GridBagConstraints constraints,
            final int row,
            final String label,
            final Component field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel(label), constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        formPanel.add(field, constraints);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() != viewSkyButton
                || (viewSkyWorker != null && !viewSkyWorker.isDone())) {
            return;
        }

        final ObserverLocation location = locationField.getSelectedLocation();
        final String date = dateField.getText();
        final String time = timeField.getText();

        // A location that was never chosen from the list is reported here rather than sent on,
        // since there are no coordinates to send.
        if (location == null) {
            viewModel.setErrorMessage("Choose a location from the suggestions first.");
            return;
        }

        viewModel.setSelectedLocation(location);
        viewModel.setDate(date);
        viewModel.setTime(time);
        viewModel.setErrorMessage("");

        viewSkyButton.setEnabled(false);
        viewManagerModel.setActiveView(ViewManagerModel.loading_view);

        viewSkyWorker = new SwingWorker<>() {
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

                viewSkyButton.setEnabled(true);
                if (viewModel.getErrorMessage().isBlank()) {
                    viewManagerModel.setActiveView(ViewManagerModel.sky_view);
                    resizeWindow(1350, 800);
                }
                else {
                    viewManagerModel.setActiveView(ViewManagerModel.observation_setup_view);
                    resizeWindow(900, 600);
                }
            }
        };
        viewSkyWorker.execute();
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
        final Runnable updateFields = () -> {
            // The location field is left alone: it owns the resolved choice, and rewriting its
            // text from the view model would discard the coordinates that came with it.
            dateField.setText(viewModel.getDate());
            timeField.setText(viewModel.getTime());
            errorLabel.setText(viewModel.getErrorMessage());
            revalidate();
            repaint();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            updateFields.run();
        }
        else {
            SwingUtilities.invokeLater(updateFields);
        }
    }
}
