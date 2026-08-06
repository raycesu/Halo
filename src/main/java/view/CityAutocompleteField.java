package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import entity.ObserverLocation;
import interface_adapter.lookup_location.LookupLocationController;
import interface_adapter.lookup_location.LookupLocationViewModel;

/**
 * A place-name field that suggests matching cities as the user types and hands back the one they
 * pick, already resolved to coordinates and a time zone.
 *
 * <p>The point is that a typed name is never trusted. {@link #getSelectedLocation()} returns a
 * location only when the user has actually chosen one from the list, and editing the text after
 * choosing clears it again. A caller therefore cannot be handed a half-typed "Toront" that
 * silently resolves to somewhere else, which is the failure this component exists to prevent.
 *
 * <p>Suggestions are looked up on the event dispatch thread. That is safe here only because the
 * lookup is a scan of an in-memory list; a data access implementation backed by a web service
 * would need this moved onto a worker thread.
 */
public class CityAutocompleteField extends JPanel implements PropertyChangeListener {

    /** Enough suggestions to disambiguate the common cases without covering the form. */
    private static final int maxSuggestions = 8;

    /**
     * How wide the popup may grow beyond the field to fit a label.
     *
     * <p>It has to grow: labels read "City, Region, Country" so that eight Springfields can be
     * told apart, and at the width of the field they truncate to the part they all share.
     */
    private static final int maxPopupWidth = 380;

    private final LookupLocationController lookupLocationController;
    private final LookupLocationViewModel lookupLocationViewModel;
    private final JTextField textField = new JTextField();
    private final DefaultListModel<ObserverLocation> suggestionModel = new DefaultListModel<>();
    private final JList<ObserverLocation> suggestionList = new JList<>(suggestionModel);
    private final JPopupMenu popup = new JPopupMenu();

    /** The place the user picked, or null if the current text is not a confirmed choice. */
    private ObserverLocation selectedLocation;

    /** Suppresses the document listener while the field's text is being set in code. */
    private boolean updatingText;

    public CityAutocompleteField(
            final LookupLocationController lookupLocationController,
            final LookupLocationViewModel lookupLocationViewModel) {
        this.lookupLocationController = lookupLocationController;
        this.lookupLocationViewModel = lookupLocationViewModel;

        setLayout(new BorderLayout());
        setOpaque(false);
        add(textField, BorderLayout.CENTER);

        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setCellRenderer(new LocationCellRenderer());

        final JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setBorder(null);

        // A focusable popup would pull focus off the field on every keystroke, which stops the
        // user from typing into it at all.
        popup.setFocusable(false);
        popup.setBorder(null);
        popup.add(scrollPane);

        textField.getDocument().addDocumentListener(new SuggestionRefresher());
        textField.addKeyListener(new NavigationKeyListener());
        suggestionList.addMouseListener(new SuggestionClickListener());

        lookupLocationViewModel.addPropertyChangeListener(this);
    }

    /**
     * The place the user chose, or null if they have not chosen one or have edited the text since.
     */
    public ObserverLocation getSelectedLocation() {
        return selectedLocation;
    }

    /**
     * Sets the field to an already-resolved place, as when restoring a previous session, without
     * treating it as fresh typing.
     */
    public void setSelectedLocation(final ObserverLocation location) {
        selectedLocation = location;
        setTextQuietly(location == null ? "" : location.getDisplayName());
        hideSuggestions();
    }

    public String getText() {
        return textField.getText();
    }

    public JTextField getTextField() {
        return textField;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (LookupLocationViewModel.SUGGESTIONS_PROPERTY.equals(event.getPropertyName())) {
            applySuggestions(lookupLocationViewModel.getSuggestions());
        }
    }

    private void refreshSuggestions() {
        final String query = textField.getText();

        // Any edit invalidates an earlier choice: the text no longer necessarily names it.
        selectedLocation = null;

        lookupLocationController.lookupLocation(query, maxSuggestions);
    }

    private void applySuggestions(final List<ObserverLocation> matches) {
        suggestionModel.clear();
        for (final ObserverLocation match : matches) {
            suggestionModel.addElement(match);
        }

        if (suggestionModel.isEmpty()) {
            hideSuggestions();
        }
        else {
            showSuggestions();
        }
    }

    private void showSuggestions() {
        suggestionList.setSelectedIndex(0);
        suggestionList.setVisibleRowCount(Math.min(suggestionModel.size(), maxSuggestions));

        // Room for the scroll bar the list shows once there are more rows than fit.
        final Dimension size = suggestionList.getPreferredScrollableViewportSize();
        final int width = Math.min(
                maxPopupWidth, Math.max(textField.getWidth(), size.width + 24));
        popup.setPopupSize(new Dimension(width, size.height + 4));

        if (popup.isVisible()) {
            popup.revalidate();
            popup.repaint();
        }
        else if (textField.isShowing()) {
            popup.show(textField, 0, textField.getHeight());
        }
    }

    private void hideSuggestions() {
        popup.setVisible(false);
    }

    private void chooseHighlighted() {
        final ObserverLocation choice = suggestionList.getSelectedValue();
        if (choice != null) {
            selectedLocation = choice;
            setTextQuietly(choice.getDisplayName());
            hideSuggestions();
        }
    }

    /**
     * Moves the highlight by {@code offset}, staying inside the list rather than wrapping, so
     * holding an arrow key comes to rest at an end instead of cycling.
     */
    private void moveHighlight(final int offset) {
        if (!suggestionModel.isEmpty()) {
            final int lastIndex = suggestionModel.size() - 1;
            final int target = Math.max(
                    0, Math.min(lastIndex, suggestionList.getSelectedIndex() + offset));
            suggestionList.setSelectedIndex(target);
            suggestionList.ensureIndexIsVisible(target);
        }
    }

    /**
     * Replaces the text without the document listener treating it as the user typing, which would
     * clear {@link #selectedLocation} and reopen the popup the selection just closed.
     */
    private void setTextQuietly(final String text) {
        updatingText = true;
        try {
            textField.setText(text);
        }
        finally {
            updatingText = false;
        }
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

    private final class SuggestionRefresher implements DocumentListener {

        @Override
        public void insertUpdate(final DocumentEvent event) {
            scheduleRefresh();
        }

        @Override
        public void removeUpdate(final DocumentEvent event) {
            scheduleRefresh();
        }

        @Override
        public void changedUpdate(final DocumentEvent event) {
            scheduleRefresh();
        }

        /**
         * Defers the refresh, because a document listener may not modify the document it is
         * reacting to and the popup work reads it while Swing still holds the write lock.
         */
        private void scheduleRefresh() {
            if (!updatingText) {
                SwingUtilities.invokeLater(CityAutocompleteField.this::refreshSuggestions);
            }
        }
    }

    private final class NavigationKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(final KeyEvent event) {
            if (popup.isVisible()) {
                handleWhileOpen(event);
            }
            else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                // Reopen the list without needing another character typed.
                refreshSuggestions();
                event.consume();
            }
        }

        private void handleWhileOpen(final KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    moveHighlight(1);
                    event.consume();
                    break;
                case KeyEvent.VK_UP:
                    moveHighlight(-1);
                    event.consume();
                    break;
                case KeyEvent.VK_ENTER:
                    chooseHighlighted();
                    event.consume();
                    break;
                case KeyEvent.VK_ESCAPE:
                    hideSuggestions();
                    event.consume();
                    break;
                default:
                    break;
            }
        }
    }

    private final class SuggestionClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent event) {
            final int index = suggestionList.locationToIndex(event.getPoint());
            if (index >= 0) {
                suggestionList.setSelectedIndex(index);
                chooseHighlighted();
                textField.requestFocusInWindow();
            }
        }
    }
}
