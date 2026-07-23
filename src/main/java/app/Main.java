package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new HaloAppBuilder().build();
            frame.setVisible(true);
        });
    }
}
