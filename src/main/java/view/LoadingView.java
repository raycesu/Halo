package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class LoadingView extends JPanel {

    private static final int LOADING_LABEL_FONT_SIZE = 24;
    private static final int PROGRESS_BAR_WIDTH = 320;
    private static final int PROGRESS_BAR_HEIGHT = 20;
    private static final int LABEL_PROGRESS_SPACING = 20;

    public LoadingView() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        final JLabel loadingLabel = new JLabel("Star map loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LOADING_LABEL_FONT_SIZE));
        loadingLabel.setAlignmentX(CENTER_ALIGNMENT);

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT));
        progressBar.setMaximumSize(new Dimension(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT));
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        contentPanel.add(loadingLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, LABEL_PROGRESS_SPACING)));
        contentPanel.add(progressBar);
        add(contentPanel);
    }
}
