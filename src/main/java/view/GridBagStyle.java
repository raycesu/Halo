package view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Small shared factory for the {@code GridBag}-family AWT types, kept separate from
 * {@link SwingStyle} so that no single helper class ends up depending on too many AWT types.
 */
final class GridBagStyle {

    private GridBagStyle() {
    }

    static GridBagLayout layout() {
        return new GridBagLayout();
    }

    static GridBagConstraints constraints() {
        return new GridBagConstraints();
    }

    static Insets insets(final int top, final int left, final int bottom, final int right) {
        return new Insets(top, left, bottom, right);
    }
}
