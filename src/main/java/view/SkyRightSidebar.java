package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * The right-hand sidebar of {@link SkyView}: the selected star/object information box and the
 * weather box with its two trigger buttons ("Check Conditions", "Forecast Ranking").
 *
 * <p>This class only builds and exposes the widgets; {@link SkyView} keeps all of the
 * weather/forecast business logic and wires listeners to the widgets returned here.
 */
class SkyRightSidebar extends JPanel {

    static final Color BACKGROUND = SwingStyle.rgb(238, 241, 246);

    private static final int PANEL_WIDTH = 260;
    private static final int SECTION_ROW_GAP = 15;
    private static final int PANEL_BORDER_TOP_BOTTOM = 20;
    private static final int PANEL_BORDER_LEFT_RIGHT = 15;
    private static final int OBJECT_NAME_FONT_SIZE = 18;
    private static final int TEXT_AREA_FONT_SIZE = 14;
    private static final int TEXT_AREA_PADDING = 5;

    private final JLabel objectNameLabel = new JLabel();
    private final JTextArea objectDetailsArea = new JTextArea();
    private final JTextArea weatherArea = new JTextArea();
    private final JButton checkConditionsButton = new JButton("Check Conditions");
    private final JButton forecastRankingButton = new JButton("Forecast Ranking");

    SkyRightSidebar() {
        setLayout(SwingStyle.grid(2, 1, 0, SECTION_ROW_GAP));
        setPreferredSize(SwingStyle.size(PANEL_WIDTH, 0));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(
                PANEL_BORDER_TOP_BOTTOM, PANEL_BORDER_LEFT_RIGHT,
                PANEL_BORDER_TOP_BOTTOM, PANEL_BORDER_LEFT_RIGHT));

        final JPanel starPanel = new JPanel(SwingStyle.border(0, 10));
        starPanel.setBackground(getBackground());
        starPanel.setBorder(BorderFactory.createTitledBorder("Star Information"));
        objectNameLabel.setFont(SwingStyle.sansSerifFont(Font.BOLD, OBJECT_NAME_FONT_SIZE));
        configureTextArea(objectDetailsArea);
        starPanel.add(objectNameLabel, BorderLayout.NORTH);
        starPanel.add(objectDetailsArea, BorderLayout.CENTER);

        final JPanel weatherPanel = new JPanel(SwingStyle.border(0, 8));
        weatherPanel.setBackground(getBackground());
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Weather"));
        configureTextArea(weatherArea);
        weatherPanel.add(weatherArea, BorderLayout.CENTER);

        final JPanel weatherButtonColumn = new JPanel(SwingStyle.grid(2, 1, 0, 8));
        weatherButtonColumn.setOpaque(false);
        weatherButtonColumn.add(checkConditionsButton);
        weatherButtonColumn.add(forecastRankingButton);
        weatherPanel.add(weatherButtonColumn, BorderLayout.SOUTH);

        add(starPanel);
        add(weatherPanel);
    }

    private void configureTextArea(final JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setFont(SwingStyle.sansSerifFont(Font.PLAIN, TEXT_AREA_FONT_SIZE));
        textArea.setBorder(BorderFactory.createEmptyBorder(
                TEXT_AREA_PADDING, TEXT_AREA_PADDING, TEXT_AREA_PADDING, TEXT_AREA_PADDING));
    }

    JLabel getObjectNameLabel() {
        return objectNameLabel;
    }

    JTextArea getObjectDetailsArea() {
        return objectDetailsArea;
    }

    JTextArea getWeatherArea() {
        return weatherArea;
    }

    JButton getCheckConditionsButton() {
        return checkConditionsButton;
    }

    JButton getForecastRankingButton() {
        return forecastRankingButton;
    }
}
