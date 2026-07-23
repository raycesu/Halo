package app;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Starts the Halo application.
 */
public final class Main {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;

    private Main() {
    }

    /**
     * Launches Halo.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(Main::openWindow);
    }

    private static void openWindow() {
        final JFrame window = new JFrame("Halo");

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(WIDTH, HEIGHT);
        window.setLocationRelativeTo(null);

        final JLabel heading = new JLabel("HALO", SwingConstants.CENTER);
        window.add(heading);

        window.setVisible(true);
    }
}