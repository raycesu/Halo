package view;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import entity.ObserverLocation;

/**
 * Owns the raw Swing widgets behind {@link CityAutocompleteField}: the text field, the suggestion
 * list and its backing model, and the popup that shows the list.
 *
 * <p>Split out purely to keep the number of distinct classes {@link CityAutocompleteField}
 * constructs small; the behaviour of the widgets is unchanged.
 */
final class CityAutocompleteWidgets {

    private final JTextField textField = new JTextField();
    private final DefaultListModel<ObserverLocation> suggestionModel = new DefaultListModel<>();
    private final JList<ObserverLocation> suggestionList = new JList<>(suggestionModel);
    private final JPopupMenu popup = new JPopupMenu();

    CityAutocompleteWidgets() {
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setCellRenderer(new LocationCellRenderer());

        final JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setBorder(null);

        // A focusable popup would pull focus off the field on every keystroke, which stops the
        // user from typing into it at all.
        popup.setFocusable(false);
        popup.setBorder(null);
        popup.add(scrollPane);
    }

    JTextField getTextField() {
        return textField;
    }

    DefaultListModel<ObserverLocation> getSuggestionModel() {
        return suggestionModel;
    }

    JList<ObserverLocation> getSuggestionList() {
        return suggestionList;
    }

    JPopupMenu getPopup() {
        return popup;
    }

    /** Renders a suggestion as the label the dataset built, e.g. "Toronto, Ontario, Canada". */
    private static final class LocationCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                final JList<?> list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {

            final Component component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value instanceof ObserverLocation) {
                setText(((ObserverLocation) value).getDisplayName());
            }
            return component;
        }
    }
}
