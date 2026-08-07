package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.lookup_location.LookupLocationViewModel;
import interface_adapter.view_sky.ObservationSetupViewModel;

/**
 * The centred "HALO" title and location/date/time form for {@link ObservationSetupView}, split
 * out purely to keep the number of distinct classes {@code ObservationSetupView} depends on
 * small; the widgets, layout, and styling are unchanged.
 */
final class ObservationSetupFormPanel extends JPanel {

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
    private static final int DATE_ROW = 1;
    private static final int TIME_ROW = 2;
    private static final int BUTTON_ROW = 3;
    private static final int ERROR_ROW = 4;
    private static final int FIELD_COLUMNS = 20;

    private final CityAutocompleteField locationField;
    private final JTextField dateField;
    private final JTextField timeField;
    private final JButton viewSkyButton = new JButton("View Sky");
    private final JLabel errorLabel;

    ObservationSetupFormPanel(
            final ObservationSetupViewModel viewModel,
            final LookupLocationController lookupLocationController,
            final LookupLocationViewModel lookupLocationViewModel) {
        super(SwingStyle.border());
        setBackground(Color.WHITE);

        locationField = new CityAutocompleteField(lookupLocationController, lookupLocationViewModel);
        locationField.setName("locationField");
        locationField.getTextField().setName("locationTextField");
        locationField.setSelectedLocation(viewModel.getSelectedLocation());

        dateField = createField("dateField", viewModel.getDate());
        timeField = createField("timeField", viewModel.getTime());
        errorLabel = new JLabel(viewModel.getErrorMessage());
        viewSkyButton.setName("viewSkyButton");

        add(createTitleLabel(), BorderLayout.NORTH);
        add(createFormWrapper(), BorderLayout.CENTER);
    }

    CityAutocompleteField getLocationField() {
        return locationField;
    }

    JTextField getDateField() {
        return dateField;
    }

    JTextField getTimeField() {
        return timeField;
    }

    JButton getViewSkyButton() {
        return viewSkyButton;
    }

    JLabel getErrorLabel() {
        return errorLabel;
    }

    private JLabel createTitleLabel() {
        final JLabel titleLabel = new JLabel("HALO", SwingConstants.CENTER);
        titleLabel.setFont(SwingStyle.sansSerifFont(Font.BOLD, TITLE_FONT_SIZE));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(
                TITLE_BORDER_TOP, TITLE_BORDER_SIDE, TITLE_BORDER_BOTTOM, TITLE_BORDER_SIDE));
        return titleLabel;
    }

    private JPanel createFormWrapper() {
        final JPanel formPanel = new JPanel(GridBagStyle.layout());
        formPanel.setBackground(Color.WHITE);

        final GridBagConstraints constraints = GridBagStyle.constraints();
        constraints.insets = GridBagStyle.insets(
                FIELD_INSET, FIELD_INSET_HORIZONTAL, FIELD_INSET, FIELD_INSET_HORIZONTAL);
        constraints.anchor = GridBagConstraints.WEST;

        addFieldRow(formPanel, constraints, LOCATION_ROW, "Location:", locationField);
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

        final JPanel formWrapper = new JPanel(GridBagStyle.layout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.add(formPanel);
        return formWrapper;
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
}
