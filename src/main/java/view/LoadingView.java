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

    public LoadingView() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        final JLabel loadingLabel = new JLabel("Star map loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        loadingLabel.setAlignmentX(CENTER_ALIGNMENT);

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(320, 20));
        progressBar.setMaximumSize(new Dimension(320, 20));
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        contentPanel.add(loadingLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(progressBar);
        add(contentPanel);
    }
}
