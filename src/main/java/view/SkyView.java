package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import entity.Star;
import interface_adapter.ViewManagerModel;
import interface_adapter.check_conditions.CheckConditionsController;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationController;
import interface_adapter.custom_constellation.ConstellationViewModel;
import interface_adapter.rank_forecast_days.RankForecastDaysController;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import interface_adapter.view_sky.SkyViewModel;

public class SkyView extends JPanel implements ActionListener, PropertyChangeListener {

    private static final int DEFAULT_RANK_FORECAST_DAY_COUNT = 3;
    private static final Color CONSTELLATION_ERROR_COLOR = SwingStyle.rgb(180, 30, 30);
    private static final Color CONSTELLATION_SUCCESS_COLOR = SwingStyle.rgb(20, 130, 60);
    private static final int OBSERVATION_SETUP_WIDTH = 900;
    private static final int OBSERVATION_SETUP_HEIGHT = 600;

    private final SkyViewModel viewModel;
    private final CheckConditionsController checkConditionsController;
    private final CheckConditionsViewModel checkConditionsViewModel;
    private final RankForecastDaysController rankForecastDaysController;
    private final RankForecastDaysViewModel rankForecastDaysViewModel;
    private final ViewManagerModel viewManagerModel;
    private final SkyMapPanel skyMapPanel = new SkyMapPanel();
    private final SkyLeftPanel leftPanel = new SkyLeftPanel();
    private final SkyRightSidebar rightSidebar = new SkyRightSidebar();
    private final ForecastRankingDialogController forecastRankingDialogController =
            new ForecastRankingDialogController(this, SkyRightSidebar.BACKGROUND);
    private final List<LocalDate> selectedForecastDates = defaultForecastDates();
    private final List<Star> constellationSelection = new ArrayList<>();
    private ConstellationController constellationController;
    private ConstellationViewModel constellationViewModel;
    private boolean creatingConstellation;

    public SkyView(
            final SkyViewModel viewModel,
            final CheckConditionsController checkConditionsController,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysController rankForecastDaysController,
            final RankForecastDaysViewModel rankForecastDaysViewModel,
            final ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.checkConditionsController = checkConditionsController;
        this.checkConditionsViewModel = checkConditionsViewModel;
        this.rankForecastDaysController = rankForecastDaysController;
        this.rankForecastDaysViewModel = rankForecastDaysViewModel;
        this.viewManagerModel = viewManagerModel;
        setLayout(SwingStyle.border());
        setBackground(Color.BLACK);

        add(leftPanel, BorderLayout.WEST);
        add(skyMapPanel, BorderLayout.CENTER);
        add(rightSidebar, BorderLayout.EAST);

        skyMapPanel.setSelectionListener(this::handleObjectSelected);
        updateFromViewModel();
        updateSelectedDatesSummaryLabel();
        registerActions();
        viewModel.addPropertyChangeListener(this);
        checkConditionsViewModel.addPropertyChangeListener(this);
        rankForecastDaysViewModel.addPropertyChangeListener(this);
    }

    /**
     * Wires the custom-constellation feature into this view.
     *
     * @param controller the controller to forward constellation creation requests to
     * @param newConstellationViewModel the view model holding saved custom constellations
     */
    public void configureCustomConstellations(
            final ConstellationController controller,
            final ConstellationViewModel newConstellationViewModel) {
        this.constellationController = controller;
        this.constellationViewModel = newConstellationViewModel;

        newConstellationViewModel.addPropertyChangeListener(this);
        skyMapPanel.setCustomConstellations(newConstellationViewModel.getConstellations());
        leftPanel.getConstellationButton().setEnabled(!viewModel.getStars().isEmpty());
    }

    private void openDatePicker() {
        final var owner = SwingUtilities.getWindowAncestor(this);
        final CalendarDatePickerDialog datePickerDialog =
                new CalendarDatePickerDialog(owner, new TreeSet<>(selectedForecastDates));
        datePickerDialog.setVisible(true);

        selectedForecastDates.clear();
        selectedForecastDates.addAll(datePickerDialog.getSelectedDates());
        updateSelectedDatesSummaryLabel();
    }

    private static List<LocalDate> defaultForecastDates() {
        final LocalDate today = LocalDate.now();
        final List<LocalDate> defaultDates = new ArrayList<>(DEFAULT_RANK_FORECAST_DAY_COUNT);
        for (int offset = 0; offset < DEFAULT_RANK_FORECAST_DAY_COUNT; offset++) {
            defaultDates.add(today.plusDays(offset));
        }
        return defaultDates;
    }

    private void updateSelectedDatesSummaryLabel() {
        if (selectedForecastDates.isEmpty()) {
            forecastRankingDialogController.setSelectedDatesSummary("No dates selected", null);
        }
        else {
            final List<LocalDate> sortedDates = new ArrayList<>(selectedForecastDates);
            sortedDates.sort(null);
            final StringBuilder tooltipBuilder = new StringBuilder();
            for (int index = 0; index < sortedDates.size(); index++) {
                if (index > 0) {
                    tooltipBuilder.append(", ");
                }
                tooltipBuilder.append(sortedDates.get(index));
            }
            final String suffix;
            if (sortedDates.size() == 1) {
                suffix = " date selected";
            }
            else {
                suffix = " dates selected";
            }
            forecastRankingDialogController.setSelectedDatesSummary(
                    sortedDates.size() + suffix, tooltipBuilder.toString());
        }
    }

    private void updateFromViewModel() {
        leftPanel.getLocationField().setText(viewModel.getDisplayedLocation());
        leftPanel.getDateField().setText(viewModel.getDisplayedDate());
        leftPanel.getTimeField().setText(viewModel.getDisplayedTime());
        skyMapPanel.setStars(viewModel.getStars());
        skyMapPanel.setSelectedObject(viewModel.getSelectedObject());
        leftPanel.getSearchButton().setEnabled(!viewModel.getStars().isEmpty());
        rightSidebar.getObjectNameLabel().setText(textOrPlaceholder(
                viewModel.getSelectedObjectName(), "No object selected"));
        rightSidebar.getObjectDetailsArea().setText(textOrPlaceholder(
                viewModel.getSelectedObjectDetails(), "Details unavailable"));
        updateWeatherFromViewModel();
        leftPanel.getErrorLabel().setText(viewModel.getErrorMessage());
        updateRankedDaysFromViewModel();
        revalidate();
        repaint();
    }

    private void handleObjectSelected(final Star selectedObject) {
        if (creatingConstellation) {
            if (selectedObject != null && !constellationSelection.contains(selectedObject)) {
                constellationSelection.add(selectedObject);
                skyMapPanel.setConstellationSelection(constellationSelection);
                leftPanel.getConstellationStatusLabel().setText(
                        constellationSelection.size() + " stars selected");
            }
        }
        else {
            viewModel.setSelectedObject(selectedObject);
            viewModel.setSelectedObjectDetails(
                    StarDetailsFormatter.formatObjectDetails(selectedObject));
            if (selectedObject != null) {
                viewModel.setErrorMessage("");
            }
        }
    }

    private void updateRankedDaysFromViewModel() {
        forecastRankingDialogController.setRankedDays(rankForecastDaysViewModel.getRankedDays());
        forecastRankingDialogController.setErrorText(rankForecastDaysViewModel.getErrorMessage());
    }

    private void updateWeatherFromViewModel() {
        final String error = checkConditionsViewModel.getErrorMessage();
        if (!error.isBlank()) {
            rightSidebar.getWeatherArea().setForeground(Color.RED);
            rightSidebar.getWeatherArea().setText(error);
        }
        else {
            final String combinedText = String.join("\n",
                    checkConditionsViewModel.getCloudCoverText(),
                    checkConditionsViewModel.getVisibilityText(),
                    checkConditionsViewModel.getPrecipitationText(),
                    checkConditionsViewModel.getWeatherCodeText(),
                    checkConditionsViewModel.getOverallScoreText(),
                    checkConditionsViewModel.getRatingText());
            rightSidebar.getWeatherArea().setForeground(
                    SwingStyle.colorOrDefault(checkConditionsViewModel.getRatingColor()));
            rightSidebar.getWeatherArea().setText(textOrPlaceholder(
                    combinedText, "Click Check Conditions to see forecast conditions."));
        }
    }

    private void registerActions() {
        leftPanel.getSearchButton().addActionListener(this);
        leftPanel.getChangeObservationButton().addActionListener(this);
        rightSidebar.getCheckConditionsButton().addActionListener(this);
        rightSidebar.getForecastRankingButton().addActionListener(this);
        forecastRankingDialogController.getSelectDatesButton().addActionListener(this);
        forecastRankingDialogController.getRankForecastButton().addActionListener(this);
        leftPanel.getConstellationButton().addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final Object source = event.getSource();
        if (source == leftPanel.getSearchButton()) {
            handleSearch();
        }
        else if (source == leftPanel.getChangeObservationButton()) {
            viewManagerModel.setActiveView(ViewManagerModel.OBSERVATION_SETUP_VIEW);
            resizeWindow(OBSERVATION_SETUP_WIDTH, OBSERVATION_SETUP_HEIGHT);
        }
        else if (source == rightSidebar.getCheckConditionsButton()) {
            handleCheckConditions();
        }
        else if (source == rightSidebar.getForecastRankingButton()) {
            forecastRankingDialogController.toggle();
        }
        else if (source == forecastRankingDialogController.getSelectDatesButton()) {
            openDatePicker();
        }
        else if (source == forecastRankingDialogController.getRankForecastButton()) {
            handleRankForecastDays();
        }
        else if (source == leftPanel.getConstellationButton()) {
            handleConstellationAction();
        }
    }

    private void handleConstellationAction() {
        if (creatingConstellation) {
            saveConstellation();
        }
        else {
            startConstellationCreation();
        }
    }

    private void startConstellationCreation() {
        creatingConstellation = true;
        constellationSelection.clear();
        skyMapPanel.setConstellationSelection(constellationSelection);
        leftPanel.getConstellationButton().setText("Save Constellation");
        leftPanel.getConstellationStatusLabel().setForeground(Color.BLACK);
        leftPanel.getConstellationStatusLabel().setText("Select stars in order");
    }

    private void saveConstellation() {
        final String name = leftPanel.promptForConstellationName();

        // Press Cancel ends creation without saving
        if (name == null) {
            finishConstellationCreation();
        }
        else {
            constellationController.createConstellation(name, constellationSelection);
            // Keep creation mode active when validation fails
            if (constellationViewModel.getErrorMessage().isBlank()) {
                finishConstellationCreation();
            }
        }
    }

    private void finishConstellationCreation() {
        creatingConstellation = false;
        constellationSelection.clear();
        skyMapPanel.setConstellationSelection(List.of());
        leftPanel.getConstellationButton().setText("Custom Constellation");
    }

    private void handleSearch() {
        final String requestedName = leftPanel.getSearchField().getText().trim();
        if (requestedName.isEmpty()) {
            viewModel.setErrorMessage("Enter an object name to search.");
        }
        else {
            Star matchingObject = null;
            for (final Star object : viewModel.getStars()) {
                if (object.getDisplayName() != null
                        && object.getDisplayName().equalsIgnoreCase(requestedName)) {
                    matchingObject = object;
                    break;
                }
            }

            if (matchingObject == null) {
                viewModel.setErrorMessage(
                        "No displayed object matches \"" + requestedName + "\".");
            }
            else {
                handleObjectSelected(matchingObject);
            }
        }
    }

    private void handleCheckConditions() {
        final var location = viewModel.getObserverLocation();
        if (location == null) {
            checkConditionsViewModel.setErrorMessage(
                    "Set an observation location before checking conditions.");
        }
        else {
            final var observationDateTime = ObservationTimeParser.parse(
                    viewModel.getDisplayedDate(), viewModel.getDisplayedTime());
            if (observationDateTime == null) {
                checkConditionsViewModel.setErrorMessage(
                        "Enter a valid date (yyyy-MM-dd) and time (HH:mm) before checking conditions.");
            }
            else {
                rightSidebar.getCheckConditionsButton().setEnabled(false);
                new Thread(() -> {
                    try {
                        checkConditionsController.checkConditions(location, observationDateTime);
                    }
                    finally {
                        SwingUtilities.invokeLater(
                                () -> rightSidebar.getCheckConditionsButton().setEnabled(true));
                    }
                }, "check-conditions-worker").start();
            }
        }
    }

    private void handleRankForecastDays() {
        final var location = viewModel.getObserverLocation();
        if (location == null) {
            forecastRankingDialogController.setErrorText("Set an observation location first.");
        }
        else if (selectedForecastDates.isEmpty()) {
            forecastRankingDialogController.setErrorText("Select at least one date to rank.");
        }
        else {
            final List<LocalDate> datesToRank = new ArrayList<>(selectedForecastDates);
            datesToRank.sort(null);

            forecastRankingDialogController.getRankForecastButton().setEnabled(false);
            new Thread(() -> {
                try {
                    rankForecastDaysController.rankForecastDays(location, datesToRank);
                }
                finally {
                    SwingUtilities.invokeLater(() -> {
                        forecastRankingDialogController.getRankForecastButton().setEnabled(true);
                    });
                }
            }, "rank-forecast-days-worker").start();
        }
    }

    private void resizeWindow(final int width, final int height) {
        final var window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.setSize(width, height);
            window.setLocationRelativeTo(null);
        }
    }

    /**
     * Returns {@code text}, or {@code placeholder} when {@code text} is null or blank.
     *
     * @param text the preferred text
     * @param placeholder the fallback text
     * @return {@code text} if non-blank, otherwise {@code placeholder}
     */
    private String textOrPlaceholder(final String text, final String placeholder) {
        if (text == null || text.isBlank()) {
            return placeholder;
        }
        return text;
    }

    /**
     * Package-private so {@code SkyViewTest} can reach the buttons inside the popup dialog,
     * which is a top-level window and therefore not part of this panel's component tree.
     *
     * @return the forecast ranking dialog
     */
    JDialog getForecastRankingDialogForTesting() {
        return forecastRankingDialogController.dialogInstance();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(event));
        }
        else if (event.getSource() == constellationViewModel) {
            handleConstellationViewModelChange();
        }
        else if (event.getSource() == checkConditionsViewModel) {
            updateWeatherFromViewModel();
        }
        else if (event.getSource() == rankForecastDaysViewModel) {
            updateRankedDaysFromViewModel();
        }
        else {
            handleSkyViewModelPropertyChange(event.getPropertyName());
            revalidate();
            repaint();
        }
    }

    private void handleConstellationViewModelChange() {
        skyMapPanel.setCustomConstellations(constellationViewModel.getConstellations());
        if (!constellationViewModel.getErrorMessage().isBlank()) {
            leftPanel.getConstellationStatusLabel().setForeground(CONSTELLATION_ERROR_COLOR);
            leftPanel.getConstellationStatusLabel().setText(constellationViewModel.getErrorMessage());
        }
        else {
            leftPanel.getConstellationStatusLabel().setForeground(CONSTELLATION_SUCCESS_COLOR);
            leftPanel.getConstellationStatusLabel().setText(constellationViewModel.getSuccessMessage());
        }
    }

    private void handleSkyViewModelPropertyChange(final String propertyName) {
        if ("displayedLocation".equals(propertyName)) {
            leftPanel.setLocationText(viewModel.getDisplayedLocation());
        }
        else if ("displayedDate".equals(propertyName)) {
            leftPanel.setDateText(viewModel.getDisplayedDate());
        }
        else if ("displayedTime".equals(propertyName)) {
            leftPanel.setTimeText(viewModel.getDisplayedTime());
        }
        else if ("stars".equals(propertyName)) {
            skyMapPanel.setStars(viewModel.getStars());
            leftPanel.getSearchButton().setEnabled(!viewModel.getStars().isEmpty());
            leftPanel.getConstellationButton().setEnabled(constellationController != null
                    && !viewModel.getStars().isEmpty());
        }
        else if ("selectedObject".equals(propertyName)) {
            skyMapPanel.setSelectedObject(viewModel.getSelectedObject());
        }
        else if ("selectedObjectName".equals(propertyName)) {
            rightSidebar.getObjectNameLabel().setText(textOrPlaceholder(
                    viewModel.getSelectedObjectName(), "No object selected"));
        }
        else if ("selectedObjectDetails".equals(propertyName)) {
            rightSidebar.getObjectDetailsArea().setText(textOrPlaceholder(
                    viewModel.getSelectedObjectDetails(), "Details unavailable"));
        }
        else if ("errorMessage".equals(propertyName)) {
            leftPanel.getErrorLabel().setText(viewModel.getErrorMessage());
        }
    }
}
