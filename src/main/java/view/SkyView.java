package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import entity.ObserverLocation;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysController;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel.RankedDayDisplayItem;
import interface_adapter.view_sky.SkyViewModel;
import interface_adapter.view_sky.ViewSkyController;
import use_case.location.LocationDataAccessInterface;

public class SkyView extends JPanel implements PropertyChangeListener {

    /** How many nights the forecast ranking starts out comparing. */
    private static final int DEFAULT_RANK_FORECAST_NIGHTS = 3;

    private final SkyViewModel viewModel;
    private final ViewSkyController viewSkyController;
    private final CheckConditionsController checkConditionsController;
    private final CheckConditionsViewModel checkConditionsViewModel;
    private final RankForecastDaysController rankForecastDaysController;
    private final RankForecastDaysViewModel rankForecastDaysViewModel;
    private final CityAutocompleteField locationField;
    private final SkyMapPanel skyMapPanel = new SkyMapPanel();
    private final JButton viewSkyButton = new JButton("View Sky");
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JLabel objectNameLabel = new JLabel();
    private final JTextArea objectDetailsArea = new JTextArea();
    private final JTextArea weatherArea = new JTextArea();
    private final JButton checkConditionsButton = new JButton("Check Conditions");
    private final JLabel errorLabel = new JLabel();
    private final List<LocalDate> selectedForecastDates = new ArrayList<>(defaultForecastDates());
    private final JButton selectDatesButton = new JButton("Select Dates");
    private final JLabel selectedDatesSummaryLabel = new JLabel();
    private final JButton rankForecastButton = new JButton("Rank Nights");
    private final DefaultListModel<RankedDayDisplayItem> rankedDaysListModel = new DefaultListModel<>();
    private final JList<RankedDayDisplayItem> rankedDaysList = new JList<>(rankedDaysListModel);
    private final JLabel rankForecastErrorLabel = new JLabel();
    private boolean updatingFields;

    public SkyView(
            final SkyViewModel viewModel,
            final ViewSkyController viewSkyController,
            final LocationDataAccessInterface locationDataAccess,
            final ObserverLocation initialLocation,
            final CheckConditionsController checkConditionsController,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysController rankForecastDaysController,
            final RankForecastDaysViewModel rankForecastDaysViewModel) {
        this.viewModel = viewModel;
        this.viewSkyController = viewSkyController;
        this.checkConditionsController = checkConditionsController;
        this.checkConditionsViewModel = checkConditionsViewModel;
        this.rankForecastDaysController = rankForecastDaysController;
        this.rankForecastDaysViewModel = rankForecastDaysViewModel;
        this.locationField = new CityAutocompleteField(locationDataAccess);
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        add(createLeftPanel(), BorderLayout.WEST);
        add(skyMapPanel, BorderLayout.CENTER);
        add(createRightSidebar(), BorderLayout.EAST);

        // Start on a resolved place so the first request works without the user picking one, while
        // still going through the same selection the autocomplete produces.
        locationField.setSelectedLocation(initialLocation);

        updateFromViewModel();
        registerTextFieldListeners();
        viewModel.addPropertyChangeListener(this);
        checkConditionsViewModel.addPropertyChangeListener(this);
        rankForecastDaysViewModel.addPropertyChangeListener(this);
    }

    private JPanel createLeftPanel() {
        final JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(260, 0));
        leftPanel.setBackground(new Color(238, 241, 246));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        final JLabel searchHeading = new JLabel("Search");
        searchHeading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        searchHeading.setAlignmentX(LEFT_ALIGNMENT);

        final JTextField searchField = new JTextField();

        final JButton searchButton = new JButton("Search");
        searchButton.setEnabled(false);

        final JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(190, 28));
        searchPanel.setMaximumSize(new Dimension(190, 28));
        searchPanel.setAlignmentX(LEFT_ALIGNMENT);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        final JLabel heading = new JLabel("Observation");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        heading.setAlignmentX(LEFT_ALIGNMENT);

        final Dimension fieldSize = new Dimension(190, 28);
        locationField.setPreferredSize(fieldSize);
        locationField.setMaximumSize(fieldSize);
        dateField.setPreferredSize(fieldSize);
        dateField.setMaximumSize(fieldSize);
        timeField.setPreferredSize(fieldSize);
        timeField.setMaximumSize(fieldSize);
        locationField.setAlignmentX(LEFT_ALIGNMENT);
        dateField.setAlignmentX(LEFT_ALIGNMENT);
        timeField.setAlignmentX(LEFT_ALIGNMENT);

        viewSkyButton.setAlignmentX(LEFT_ALIGNMENT);
        viewSkyButton.addActionListener(event -> handleViewSky());

        errorLabel.setForeground(new Color(180, 30, 30));
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        final JButton constellationButton = new JButton("Custom Constellation");
        constellationButton.setEnabled(false);

        final JPanel constellationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        constellationPanel.setOpaque(false);
        constellationPanel.setAlignmentX(LEFT_ALIGNMENT);
        constellationPanel.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, constellationButton.getPreferredSize().height));
        constellationPanel.add(constellationButton);

        leftPanel.add(searchHeading);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(searchPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        leftPanel.add(heading);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        leftPanel.add(new JLabel("Location"));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(locationField);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        leftPanel.add(new JLabel("Date"));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(dateField);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        leftPanel.add(new JLabel("Time"));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(timeField);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        leftPanel.add(viewSkyButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        leftPanel.add(errorLabel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(constellationPanel);
        return leftPanel;
    }

    private JPanel createRightSidebar() {
        final JPanel sidebar = new JPanel(new GridLayout(3, 1, 0, 15));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(238, 241, 246));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        final JPanel starPanel = new JPanel(new BorderLayout(0, 10));
        starPanel.setBackground(sidebar.getBackground());
        starPanel.setBorder(BorderFactory.createTitledBorder("Star Information"));
        objectNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        configureTextArea(objectDetailsArea);
        starPanel.add(objectNameLabel, BorderLayout.NORTH);
        starPanel.add(objectDetailsArea, BorderLayout.CENTER);

        final JPanel weatherPanel = new JPanel(new BorderLayout(0, 8));
        weatherPanel.setBackground(sidebar.getBackground());
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Weather"));
        configureTextArea(weatherArea);
        weatherPanel.add(weatherArea, BorderLayout.CENTER);
        weatherPanel.add(checkConditionsButton, BorderLayout.SOUTH);
        checkConditionsButton.addActionListener(event -> handleCheckConditions());

        sidebar.add(starPanel);
        sidebar.add(weatherPanel);
        sidebar.add(createForecastRankingPanel(sidebar.getBackground()));
        return sidebar;
    }

    private JPanel createForecastRankingPanel(final Color backgroundColor) {
        final JPanel forecastPanel = new JPanel(new BorderLayout(0, 8));
        forecastPanel.setBackground(backgroundColor);
        forecastPanel.setBorder(BorderFactory.createTitledBorder("Forecast Ranking"));

        final JPanel dateSelectionRow = new JPanel(new BorderLayout(6, 0));
        dateSelectionRow.setOpaque(false);
        dateSelectionRow.setAlignmentX(LEFT_ALIGNMENT);
        selectedDatesSummaryLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dateSelectionRow.add(selectedDatesSummaryLabel, BorderLayout.CENTER);
        dateSelectionRow.add(selectDatesButton, BorderLayout.EAST);
        selectDatesButton.addActionListener(event -> openDatePicker());

        rankForecastButton.setAlignmentX(LEFT_ALIGNMENT);
        rankForecastButton.addActionListener(event -> handleRankForecastDays());

        rankedDaysList.setCellRenderer(new RankedDayCellRenderer());
        rankedDaysList.setVisibleRowCount(4);
        final JScrollPane rankedDaysScrollPane = new JScrollPane(rankedDaysList);

        rankForecastErrorLabel.setForeground(new Color(180, 30, 30));
        rankForecastErrorLabel.setAlignmentX(LEFT_ALIGNMENT);

        final JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(dateSelectionRow);
        topSection.add(Box.createRigidArea(new Dimension(0, 6)));
        topSection.add(rankForecastButton);
        topSection.add(Box.createRigidArea(new Dimension(0, 6)));
        topSection.add(rankForecastErrorLabel);

        forecastPanel.add(topSection, BorderLayout.NORTH);
        forecastPanel.add(rankedDaysScrollPane, BorderLayout.CENTER);
        updateSelectedDatesSummaryLabel();
        return forecastPanel;
    }

    private void openDatePicker() {
        final Window owner = SwingUtilities.getWindowAncestor(this);
        final CalendarDatePickerDialog datePickerDialog =
                new CalendarDatePickerDialog(owner, new TreeSet<>(selectedForecastDates));
        datePickerDialog.setVisible(true);

        selectedForecastDates.clear();
        selectedForecastDates.addAll(datePickerDialog.getSelectedDates());
        updateSelectedDatesSummaryLabel();
    }

    private void updateSelectedDatesSummaryLabel() {
        if (selectedForecastDates.isEmpty()) {
            selectedDatesSummaryLabel.setText("No dates selected");
            selectedDatesSummaryLabel.setToolTipText(null);
            return;
        }
        final List<LocalDate> sortedDates = new ArrayList<>(selectedForecastDates);
        Collections.sort(sortedDates);
        final StringBuilder tooltipBuilder = new StringBuilder();
        for (int index = 0; index < sortedDates.size(); index++) {
            if (index > 0) {
                tooltipBuilder.append(", ");
            }
            tooltipBuilder.append(sortedDates.get(index));
        }
        final String suffix = sortedDates.size() == 1 ? " date selected" : " dates selected";
        selectedDatesSummaryLabel.setText(sortedDates.size() + suffix);
        selectedDatesSummaryLabel.setToolTipText(tooltipBuilder.toString());
    }

    private final class RankedDayCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                final JList<?> list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final Component component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value instanceof RankedDayDisplayItem) {
                final RankedDayDisplayItem item = (RankedDayDisplayItem) value;
                setText(item.getRank() + ". " + item.getDateText()
                        + " \u2014 " + item.getRatingText() + " (" + item.getOverallScoreText() + ")");
                if (!isSelected) {
                    setForeground(colorOrDefault(item.getRatingColor()));
                }
            }
            return component;
        }
    }

    private void configureTextArea(final JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void updateFromViewModel() {
        // The location field is not refreshed from the view model: it owns a resolved choice, and
        // overwriting its text would silently discard the coordinates that came with it.
        dateField.setText(viewModel.getDisplayedDate());
        timeField.setText(viewModel.getDisplayedTime());
        objectNameLabel.setText(textOrPlaceholder(
                viewModel.getSelectedObjectName(), "No object selected"));
        objectDetailsArea.setText(textOrPlaceholder(
                viewModel.getSelectedObjectDetails(), "Details unavailable"));
        updateWeatherFromViewModel();
        errorLabel.setText(viewModel.getErrorMessage());
        updateRankedDaysFromViewModel();
        revalidate();
        repaint();
    }

    private void updateRankedDaysFromViewModel() {
        rankedDaysListModel.clear();
        for (final RankedDayDisplayItem item : rankForecastDaysViewModel.getRankedDays()) {
            rankedDaysListModel.addElement(item);
        }
        rankForecastErrorLabel.setText(rankForecastDaysViewModel.getErrorMessage());
    }

    private void updateWeatherFromViewModel() {
        final String error = checkConditionsViewModel.getErrorMessage();
        if (!error.isBlank()) {
            weatherArea.setForeground(Color.RED);
            weatherArea.setText(error);
            return;
        }
        final String combinedText = String.join("\n",
                checkConditionsViewModel.getCloudCoverText(),
                checkConditionsViewModel.getVisibilityText(),
                checkConditionsViewModel.getPrecipitationText(),
                checkConditionsViewModel.getWeatherCodeText(),
                checkConditionsViewModel.getOverallScoreText(),
                checkConditionsViewModel.getRatingText());
        weatherArea.setForeground(colorOrDefault(checkConditionsViewModel.getRatingColor()));
        weatherArea.setText(textOrPlaceholder(
                combinedText, "Click Check Conditions to see forecast conditions."));
    }

    private Color colorOrDefault(final String hexColor) {
        final Color color;
        if (hexColor == null || hexColor.isBlank()) {
            color = Color.BLACK;
        }
        else {
            color = Color.decode(hexColor);
        }
        return color;
    }

    private void registerTextFieldListeners() {
        final DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent event) {
                updateObservationValues();
            }

            @Override
            public void removeUpdate(final DocumentEvent event) {
                updateObservationValues();
            }

            @Override
            public void changedUpdate(final DocumentEvent event) {
                updateObservationValues();
            }
        };

        locationField.getTextField().getDocument().addDocumentListener(listener);
        dateField.getDocument().addDocumentListener(listener);
        timeField.getDocument().addDocumentListener(listener);
    }

    private void updateObservationValues() {
        if (!updatingFields) {
            viewModel.setDisplayedLocation(locationField.getText());
            viewModel.setDisplayedDate(dateField.getText());
            viewModel.setDisplayedTime(timeField.getText());
        }
    }

    /**
     * Builds the sky for the chosen place and moment.
     *
     * <p>Runs off the event dispatch thread because the interactor reaches the ephemeris service
     * over the network, which would otherwise freeze the window until it answered. The presenter
     * writes to the view model from that worker, so the property change handler hops back to the
     * dispatch thread before touching Swing.
     */
    private void handleViewSky() {
        viewSkyButton.setEnabled(false);
        new Thread(() -> {
            try {
                viewSkyController.viewSky(
                        locationField.getSelectedLocation(),
                        viewModel.getDisplayedDate(),
                        viewModel.getDisplayedTime());
            }
            finally {
                SwingUtilities.invokeLater(() -> viewSkyButton.setEnabled(true));
            }
        }, "view-sky-worker").start();
    }

    private void handleCheckConditions() {
        final ObserverLocation location = locationField.getSelectedLocation();
        if (location == null) {
            checkConditionsViewModel.setErrorMessage(
                    "Choose a location from the suggestions before checking conditions.");
            return;
        }

        final LocalDateTime observationDateTime;
        try {
            observationDateTime = LocalDateTime.of(
                    LocalDate.parse(viewModel.getDisplayedDate()),
                    LocalTime.parse(viewModel.getDisplayedTime()));
        }
        catch (DateTimeParseException exception) {
            checkConditionsViewModel.setErrorMessage(
                    "Enter a valid date (yyyy-MM-dd) and time (HH:mm) before checking conditions.");
            return;
        }
        checkConditionsButton.setEnabled(false);
        new Thread(() -> {
            try {
                checkConditionsController.checkConditions(location, observationDateTime);
            }
            finally {
                SwingUtilities.invokeLater(() -> checkConditionsButton.setEnabled(true));
            }
        }, "check-conditions-worker").start();
    }

    private void handleRankForecastDays() {
        final ObserverLocation location = locationField.getSelectedLocation();
        if (location == null) {
            rankForecastErrorLabel.setText("Choose a location from the suggestions first.");
            return;
        }
        if (selectedForecastDates.isEmpty()) {
            rankForecastErrorLabel.setText("Select at least one date to rank.");
            return;
        }
        final List<LocalDate> datesToRank = new ArrayList<>(selectedForecastDates);
        Collections.sort(datesToRank);

        rankForecastButton.setEnabled(false);
        new Thread(() -> {
            try {
                rankForecastDaysController.rankForecastDays(location, datesToRank);
            }
            finally {
                SwingUtilities.invokeLater(() -> rankForecastButton.setEnabled(true));
            }
        }, "rank-forecast-days-worker").start();
    }

    private void setFieldText(final JTextField field, final String text) {
        if (!field.getText().equals(text)) {
            updatingFields = true;
            field.setText(text);
            updatingFields = false;
        }
    }

    /**
     * The nights the ranking panel starts on: tonight and the two after it.
     *
     * <p>Computed rather than fixed, because the weather service only forecasts forward. A hard
     * coded date is correct for a day and then silently ranks nothing.
     */
    private static List<LocalDate> defaultForecastDates() {
        final LocalDate today = LocalDate.now();
        final List<LocalDate> dates = new ArrayList<>(DEFAULT_RANK_FORECAST_NIGHTS);
        for (int offset = 0; offset < DEFAULT_RANK_FORECAST_NIGHTS; offset++) {
            dates.add(today.plusDays(offset));
        }
        return dates;
    }

    private String textOrPlaceholder(final String text, final String placeholder) {
        if (text == null || text.isBlank()) {
            return placeholder;
        }
        return text;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() == checkConditionsViewModel) {
            // Presenter updates may arrive on a background thread (see handleCheckConditions()),
            // so defer the Swing mutation to the Event Dispatch Thread.
            SwingUtilities.invokeLater(this::updateWeatherFromViewModel);
            return;
        }
        if (event.getSource() == rankForecastDaysViewModel) {
            // Presenter updates may arrive on a background thread (see handleRankForecastDays()),
            // so defer the Swing mutation to the Event Dispatch Thread.
            SwingUtilities.invokeLater(this::updateRankedDaysFromViewModel);
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            // The sky presenter writes from the worker started in handleViewSky(), so re-enter on
            // the Event Dispatch Thread before any of the branches below touch a component.
            SwingUtilities.invokeLater(() -> propertyChange(event));
            return;
        }
        if ("stars".equals(event.getPropertyName())) {
            skyMapPanel.setStars(viewModel.getStars());
        }
        else if ("displayedDate".equals(event.getPropertyName())) {
            setFieldText(dateField, viewModel.getDisplayedDate());
        }
        else if ("displayedTime".equals(event.getPropertyName())) {
            setFieldText(timeField, viewModel.getDisplayedTime());
        }
        else if ("selectedObjectName".equals(event.getPropertyName())) {
            objectNameLabel.setText(textOrPlaceholder(
                    viewModel.getSelectedObjectName(), "No object selected"));
        }
        else if ("selectedObjectDetails".equals(event.getPropertyName())) {
            objectDetailsArea.setText(textOrPlaceholder(
                    viewModel.getSelectedObjectDetails(), "Details unavailable"));
        }
        else if ("errorMessage".equals(event.getPropertyName())) {
            errorLabel.setText(viewModel.getErrorMessage());
        }
        revalidate();
        repaint();
    }
}
