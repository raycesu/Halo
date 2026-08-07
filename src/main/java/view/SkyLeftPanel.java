package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The left-hand column of {@link SkyView}: the search box, the current observation summary
 * (location/date/time), the "Change Observation" button, and the custom-constellation trigger.
 *
 * <p>This class only builds and exposes the widgets; {@link SkyView} keeps all of the
 * search/constellation business logic and wires listeners to the widgets returned here.
 */
class SkyLeftPanel extends JPanel {

    private static final int PANEL_WIDTH = 260;
    private static final int FIELD_WIDTH = 190;
    private static final int FIELD_HEIGHT = 28;
    private static final int PANEL_BORDER_VERTICAL = 20;
    private static final int PANEL_BORDER_HORIZONTAL = 15;
    private static final int BACKGROUND_RED = 238;
    private static final int BACKGROUND_GREEN = 241;
    private static final int BACKGROUND_BLUE = 246;
    private static final int ERROR_RED = 180;
    private static final int ERROR_GREEN = 30;
    private static final int ERROR_BLUE = 30;
    private static final int SEARCH_HEADING_FONT_SIZE = 18;
    private static final int OBSERVATION_HEADING_FONT_SIZE = 20;
    private static final int GAP_AFTER_SEARCH_HEADING = 8;
    private static final int GAP_BEFORE_OBSERVATION_HEADING = 30;
    private static final int GAP_AFTER_OBSERVATION_HEADING = 18;
    private static final int GAP_AFTER_CHANGE_OBSERVATION_BUTTON = 12;
    private static final int GAP_BEFORE_CONSTELLATION_STATUS = 8;
    private static final int GAP_LABEL_TO_FIELD = 5;
    private static final int GAP_AFTER_FIELD_ROW = 12;

    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Search");
    private final JTextField locationField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JButton changeObservationButton = new JButton("Change Observation");
    private final JLabel errorLabel = new JLabel();
    private final JButton constellationButton = new JButton("Constellation");
    private final JLabel constellationStatusLabel = new JLabel();

    SkyLeftPanel() {
        SwingStyle.stackVertically(this);
        setPreferredSize(SwingStyle.size(PANEL_WIDTH, 0));
        setBackground(SwingStyle.rgb(BACKGROUND_RED, BACKGROUND_GREEN, BACKGROUND_BLUE));
        setBorder(BorderFactory.createEmptyBorder(
                PANEL_BORDER_VERTICAL, PANEL_BORDER_HORIZONTAL,
                PANEL_BORDER_VERTICAL, PANEL_BORDER_HORIZONTAL));

        configureSearchWidgets();
        configureObservationFields();
        configureConstellationWidgets();
        layoutComponents();
    }

    private void configureSearchWidgets() {
        searchButton.setEnabled(false);
        searchField.setName("searchField");
    }

    private JPanel createSearchPanel() {
        final JPanel searchPanel = new JPanel(SwingStyle.border(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        searchPanel.setMaximumSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        searchPanel.setAlignmentX(LEFT_ALIGNMENT);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        return searchPanel;
    }

    private void configureObservationFields() {
        locationField.setPreferredSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        locationField.setMaximumSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        dateField.setPreferredSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        dateField.setMaximumSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        timeField.setPreferredSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        timeField.setMaximumSize(SwingStyle.size(FIELD_WIDTH, FIELD_HEIGHT));
        locationField.setEditable(false);
        dateField.setEditable(false);
        timeField.setEditable(false);
        locationField.setName("locationDisplayField");
        dateField.setName("dateDisplayField");
        timeField.setName("timeDisplayField");
        locationField.setAlignmentX(LEFT_ALIGNMENT);
        dateField.setAlignmentX(LEFT_ALIGNMENT);
        timeField.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void configureConstellationWidgets() {
        errorLabel.setForeground(SwingStyle.rgb(ERROR_RED, ERROR_GREEN, ERROR_BLUE));
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        constellationButton.setEnabled(false);
        constellationStatusLabel.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JPanel createConstellationPanel() {
        final JPanel constellationPanel = new JPanel(SwingStyle.flow(FlowLayout.CENTER, 0, 0));
        constellationPanel.setOpaque(false);
        constellationPanel.setAlignmentX(LEFT_ALIGNMENT);
        constellationPanel.setMaximumSize(SwingStyle.size(
                Integer.MAX_VALUE, constellationButton.getPreferredSize().height));
        constellationPanel.add(constellationButton);
        return constellationPanel;
    }

    private JLabel createSearchHeading() {
        final JLabel searchHeading = new JLabel("Search");
        searchHeading.setFont(SwingStyle.sansSerifFont(Font.BOLD, SEARCH_HEADING_FONT_SIZE));
        searchHeading.setAlignmentX(LEFT_ALIGNMENT);
        return searchHeading;
    }

    private JLabel createObservationHeading() {
        final JLabel heading = new JLabel("Observation");
        heading.setFont(SwingStyle.sansSerifFont(Font.BOLD, OBSERVATION_HEADING_FONT_SIZE));
        heading.setAlignmentX(LEFT_ALIGNMENT);
        return heading;
    }

    private void layoutComponents() {
        add(createSearchHeading());
        add(Box.createRigidArea(SwingStyle.size(0, GAP_AFTER_SEARCH_HEADING)));
        add(createSearchPanel());
        add(Box.createRigidArea(SwingStyle.size(0, GAP_BEFORE_OBSERVATION_HEADING)));
        add(createObservationHeading());
        add(Box.createRigidArea(SwingStyle.size(0, GAP_AFTER_OBSERVATION_HEADING)));
        addLabeledField("Location", locationField);
        addLabeledField("Date", dateField);
        addLabeledField("Time", timeField);
        changeObservationButton.setAlignmentX(LEFT_ALIGNMENT);
        add(changeObservationButton);
        add(Box.createRigidArea(SwingStyle.size(0, GAP_AFTER_CHANGE_OBSERVATION_BUTTON)));
        add(errorLabel);
        add(Box.createVerticalGlue());
        add(createConstellationPanel());
        add(Box.createRigidArea(SwingStyle.size(0, GAP_BEFORE_CONSTELLATION_STATUS)));
        add(constellationStatusLabel);
    }

    private void addLabeledField(final String label, final JTextField field) {
        add(new JLabel(label));
        add(Box.createRigidArea(SwingStyle.size(0, GAP_LABEL_TO_FIELD)));
        add(field);
        add(Box.createRigidArea(SwingStyle.size(0, GAP_AFTER_FIELD_ROW)));
    }

    JTextField getSearchField() {
        return searchField;
    }

    JButton getSearchButton() {
        return searchButton;
    }

    JTextField getLocationField() {
        return locationField;
    }

    JTextField getDateField() {
        return dateField;
    }

    JTextField getTimeField() {
        return timeField;
    }

    JButton getChangeObservationButton() {
        return changeObservationButton;
    }

    JLabel getErrorLabel() {
        return errorLabel;
    }

    JButton getConstellationButton() {
        return constellationButton;
    }

    JLabel getConstellationStatusLabel() {
        return constellationStatusLabel;
    }

    void setLocationText(final String text) {
        setTextIfChanged(locationField, text);
    }

    void setDateText(final String text) {
        setTextIfChanged(dateField, text);
    }

    void setTimeText(final String text) {
        setTextIfChanged(timeField, text);
    }

    private void setTextIfChanged(final JTextField field, final String text) {
        if (!field.getText().equals(text)) {
            field.setText(text);
        }
    }

    /**
     * Prompts the user for a new custom constellation's name.
     *
     * @return the name the user entered, or null if they cancel
     */
    String promptForConstellationName() {
        return JOptionPane.showInputDialog(this, "Enter a name for the constellation:");
    }
}
