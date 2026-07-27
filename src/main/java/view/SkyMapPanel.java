package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;

import entity.Star;

public class SkyMapPanel extends JPanel {

    private static final double[][] demo_star_positions = {
        {-0.72, -0.45},
        {-0.55, -0.66},
        {-0.30, -0.75},
        {-0.05, -0.82},
        {0.20, -0.72},
        {0.45, -0.66},
        {0.68, -0.48},
        {-0.82, -0.15},
        {-0.62, -0.30},
        {-0.40, -0.45},
        {-0.18, -0.55},
        {0.08, -0.52},
        {0.32, -0.50},
        {0.58, -0.32},
        {0.78, -0.18},
        {-0.88, 0.05},
        {-0.70, 0.12},
        {-0.50, 0.00},
        {-0.28, -0.10},
        {-0.08, -0.20},
        {0.15, -0.16},
        {0.38, -0.08},
        {0.60, 0.02},
        {0.82, 0.10},
        {-0.80, 0.32},
        {-0.60, 0.38},
        {-0.38, 0.20},
        {-0.18, 0.10},
        {0.02, 0.05},
        {0.25, 0.12},
        {0.48, 0.22},
        {0.68, 0.34},
        {-0.68, 0.55},
        {-0.45, 0.58},
        {-0.22, 0.42},
        {0.05, 0.32},
        {0.28, 0.38},
        {0.50, 0.48},
        {0.62, 0.62},
        {-0.50, 0.72},
        {-0.25, 0.68},
        {0.02, 0.62},
        {0.30, 0.70},
        {-0.10, -0.35},
        {0.42, -0.28},
        {-0.35, 0.05},
        {0.10, 0.22},
        {0.72, 0.18},
        {-0.72, -0.05},
        {0.55, 0.10}
    };

    public SkyMapPanel() {
        setBackground(new Color(7, 7, 9));
    }

    public void setStars(List<Star> stars) {
        this.stars = stars;
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);

        final Graphics2D graphics2D = (Graphics2D) graphics.create();
        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            final int diameter = Math.max(
                    0, Math.min(getWidth() - 80, getHeight() - 60));
            final int radius = diameter / 2;
            final int centreX = getWidth() / 2;
            final int centreY = getHeight() / 2;
            final int circleX = centreX - radius;
            final int circleY = centreY - radius;

            graphics2D.setColor(Color.BLACK);
            graphics2D.fillOval(circleX, circleY, diameter, diameter);

            graphics2D.setColor(Color.WHITE);
            graphics2D.setStroke(new BasicStroke(2.0F));
            graphics2D.drawOval(circleX, circleY, diameter, diameter);

            graphics2D.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            final FontMetrics metrics = graphics2D.getFontMetrics();
            graphics2D.drawString(
                    "N",
                    centreX - metrics.stringWidth("N") / 2,
                    circleY - 8);
            graphics2D.drawString(
                    "E",
                    circleX + diameter + 10,
                    centreY + metrics.getAscent() / 2);
            graphics2D.drawString(
                    "S",
                    centreX - metrics.stringWidth("S") / 2,
                    circleY + diameter + metrics.getAscent() + 8);
            graphics2D.drawString(
                    "W",
                    circleX - metrics.stringWidth("W") - 10,
                    centreY + metrics.getAscent() / 2);

            graphics2D.fillOval(centreX - 3, centreY - 3, 6, 6);

            if (stars != null) {
                graphics2D.setColor(Color.WHITE);
                List<Star> visibleStars = VisibilityFilter.filterVisible(stars);

                for (Star star : visibleStars) {
                    SkyVisualization.ScreenPosition pos =
                            SkyVisualization.project(star, centreX, centreY, radius);

                    int starSize = 5;
                    graphics2D.fillOval(
                            pos.x - starSize / 2,
                            pos.y - starSize / 2,
                            starSize,
                            starSize);
                }
            }
        }
        finally {
            graphics2D.dispose();
        }
    }
}
