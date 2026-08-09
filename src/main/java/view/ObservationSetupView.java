package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import interface_adapter.ViewManagerModel;
import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.lookup_location.LookupLocationViewModel.LocationSuggestion;
import interface_adapter.view_sky.ObservationSetupViewModel;
import interface_adapter.view_sky.ViewSkyController;

public class ObservationSetupView extends JPanel
        implements ActionListener, PropertyChangeListener {

    private static final int SETUP_VIEW_WIDTH = 900;
    private static final int SETUP_VIEW_HEIGHT = 600;
    private static final int SKY_VIEW_WIDTH = 1350;
    private static final int SKY_VIEW_HEIGHT = 800;
    private static final int TITLE_FONT_SIZE = 52;
    private static final int TITLE_BORDER_TOP = 35;
    private static final int TITLE_BORDER_SIDE = 20;
    private static final int TITLE_BORDER_BOTTOM = 15;
    private static final int FIELD_INSET = 6;
    private static final int FIELD_INSET_HORIZONTAL = 8;
    private static final int ERROR_RED = 180;
    private static final int ERROR_GREEN = 30;
    private static final int ERROR_BLUE = 30;
    private static final int LOCATION_ROW = 0;
    private static final int DATE_TIME_MODE_ROW = 1;
    private static final int DATE_ROW = 2;
    private static final int TIME_ROW = 3;
    private static final int BUTTON_ROW = 4;
    private static final int ERROR_ROW = 5;
    private static final int FIELD_COLUMNS = 20;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ObservationSetupViewModel viewModel;
    private final ViewSkyController viewSkyController;
    private final ViewManagerModel viewManagerModel;
    private final Clock clock;
    private final CityAutocompleteField locationField;
    private final JTextField dateField;
    private final JTextField timeField;
    private final JButton nowButton = new JButton("Now");
    private final JButton customButton = new JButton("Custom");
    private final JButton viewSkyButton = new JButton("View Sky");
    private final JLabel errorLabel;
    private boolean useCurrentDateTime = true;
    private SwingWorker<Void, Void> viewSkyWorker;

    public ObservationSetupView(
            final ObservationSetupViewModel viewModel,
            final ViewSkyController viewSkyController,
            final ViewManagerModel viewManagerModel,
            final LookupLocationController lookupLocationController,
            final LookupLocationViewModel lookupLocationViewModel) {
        this(
                viewModel,
                viewSkyController,
                viewManagerModel,
                lookupLocationController,
                lookupLocationViewModel,
                Clock.systemDefaultZone());
    }

    ObservationSetupView(
            final ObservationSetupViewModel viewModel,
            final ViewSkyController viewSkyController,
            final ViewManagerModel viewManagerModel,
            final LookupLocationController lookupLocationController,
            final LookupLocationViewModel lookupLocationViewModel,
            final Clock clock) {
        this.viewModel = viewModel;
        this.viewSkyController = viewSkyController;
        this.viewManagerModel = viewManagerModel;
        this.clock = Objects.requireNonNull(clock);

        locationField = new CityAutocompleteField(lookupLocationController, lookupLocationViewModel);
        locationField.setName("locationField");
        locationField.getTextField().setName("locationTextField");
        if (viewModel.hasSelectedLocation()) {
            locationField.setSelectedLocation(new LocationSuggestion(
                    viewModel.getSelectedLocationName(),
                    viewModel.getSelectedLatitude(),
                    viewModel.getSelectedLongitude(),
                    viewModel.getSelectedZoneId()));
        }

        dateField = createField("dateField", viewModel.getDate());
        timeField = createField("timeField", viewModel.getTime());
        errorLabel = new JLabel(viewModel.getErrorMessage());
        nowButton.setName("nowButton");
        customButton.setName("customButton");
        viewSkyButton.setName("viewSkyButton");

        setDateTimeMode(true);

        setLayout(SwingStyle.border());
        setBackground(Color.WHITE);

        add(createTitleLabel(), BorderLayout.NORTH);
        add(createFormWrapper(), BorderLayout.CENTER);

        nowButton.addActionListener(this);
        customButton.addActionListener(this);
        viewSkyButton.addActionListener(this);
        viewModel.addPropertyChangeListener(this);
    }

    /**
     * The location input, exposed so a caller can seed or read the chosen place.
     *
     * @return the location autocomplete field
     */
    public CityAutocompleteField getLocationField() {
        return locationField;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final boolean workerBusy = viewSkyWorker != null && !viewSkyWorker.isDone();
        if (event.getSource() == nowButton) {
            setDateTimeMode(true);
        }
        else if (event.getSource() == customButton) {
            setDateTimeMode(false);
        }
        else if (event.getSource() == viewSkyButton && !workerBusy) {
            submitObservation();
        }
    }

    private void submitObservation() {
        final LocationSuggestion location = locationField.getSelectedLocation();

        if (location == null) {
            viewModel.setErrorMessage("Choose a location from the suggestions first.");
        }
        else {
            if (useCurrentDateTime) {
                refreshCurrentDateTime(location);
            }
            final String date = dateField.getText();
            final String time = timeField.getText();

            viewModel.setSelectedLocation(
                    location.getDisplayName(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getZoneId());
            viewModel.setDate(date);
            viewModel.setTime(time);
            viewModel.setErrorMessage("");

            viewSkyButton.setEnabled(false);
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
        dateField.setText(viewModel.getDate());
        timeField.setText(viewModel.getTime());
        errorLabel.setText(viewModel.getErrorMessage());
        revalidate();
        repaint();
    }

    private JLabel createTitleLabel() {
        final JLabel titleLabel = new JLabel("HALO", SwingConstants.CENTER);
        titleLabel.setFont(SwingStyle.sansSerifFont(Font.BOLD, TITLE_FONT_SIZE));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(
                TITLE_BORDER_TOP, TITLE_BORDER_SIDE, TITLE_BORDER_BOTTOM, TITLE_BORDER_SIDE));
        return titleLabel;
    }

    private JPanel createFormWrapper() {
        final JPanel formPanel = new JPanel(SwingStyle.gridBagLayout());
        formPanel.setBackground(Color.WHITE);

        final GridBagConstraints constraints = SwingStyle.gridBagConstraints();
        constraints.insets = SwingStyle.insets(
                FIELD_INSET, FIELD_INSET_HORIZONTAL, FIELD_INSET, FIELD_INSET_HORIZONTAL);
        constraints.anchor = GridBagConstraints.WEST;

        addFieldRow(formPanel, constraints, LOCATION_ROW, "Location:", locationField);
        addFieldRow(
                formPanel,
                constraints,
                DATE_TIME_MODE_ROW,
                "Observation time:",
                createDateTimeModePanel());
        addFieldRow(formPanel, constraints, DATE_ROW, "Date (yyyy-MM-dd):", dateField);
        addFieldRow(formPanel, constraints, TIME_ROW, "Time (HH:mm):", timeField);

        constraints.gridx = 0;
        constraints.gridy = BUTTON_ROW;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.0;
        formPanel.add(viewSkyButton, constraints);

        errorLabel.setForeground(SwingStyle.rgb(ERROR_RED, ERROR_GREEN, ERROR_BLUE));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        constraints.gridy = ERROR_ROW;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(errorLabel, constraints);

        final JPanel formWrapper = new JPanel(SwingStyle.gridBagLayout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.add(formPanel);
        return formWrapper;
    }

    private JPanel createDateTimeModePanel() {
        final JPanel modePanel = new JPanel(SwingStyle.flow(FlowLayout.LEFT, 6, 0));
        modePanel.setBackground(Color.WHITE);
        modePanel.add(nowButton);
        modePanel.add(customButton);
        return modePanel;
    }

    private void setDateTimeMode(final boolean currentDateTime) {
        useCurrentDateTime = currentDateTime;
        nowButton.setEnabled(!currentDateTime);
        customButton.setEnabled(currentDateTime);
        dateField.setEditable(!currentDateTime);
        timeField.setEditable(!currentDateTime);
        if (currentDateTime) {
            refreshCurrentDateTime(locationField.getSelectedLocation());
        }
    }

    private void refreshCurrentDateTime(final LocationSuggestion location) {
        ZoneId zoneId = clock.getZone();
        if (location != null) {
            zoneId = ZoneId.of(location.getZoneId());
        }
        final ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zoneId);
        final String currentDate = now.toLocalDate().toString();
        final String currentTime = now.format(TIME_FORMAT);
        dateField.setText(currentDate);
        timeField.setText(currentTime);
        viewModel.setDate(currentDate);
        viewModel.setTime(currentTime);
    }

    private JTextField createField(final String name, final String value) {
        final JTextField field = new JTextField(value, FIELD_COLUMNS);
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

    /** Runs the sky use case off the event dispatch thread, then reports the outcome back on it. */
    private final class ViewSkyWorker extends SwingWorker<Void, Void> {

        private final LocationSuggestion location;
        private final String date;
        private final String time;

        ViewSkyWorker(
                final LocationSuggestion location,
                final String date,
                final String time) {
            this.location = location;
            this.date = date;
            this.time = time;
        }

        @Override
        protected Void doInBackground() {
            viewSkyController.viewSky(
                    location.getDisplayName(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getZoneId(),
                    date,
                    time);
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
