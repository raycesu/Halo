package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import interface_adapter.rank_forecast_days.RankForecastDaysViewModel.RankedDayDisplayItem;

/**
 * Owns the pop-up "Forecast Ranking" dialog: its widgets and its lazy construction, showing,
 * and hiding. {@link SkyView} keeps the actual ranking business logic (tracking the selected
 * dates, calling the controller when the button is clicked) and reads/writes the widgets
 * exposed here.
 */
class ForecastRankingDialogController {

    private static final int DIALOG_WIDTH = 320;
    private static final int DIALOG_HEIGHT = 380;
    private static final int SUMMARY_LABEL_FONT_SIZE = 12;
    private static final int ERROR_COLOR_RED = 180;
    private static final int ERROR_COLOR_GREEN = 30;
    private static final int ERROR_COLOR_BLUE = 30;
    private static final int SECTION_SPACING = 6;

    private final Component owner;
    private final Color backgroundColor;
    private final JButton selectDatesButton = new JButton("Select Dates");
    private final JLabel selectedDatesSummaryLabel = new JLabel();
    private final JButton rankForecastButton = new JButton("Rank Nights");
    private final RankedDaysListPanel rankedDaysListPanel = new RankedDaysListPanel();
    private final JLabel rankForecastErrorLabel = new JLabel();
    private JDialog dialog;

    ForecastRankingDialogController(final Component owner, final Color backgroundColor) {
        this.owner = owner;
        this.backgroundColor = backgroundColor;
    }

    JButton getSelectDatesButton() {
        return selectDatesButton;
    }

    JButton getRankForecastButton() {
        return rankForecastButton;
    }

    void setSelectedDatesSummary(final String text, final String tooltip) {
        selectedDatesSummaryLabel.setText(text);
        selectedDatesSummaryLabel.setToolTipText(tooltip);
    }

    void setRankedDays(final List<RankedDayDisplayItem> items) {
        rankedDaysListPanel.setItems(items);
    }

    void setErrorText(final String text) {
        rankForecastErrorLabel.setText(text);
    }

    void toggle() {
        final JDialog dialogInstance = dialogInstance();
        dialogInstance.setVisible(!dialogInstance.isVisible());
    }

    /**
     * Package-private so {@code SkyViewTest} can reach the buttons inside the popup dialog,
     * which is a top-level window and therefore not part of the enclosing panel's component
     * tree.
     *
     * @return the lazily-created forecast ranking dialog
     */
    JDialog dialogInstance() {
        if (dialog == null) {
            final Window windowOwner = SwingUtilities.getWindowAncestor(owner);
            final JDialog newDialog =
                    new JDialog(windowOwner, "Forecast Ranking", Dialog.ModalityType.MODELESS);
            newDialog.setContentPane(createContentPanel());
            newDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
            newDialog.setLocationRelativeTo(owner);
            dialog = newDialog;
        }
        return dialog;
    }

    private JPanel createContentPanel() {
        final JPanel forecastPanel = new JPanel(SwingStyle.border(0, 8));
        forecastPanel.setBackground(backgroundColor);
        forecastPanel.setBorder(BorderFactory.createTitledBorder("Forecast Ranking"));

        final JPanel dateSelectionRow = new JPanel(SwingStyle.border(6, 0));
        dateSelectionRow.setOpaque(false);
        dateSelectionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedDatesSummaryLabel.setFont(
                SwingStyle.sansSerifFont(Font.PLAIN, SUMMARY_LABEL_FONT_SIZE));
        dateSelectionRow.add(selectedDatesSummaryLabel, BorderLayout.CENTER);
        dateSelectionRow.add(selectDatesButton, BorderLayout.EAST);

        rankForecastButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        rankForecastErrorLabel.setForeground(
                SwingStyle.rgb(ERROR_COLOR_RED, ERROR_COLOR_GREEN, ERROR_COLOR_BLUE));
        rankForecastErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        SwingStyle.stackVertically(topSection);
        topSection.add(dateSelectionRow);
        topSection.add(Box.createRigidArea(SwingStyle.size(0, SECTION_SPACING)));
        topSection.add(rankForecastButton);
        topSection.add(Box.createRigidArea(SwingStyle.size(0, SECTION_SPACING)));
        topSection.add(rankForecastErrorLabel);

        forecastPanel.add(topSection, BorderLayout.NORTH);
        forecastPanel.add(rankedDaysListPanel, BorderLayout.CENTER);
        return forecastPanel;
    }
}
