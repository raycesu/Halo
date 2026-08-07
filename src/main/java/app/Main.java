package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    /**
     * Launches the Halo application on the Swing Event Dispatch Thread.
     *
     * @param args command-line arguments, currently unused
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new HaloAppBuilder().build();
            frame.setVisible(true);
        });
    }
}
