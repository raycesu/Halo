package view;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import interface_adapter.rank_forecast_days.RankForecastDaysViewModel.RankedDayDisplayItem;

/** Scrollable list of ranked forecast days, one row per {@link RankedDayDisplayItem}. */
class RankedDaysListPanel extends JScrollPane {

    private static final int VISIBLE_ROW_COUNT = 4;

    private final DefaultListModel<RankedDayDisplayItem> listModel = new DefaultListModel<>();
    private final JList<RankedDayDisplayItem> list = new JList<>(listModel);

    RankedDaysListPanel() {
        list.setCellRenderer(new RankedDayCellRenderer());
        list.setVisibleRowCount(VISIBLE_ROW_COUNT);
        setViewportView(list);
    }

    void setItems(final List<RankedDayDisplayItem> items) {
        listModel.clear();
        for (final RankedDayDisplayItem item : items) {
            listModel.addElement(item);
        }
    }

    private static final class RankedDayCellRenderer extends DefaultListCellRenderer {

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
                        + " — " + item.getRatingText() + " (" + item.getOverallScoreText() + ")");
                if (!isSelected) {
                    setForeground(SwingStyle.colorOrDefault(item.getRatingColor()));
                }
            }
            return component;
        }
    }
}
