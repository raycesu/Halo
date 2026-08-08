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
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import interface_adapter.custom_constellation.ConstellationDisplayData;
import interface_adapter.view_sky.StarDisplayData;

public class SkyMapPanel extends JPanel {

    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 6.0;
    private static final double ZOOM_STEP = 1.15;
    private static final int INITIAL_MARGIN = 20;
    private static final int DRAG_THRESHOLD = 5;
    private static final int CLICK_RADIUS = 12;
    private static final int SUN_MARKER_SIZE = 10;
    private static final int MOON_MARKER_SIZE = 8;
    private static final int PLANET_MARKER_SIZE = 6;
    private static final int MIN_STAR_SIZE = 3;
    private static final int MAX_STAR_SIZE = 9;
    private static final int DEFAULT_STAR_SIZE = 4;
    private static final double MAGNITUDE_SIZE_OFFSET = 7.0;
    private static final int SELECTION_HIGHLIGHT_PADDING = 8;
    private static final int COMPASS_LABEL_FONT_SIZE = 14;
    private static final int COMPASS_LABEL_VERTICAL_PADDING = 8;
    private static final int COMPASS_LABEL_HORIZONTAL_PADDING = 10;
    private static final int HORIZONTAL_MARGIN = 80;
    private static final int VERTICAL_MARGIN = 60;
    private static final Color BACKGROUND_COLOR = new Color(7, 7, 9);
    private static final Color SELECTION_COLOR = new Color(255, 210, 40);
    private static final Color SUN_COLOR = new Color(255, 190, 60);
    private static final Color CONSTELLATION_LINE_COLOR = new Color(80, 180, 255);
    private static final Font COMPASS_LABEL_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, COMPASS_LABEL_FONT_SIZE);
    private static final BasicStroke HORIZON_STROKE = new BasicStroke(2.0F);
    private static final BasicStroke SELECTION_STROKE = new BasicStroke(2.0F);
    private static final BasicStroke CONSTELLATION_STROKE = new BasicStroke(1.5F);

    private List<StarDisplayData> stars = List.of();
    private StarDisplayData selectedObject;
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
    private List<ConstellationDisplayData> customConstellations = List.of();
    private List<StarDisplayData> constellationSelection = List.of();

    public SkyMapPanel() {
        setBackground(BACKGROUND_COLOR);

        final SkyMapMouseHandler mouseHandler = new SkyMapMouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    /**
     * Replaces the stars currently displayed, clearing any prior selection.
     *
     * @param stars the completed, calculated stars for the current observation
     */
    public void setStars(final List<StarDisplayData> stars) {
        this.stars = List.copyOf(stars);
        selectedObject = null;
        repaint();
    }

    /**
     * User-created constellations currently displayed on the sky map.
     *
     * @param customConstellations the constellations to draw
     */
    public void setCustomConstellations(final List<ConstellationDisplayData> customConstellations) {
        this.customConstellations = List.copyOf(customConstellations);
        repaint();
    }

    /**
     * Stars selected in order, while the user creates a constellation.
     *
     * @param constellationSelection the stars selected so far, in selection order
     */
    public void setConstellationSelection(final List<StarDisplayData> constellationSelection) {
        this.constellationSelection = List.copyOf(constellationSelection);
        repaint();
    }

    /**
     * Returns the currently selected object.
     *
     * @return the currently selected object, or null if nothing is selected
     */
    public StarDisplayData getSelectedObject() {
        return selectedObject;
    }

    /**
     * Highlights the given object as the current selection.
     *
     * @param selectedObject the object to highlight as selected, or null to clear the selection
     */
    public void setSelectedObject(final StarDisplayData selectedObject) {
        this.selectedObject = selectedObject;
        repaint();
    }

    /**
     * Registers the listener notified whenever the user clicks a displayed object.
     *
     * @param selectionListener notified whenever the user clicks a displayed object
     */
    public void setSelectionListener(final SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    /** Resets zoom and pan to the initial, fully-zoomed-out view. */
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
            graphics2D.setStroke(HORIZON_STROKE);
            graphics2D.drawOval(circleX, circleY, diameter, diameter);

            graphics2D.setFont(COMPASS_LABEL_FONT);
            final FontMetrics metrics = graphics2D.getFontMetrics();
            graphics2D.drawString(
                    "N",
                    centreX - metrics.stringWidth("N") / 2,
                    circleY - COMPASS_LABEL_VERTICAL_PADDING);
            graphics2D.drawString(
                    "E",
                    circleX + diameter + COMPASS_LABEL_HORIZONTAL_PADDING,
                    centreY + metrics.getAscent() / 2);
            graphics2D.drawString(
                    "S",
                    centreX - metrics.stringWidth("S") / 2,
                    circleY + diameter + metrics.getAscent() + COMPASS_LABEL_VERTICAL_PADDING);
            graphics2D.drawString(
                    "W",
                    circleX - metrics.stringWidth("W") - COMPASS_LABEL_HORIZONTAL_PADDING,
                    centreY + metrics.getAscent() / 2);

            drawCustomConstellations(graphics2D, centreX, centreY, radius);

            for (final StarDisplayData star : filterVisible(stars)) {
                final ScreenPosition position =
                        project(star.getAltitude(), star.getAzimuth(), centreX, centreY, radius);
                drawObject(graphics2D, star, position);
            }
        }
        finally {
            graphics2D.dispose();
        }
    }

    /**
     * Draws saved constellation as line segments between visible stars.
     *
     * @param graphics2D the graphics context to draw with
     * @param centreX the x coordinate of the map's centre (the zenith)
     * @param centreY the y coordinate of the map's centre (the zenith)
     * @param radius the pixel radius of the horizon circle
     */
    private void drawCustomConstellations(
            final Graphics2D graphics2D, final int centreX, final int centreY, final int radius) {
        graphics2D.setColor(CONSTELLATION_LINE_COLOR);
        graphics2D.setStroke(CONSTELLATION_STROKE);

        for (final ConstellationDisplayData constellation : customConstellations) {
            for (final ConstellationDisplayData.Line line : constellation.getLines()) {
                if (line.isStartAboveHorizon() && line.isEndAboveHorizon()) {
                    final ScreenPosition start =
                            project(line.getStartAltitude(), line.getStartAzimuth(),
                                    centreX, centreY, radius);
                    final ScreenPosition end =
                            project(line.getEndAltitude(), line.getEndAzimuth(),
                                    centreX, centreY, radius);

                    graphics2D.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
                }
            }
        }
    }

    private void drawObject(
            final Graphics2D graphics2D,
            final StarDisplayData star,
            final ScreenPosition position) {
        final int size = objectSize(star);

        graphics2D.setColor(objectColor(star));
        graphics2D.fillOval(
                position.getX() - size / 2,
                position.getY() - size / 2,
                size,
                size);

        if (star.equals(selectedObject) || constellationSelection.contains(star)) {
            final int highlightSize = size + SELECTION_HIGHLIGHT_PADDING;
            graphics2D.setColor(SELECTION_COLOR);
            graphics2D.setStroke(SELECTION_STROKE);
            graphics2D.drawOval(
                    position.getX() - highlightSize / 2,
                    position.getY() - highlightSize / 2,
                    highlightSize,
                    highlightSize);
        }
    }

    private int objectSize(final StarDisplayData star) {
        final String type = star.getType();
        final int size;

        if ("SUN".equals(type)) {
            size = SUN_MARKER_SIZE;
        }
        else if ("MOON".equals(type)) {
            size = MOON_MARKER_SIZE;
        }
        else if ("PLANET".equals(type)) {
            size = PLANET_MARKER_SIZE;
        }
        else if (Double.isFinite(star.getApparentMagnitude())) {
            size = Math.max(
                    MIN_STAR_SIZE,
                    Math.min(
                            MAX_STAR_SIZE,
                            (int) Math.round(MAGNITUDE_SIZE_OFFSET - star.getApparentMagnitude())));
        }
        else {
            size = DEFAULT_STAR_SIZE;
        }

        return size;
    }

    private Color objectColor(final StarDisplayData star) {
        final Color color;

        if ("SUN".equals(star.getType())) {
            color = SUN_COLOR;
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

        StarDisplayData nearestObject = null;
        double nearestDistanceSquared = maximumDistanceSquared;

        for (final StarDisplayData star : filterVisible(stars)) {
            final ScreenPosition position =
                    project(star.getAltitude(), star.getAzimuth(), centreX, centreY, radius);
            final double differenceX = position.getX() - mouseX;
            final double differenceY = position.getY() - mouseY;
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

        if (viewInitialized && wheelRotation != 0) {
            final double oldZoom = zoom;
            final double requestedZoom =
                    oldZoom * Math.pow(ZOOM_STEP, -wheelRotation);
            final double newZoom =
                    Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requestedZoom));

            if (newZoom != oldZoom) {
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
        }
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
        return Math.max(0, Math.min(getWidth() - HORIZONTAL_MARGIN, getHeight() - VERTICAL_MARGIN));
    }

    private static final double DEGREES_FROM_ZENITH_TO_HORIZON = 90.0;
    private static final double MIN_VISIBLE_ALTITUDE = 0.0;

    static ScreenPosition project(
            final double altitude,
            final double azimuth,
            final int centerX,
            final int centerY,
            final int mapRadius) {
        final double r = ((DEGREES_FROM_ZENITH_TO_HORIZON - altitude)
                / DEGREES_FROM_ZENITH_TO_HORIZON) * mapRadius;
        final double theta = Math.toRadians(azimuth);
        final int screenX = centerX + (int) (r * Math.sin(theta));
        final int screenY = centerY - (int) (r * Math.cos(theta));
        return new ScreenPosition(screenX, screenY);
    }

    private static List<StarDisplayData> filterVisible(final List<StarDisplayData> allStars) {
        final List<StarDisplayData> visible = new ArrayList<>();
        for (final StarDisplayData star : allStars) {
            final double altitude = star.getAltitude();
            if (!Double.isNaN(altitude) && altitude >= MIN_VISIBLE_ALTITUDE) {
                visible.add(star);
            }
        }
        return visible;
    }

    public static class ScreenPosition {
        private final int x;
        private final int y;

        public ScreenPosition(final int screenX, final int screenY) {
            this.x = screenX;
            this.y = screenY;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    /** Notified whenever the user clicks a displayed object on the map. */
    public interface SelectionListener {

        /**
         * Called when the user clicks a displayed object.
         *
         * @param star the clicked object, or null if the click did not land on a displayed object
         */
        void objectSelected(StarDisplayData star);
    }

    /** Handles panning by drag, zooming by wheel, and click selection on the map. */
    private final class SkyMapMouseHandler extends MouseAdapter {

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
            if (mousePressed && viewInitialized) {
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
        public void mouseWheelMoved(final MouseWheelEvent event) {
            zoomAt(event.getX(), event.getY(), event.getWheelRotation());
        }
    }
}
