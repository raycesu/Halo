package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import interface_adapter.view_sky.SkyViewModel;

public class SkyView extends JPanel implements PropertyChangeListener {

    private final SkyViewModel viewModel;
    private final JTextField locationField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JLabel objectNameLabel = new JLabel();
    private final JTextArea objectDetailsArea = new JTextArea();
    private final JTextArea weatherArea = new JTextArea();
    private final JLabel errorLabel = new JLabel();
    private boolean updatingFields;

    public SkyView(final SkyViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        add(createLeftPanel(), BorderLayout.WEST);
        add(new SkyMapPanel(), BorderLayout.CENTER);
        add(createRightSidebar(), BorderLayout.EAST);

        updateFromViewModel();
        registerTextFieldListeners();
        viewModel.addPropertyChangeListener(this);
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
        leftPanel.add(errorLabel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(constellationPanel);
        return leftPanel;
    }

    private JPanel createRightSidebar() {
        final JPanel sidebar = new JPanel(new GridLayout(2, 1, 0, 15));
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

        final JPanel weatherPanel = new JPanel(new BorderLayout());
        weatherPanel.setBackground(sidebar.getBackground());
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Weather"));
        configureTextArea(weatherArea);
        weatherPanel.add(weatherArea, BorderLayout.CENTER);

        sidebar.add(starPanel);
        sidebar.add(weatherPanel);
        return sidebar;
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
        locationField.setText(viewModel.getDisplayedLocation());
        dateField.setText(viewModel.getDisplayedDate());
        timeField.setText(viewModel.getDisplayedTime());
        objectNameLabel.setText(textOrPlaceholder(
                viewModel.getSelectedObjectName(), "No object selected"));
        objectDetailsArea.setText(textOrPlaceholder(
                viewModel.getSelectedObjectDetails(), "Details unavailable"));
        weatherArea.setText(textOrPlaceholder(
                viewModel.getWeatherOrObservabilityText(), "Weather unavailable"));
        errorLabel.setText(viewModel.getErrorMessage());
        revalidate();
        repaint();
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

        locationField.getDocument().addDocumentListener(listener);
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

    private void setFieldText(final JTextField field, final String text) {
        if (!field.getText().equals(text)) {
            updatingFields = true;
            field.setText(text);
            updatingFields = false;
        }
    }

    private String textOrPlaceholder(final String text, final String placeholder) {
        if (text == null || text.isBlank()) {
            return placeholder;
        }
        return text;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if ("displayedLocation".equals(event.getPropertyName())) {
            setFieldText(locationField, viewModel.getDisplayedLocation());
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
        else if ("weatherOrObservabilityText".equals(event.getPropertyName())) {
            weatherArea.setText(textOrPlaceholder(
                    viewModel.getWeatherOrObservabilityText(), "Weather unavailable"));
        }
        else if ("errorMessage".equals(event.getPropertyName())) {
            errorLabel.setText(viewModel.getErrorMessage());
        }
        revalidate();
        repaint();
    }
}
