package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Small shared factories for the handful of AWT/Swing value types (colors, fonts, sizes,
 * layouts) that every hand-built panel in this package needs, so individual view classes do
 * not each separately depend on every one of those types.
 */
final class SwingStyle {

    private SwingStyle() {
    }

    static Color rgb(final int red, final int green, final int blue) {
        return new Color(red, green, blue);
    }

    static Font sansSerifFont(final int style, final int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    static Dimension size(final int width, final int height) {
        return new Dimension(width, height);
    }

    static BorderLayout border() {
        return new BorderLayout();
    }

    static BorderLayout border(final int horizontalGap, final int verticalGap) {
        return new BorderLayout(horizontalGap, verticalGap);
    }

    static FlowLayout flow(final int alignment, final int horizontalGap, final int verticalGap) {
        return new FlowLayout(alignment, horizontalGap, verticalGap);
    }

    static GridLayout grid(final int rows, final int columns, final int horizontalGap, final int verticalGap) {
        return new GridLayout(rows, columns, horizontalGap, verticalGap);
    }

    static void stackVertically(final JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }

    /**
     * Parses a "#rrggbb"-style hex color, or falls back to black when the given value is
     * missing or blank (e.g. a rating that has not been computed yet).
     *
     * @param hexColor the "#rrggbb"-style color string, or null/blank for the default
     * @return the parsed color, or {@link Color#BLACK} when {@code hexColor} is missing or blank
     */
    static Color colorOrDefault(final String hexColor) {
        final Color color;
        if (hexColor == null || hexColor.isBlank()) {
            color = Color.BLACK;
        }
        else {
            color = Color.decode(hexColor);
        }
        return color;
    }

    static GridBagLayout gridBagLayout() {
        return new GridBagLayout();
    }

    static GridBagConstraints gridBagConstraints() {
        return new GridBagConstraints();
    }

    static Insets insets(final int top, final int left, final int bottom, final int right) {
        return new Insets(top, left, bottom, right);
    }
}
