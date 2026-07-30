package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;

import entity.CelestialBodyType;
import entity.Star;

public class SkyMapPanel extends JPanel {

    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 6.0;
    private static final double ZOOM_STEP = 1.15;
    private static final int INITIAL_MARGIN = 20;
    private static final int DRAG_THRESHOLD = 5;
    private static final int CLICK_RADIUS = 12;
    private static final Color SELECTION_COLOR = new Color(255, 210, 40);

    private List<Star> stars = List.of();
    private Star selectedObject;
    private SelectionListener selectionListener;
    private double zoom = MIN_ZOOM;
    private double panOffsetX;
    private double panOffsetY;
    private boolean viewInitialized;
    private boolean mousePressed;
    private boolean dragging;
    private boolean suppressClick;
    private int dragStartX;
    private int dragStartY;
    private double dragStartPanX;
    private double dragStartPanY;

    public SkyMapPanel() {
        setBackground(new Color(7, 7, 9));

        final MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent event) {
                ensureViewInitialized();
                mousePressed = true;
                dragging = false;
                suppressClick = false;
                dragStartX = event.getX();
                dragStartY = event.getY();
                dragStartPanX = panOffsetX;
                dragStartPanY = panOffsetY;
            }

            @Override
            public void mouseDragged(final MouseEvent event) {
                if (!mousePressed || !viewInitialized) {
                    return;
                }

                final int differenceX = event.getX() - dragStartX;
                final int differenceY = event.getY() - dragStartY;

                if (!dragging
                        && Math.hypot(differenceX, differenceY) >= DRAG_THRESHOLD) {
                    dragging = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                if (dragging) {
                    panOffsetX = dragStartPanX + differenceX;
                    panOffsetY = dragStartPanY + differenceY;
                    clampPanOffsets();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (dragging) {
                    suppressClick = true;
                }
                mousePressed = false;
                dragging = false;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(final MouseEvent event) {
                if (suppressClick) {
                    suppressClick = false;
                }
                else {
                    selectObjectAt(event.getX(), event.getY());
                }
            }

            @Override
            public void mouseWheelMoved(final java.awt.event.MouseWheelEvent event) {
                zoomAt(event.getX(), event.getY(), event.getWheelRotation());
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    public void setStars(final List<Star> stars) {
        this.stars = List.copyOf(stars);
        selectedObject = null;
        repaint();
    }

    public Star getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(final Star selectedObject) {
        this.selectedObject = selectedObject;
        repaint();
    }

    public void setSelectionListener(final SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void resetView() {
        panOffsetX = 0.0;
        panOffsetY = 0.0;
        viewInitialized = false;
        zoom = MIN_ZOOM;
        ensureViewInitialized();
        repaint();
    }

    double getZoom() {
        ensureViewInitialized();
        return zoom;
    }

    double getPanOffsetX() {
        return panOffsetX;
    }

    double getPanOffsetY() {
        return panOffsetY;
    }

    @Override
    protected void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        ensureViewInitialized();

        final Graphics2D graphics2D = (Graphics2D) graphics.create();
        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            final int radius = effectiveRadius();
            final int diameter = radius * 2;
            final int centreX = adjustedCentreX();
            final int centreY = adjustedCentreY();
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

            for (final Star star : VisibilityFilter.filterVisible(stars)) {
                final SkyVisualization.ScreenPosition position =
                        SkyVisualization.project(star, centreX, centreY, radius);
                drawObject(graphics2D, star, position);
            }
        }
        finally {
            graphics2D.dispose();
        }
    }

    private void drawObject(
            final Graphics2D graphics2D,
            final Star star,
            final SkyVisualization.ScreenPosition position) {
        final int size = objectSize(star);

        graphics2D.setColor(objectColor(star));
        graphics2D.fillOval(
                position.x - size / 2,
                position.y - size / 2,
                size,
                size);

        if (star == selectedObject) {
            final int highlightSize = size + 8;
            graphics2D.setColor(SELECTION_COLOR);
            graphics2D.setStroke(new BasicStroke(2.0F));
            graphics2D.drawOval(
                    position.x - highlightSize / 2,
                    position.y - highlightSize / 2,
                    highlightSize,
                    highlightSize);
        }
    }

    private int objectSize(final Star star) {
        final CelestialBodyType type = star.getType();
        final int size;

        if (type == CelestialBodyType.SUN) {
            size = 14;
        }
        else if (type == CelestialBodyType.MOON) {
            size = 12;
        }
        else if (type == CelestialBodyType.PLANET) {
            size = 9;
        }
        else if (Double.isFinite(star.getApparentMagnitude())) {
            size = Math.max(
                    3,
                    Math.min(9, (int) Math.round(7.0 - star.getApparentMagnitude())));
        }
        else {
            size = 4;
        }

        return size;
    }

    private Color objectColor(final Star star) {
        final CelestialBodyType type = star.getType();
        final Color color;

        if (type == CelestialBodyType.SUN) {
            color = new Color(255, 190, 60);
        }
        else if (type == CelestialBodyType.MOON) {
            color = new Color(220, 225, 235);
        }
        else if (type == CelestialBodyType.PLANET) {
            color = new Color(120, 190, 255);
        }
        else {
            color = Color.WHITE;
        }

        return color;
    }

    private void selectObjectAt(final int mouseX, final int mouseY) {
        ensureViewInitialized();
        final int radius = effectiveRadius();
        final int centreX = adjustedCentreX();
        final int centreY = adjustedCentreY();
        final double maximumDistanceSquared = CLICK_RADIUS * CLICK_RADIUS;

        Star nearestObject = null;
        double nearestDistanceSquared = maximumDistanceSquared;

        for (final Star star : VisibilityFilter.filterVisible(stars)) {
            final SkyVisualization.ScreenPosition position =
                    SkyVisualization.project(star, centreX, centreY, radius);
            final double differenceX = position.x - mouseX;
            final double differenceY = position.y - mouseY;
            final double distanceSquared =
                    differenceX * differenceX + differenceY * differenceY;

            if (distanceSquared <= nearestDistanceSquared) {
                nearestObject = star;
                nearestDistanceSquared = distanceSquared;
            }
        }

        selectedObject = nearestObject;
        repaint();

        if (selectionListener != null) {
            selectionListener.objectSelected(nearestObject);
        }
    }

    private void zoomAt(
            final int mouseX,
            final int mouseY,
            final int wheelRotation) {
        ensureViewInitialized();

        if (!viewInitialized || wheelRotation == 0) {
            return;
        }

        final double oldZoom = zoom;
        final double requestedZoom =
                oldZoom * Math.pow(ZOOM_STEP, -wheelRotation);
        final double newZoom =
                Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requestedZoom));

        if (newZoom == oldZoom) {
            return;
        }

        final double panelCentreX = getWidth() / 2.0;
        final double panelCentreY = getHeight() / 2.0;
        final double scaleChange = newZoom / oldZoom;

        panOffsetX = mouseX - panelCentreX
                - scaleChange * (mouseX - panelCentreX - panOffsetX);
        panOffsetY = mouseY - panelCentreY
                - scaleChange * (mouseY - panelCentreY - panOffsetY);
        zoom = newZoom;
        clampPanOffsets();
        repaint();
    }

    private void clampPanOffsets() {
        final double maximumPanDistance =
                Math.max(0.0, baseRadius() * zoom - baseRadius());
        final double currentPanDistance =
                Math.hypot(panOffsetX, panOffsetY);

        if (currentPanDistance > maximumPanDistance) {
            if (maximumPanDistance == 0.0) {
                panOffsetX = 0.0;
                panOffsetY = 0.0;
            }
            else {
                final double scale = maximumPanDistance / currentPanDistance;
                panOffsetX *= scale;
                panOffsetY *= scale;
            }
        }
    }

    private void ensureViewInitialized() {
        final int baseRadius = baseRadius();

        if (!viewInitialized && getWidth() > 0 && getHeight() > 0 && baseRadius > 0) {
            final double cornerDistance =
                    Math.hypot(getWidth() / 2.0, getHeight() / 2.0);
            final double requiredZoom =
                    (cornerDistance + INITIAL_MARGIN) / baseRadius;

            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requiredZoom));
            panOffsetX = 0.0;
            panOffsetY = 0.0;
            viewInitialized = true;
        }

        if (viewInitialized) {
            clampPanOffsets();
        }
    }

    private int adjustedCentreX() {
        return (int) Math.round(getWidth() / 2.0 + panOffsetX);
    }

    private int adjustedCentreY() {
        return (int) Math.round(getHeight() / 2.0 + panOffsetY);
    }

    private int effectiveRadius() {
        return (int) Math.round(baseRadius() * zoom);
    }

    private int baseRadius() {
        return mapDiameter() / 2;
    }

    private int mapDiameter() {
        return Math.max(0, Math.min(getWidth() - 80, getHeight() - 60));
    }

    public interface SelectionListener {

        void objectSelected(Star star);
    }
}
