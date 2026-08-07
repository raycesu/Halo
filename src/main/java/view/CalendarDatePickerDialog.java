package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Modal calendar widget that lets the user click individual days on/off to build the
 * set of dates passed to the "Rank Forecast Days" use case. Purely a Swing presentation
 * component: it holds no astronomy or weather logic and only exposes plain LocalDate values.
 */
final class CalendarDatePickerDialog extends JDialog {

    private static final int CALENDAR_COLUMNS = 7;
    private static final int DIALOG_BORDER_PADDING = 12;
    private static final int DIALOG_WIDTH = 340;
    private static final int DIALOG_HEIGHT = 360;
    private static final int HEADER_FONT_SIZE = 16;
    private static final int WEEKDAY_FONT_SIZE = 12;
    private static final int CONTENT_VERTICAL_GAP = 10;
    private static final Color SELECTED_BACKGROUND = SwingStyle.rgb(66, 133, 244);
    private static final Color SELECTED_FOREGROUND = Color.WHITE;
    private static final Color UNSELECTED_FOREGROUND = Color.BLACK;
    private static final DayOfWeek[] WEEK_STARTING_MONDAY = {
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
    };

    private final Set<LocalDate> selectedDates;
    private final JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel daysPanel = new JPanel(SwingStyle.grid(0, CALENDAR_COLUMNS, 4, 4));
    private YearMonth displayedMonth;

    CalendarDatePickerDialog(final Window owner, final Set<LocalDate> initiallySelectedDates) {
        super(owner, "Select Dates to Rank", ModalityType.APPLICATION_MODAL);
        this.selectedDates = new TreeSet<>(initiallySelectedDates);
        if (this.selectedDates.isEmpty()) {
            this.displayedMonth = YearMonth.now();
        }
        else {
            this.displayedMonth = YearMonth.from(this.selectedDates.iterator().next());
        }

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(SwingStyle.border(0, CONTENT_VERTICAL_GAP));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(
                DIALOG_BORDER_PADDING, DIALOG_BORDER_PADDING,
                DIALOG_BORDER_PADDING, DIALOG_BORDER_PADDING));
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        renderMonth();
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private JPanel createHeaderPanel() {
        final JPanel headerPanel = new JPanel(SwingStyle.border());
        final JButton previousMonthButton = new JButton("<");
        final JButton nextMonthButton = new JButton(">");
        previousMonthButton.addActionListener(event -> {
            displayedMonth = displayedMonth.minusMonths(1);
            renderMonth();
        });
        nextMonthButton.addActionListener(event -> {
            displayedMonth = displayedMonth.plusMonths(1);
            renderMonth();
        });
        monthYearLabel.setFont(SwingStyle.sansSerifFont(Font.BOLD, HEADER_FONT_SIZE));
        headerPanel.add(previousMonthButton, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextMonthButton, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createFooterPanel() {
        final JPanel footerPanel = new JPanel(SwingStyle.flow(FlowLayout.CENTER, 8, 0));
        final JButton clearButton = new JButton("Clear");
        final JButton doneButton = new JButton("Done");
        clearButton.addActionListener(event -> {
            selectedDates.clear();
            renderMonth();
        });
        doneButton.addActionListener(event -> dispose());
        footerPanel.add(clearButton);
        footerPanel.add(doneButton);
        return footerPanel;
    }

    private void renderMonth() {
        monthYearLabel.setText(
                displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + " " + displayedMonth.getYear());
        daysPanel.removeAll();

        for (final DayOfWeek dayOfWeek : WEEK_STARTING_MONDAY) {
            final JLabel dayOfWeekLabel = new JLabel(
                    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    SwingConstants.CENTER);
            dayOfWeekLabel.setFont(SwingStyle.sansSerifFont(Font.BOLD, WEEKDAY_FONT_SIZE));
            daysPanel.add(dayOfWeekLabel);
        }

        final LocalDate firstOfMonth = displayedMonth.atDay(1);
        final int leadingBlankDays = (firstOfMonth.getDayOfWeek().getValue() - 1 + 7) % 7;
        for (int blankIndex = 0; blankIndex < leadingBlankDays; blankIndex++) {
            daysPanel.add(new JLabel(""));
        }

        final int daysInMonth = displayedMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            daysPanel.add(createDayButton(displayedMonth.atDay(day)));
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private JToggleButton createDayButton(final LocalDate date) {
        final JToggleButton dayButton = new JToggleButton(String.valueOf(date.getDayOfMonth()));
        dayButton.setMargin(GridBagStyle.insets(2, 2, 2, 2));
        dayButton.setSelected(selectedDates.contains(date));
        applyDayButtonStyle(dayButton);

        // The weather API has no data for dates before today, so past days cannot be selected.
        if (date.isBefore(LocalDate.now())) {
            dayButton.setSelected(false);
            dayButton.setEnabled(false);
            selectedDates.remove(date);
            return dayButton;
        }

        dayButton.addActionListener(event -> {
            if (dayButton.isSelected()) {
                selectedDates.add(date);
            }
            else {
                selectedDates.remove(date);
            }
            applyDayButtonStyle(dayButton);
        });
        return dayButton;
    }

    private void applyDayButtonStyle(final JToggleButton dayButton) {
        if (dayButton.isSelected()) {
            dayButton.setBackground(SELECTED_BACKGROUND);
            dayButton.setForeground(SELECTED_FOREGROUND);
        }
        else {
            dayButton.setBackground(null);
            dayButton.setForeground(UNSELECTED_FOREGROUND);
        }
    }

    Set<LocalDate> getSelectedDates() {
        return new TreeSet<>(selectedDates);
    }
}
