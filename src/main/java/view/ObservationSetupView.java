package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import interface_adapter.view_sky.ObservationSetupViewModel;

public class ObservationSetupView extends JPanel implements PropertyChangeListener {

    private final ObservationSetupViewModel viewModel;
    private final JTextField locationField;
    private final JTextField dateField;
    private final JTextField timeField;
    private final JLabel errorLabel;

    public ObservationSetupView(final ObservationSetupViewModel viewModel) {
        this.viewModel = viewModel;
        locationField = new JTextField(viewModel.getLocation(), 20);
        dateField = new JTextField(viewModel.getDate(), 20);
        timeField = new JTextField(viewModel.getTime(), 20);
        errorLabel = new JLabel(viewModel.getErrorMessage());

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        final JLabel titleLabel = new JLabel("HALO", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 52));
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(60, 20, 25, 20));
        add(titleLabel, BorderLayout.NORTH);

        final JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(new JLabel("Location:"), constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        formPanel.add(locationField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        formPanel.add(new JLabel("Date:"), constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        formPanel.add(dateField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        formPanel.add(new JLabel("Time:"), constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        formPanel.add(timeField, constraints);

        final JButton viewSkyButton = new JButton("View Sky");
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

        viewModel.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        locationField.setText(viewModel.getLocation());
        dateField.setText(viewModel.getDate());
        timeField.setText(viewModel.getTime());
        errorLabel.setText(viewModel.getErrorMessage());
    }
}
