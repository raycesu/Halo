package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

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
    private static final int COLOR_CIRCLE_SIZE = 28;
    private static final int COLOR_CIRCLE_INSET = 4;
    private static final String[] CONSTELLATION_COLOR_HEX_VALUES = {
        "#50B4FF", "#FF5C5C", "#FF9F43", "#FFD93D", "#55D187", "#B388FF",
    };

    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Search");
    private final JTextField locationField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JButton changeObservationButton = new JButton("Change Observation");
    private final JLabel errorLabel = new JLabel();
    private final JButton constellationButton = new JButton("Custom Constellation");
    private final JLabel constellationStatusLabel = new JLabel();
    private String selectedConstellationColorHex = CONSTELLATION_COLOR_HEX_VALUES[0];

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

    String promptForConstellationNameAndColor() {
        final JTextField nameField = new JTextField();
        final ButtonGroup colorGroup = new ButtonGroup();
        final JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        for (int index = 0; index < CONSTELLATION_COLOR_HEX_VALUES.length; index++) {
            final String colorHex = CONSTELLATION_COLOR_HEX_VALUES[index];
            final ColorChoiceButton colorButton =
                    new ColorChoiceButton(Color.decode(colorHex));
            colorButton.setActionCommand(colorHex);
            colorButton.setSelected(index == 0);
            colorGroup.add(colorButton);
            colorPanel.add(colorButton);
        }
        final Object[] dialogContents = {
            "Constellation name:",
            nameField,
            "Line color:",
            colorPanel,
        };

        final int result = JOptionPane.showConfirmDialog(
                this,
                dialogContents,
                "Save Custom Constellation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        String name = null;
        if (result == JOptionPane.OK_OPTION) {
            name = nameField.getText();
            selectedConstellationColorHex = colorGroup.getSelection().getActionCommand();
        }
        return name;
    }

    String getSelectedConstellationColorHex() {
        return selectedConstellationColorHex;
    }

    private static final class ColorChoiceButton extends JToggleButton {
        private final Color circleColor;

        ColorChoiceButton(final Color circleColor) {
            this.circleColor = circleColor;
            setPreferredSize(SwingStyle.size(COLOR_CIRCLE_SIZE, COLOR_CIRCLE_SIZE));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D graphics2D = (Graphics2D) graphics.create();
            try {
                graphics2D.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                final int diameter = Math.min(getWidth(), getHeight()) - COLOR_CIRCLE_INSET * 2;
                final int x = (getWidth() - diameter) / 2;
                final int y = (getHeight() - diameter) / 2;
                graphics2D.setColor(circleColor);
                graphics2D.fillOval(x, y, diameter, diameter);
                if (isSelected()) {
                    graphics2D.setColor(Color.DARK_GRAY);
                    graphics2D.setStroke(new BasicStroke(2.0F));
                    graphics2D.drawOval(x, y, diameter, diameter);
                }
            }
            finally {
                graphics2D.dispose();
            }
        }
    }
}
