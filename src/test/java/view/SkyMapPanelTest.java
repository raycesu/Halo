package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import entity.CelestialBodyType;
import entity.Star;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyMapPanelTest {

    private static final int PANEL_SIZE = 400;
    private static final int BASE_RADIUS = 160;
    private static final int CENTRE = PANEL_SIZE / 2;

    private SkyMapPanel panel;
    private Star star;
    private Star selectedByListener;

    @BeforeEach
    void setUp() {
        panel = new SkyMapPanel();
        panel.setSize(PANEL_SIZE, PANEL_SIZE);
        panel.setSelectionListener(selectedObject -> selectedByListener = selectedObject);

        star = createObservedStar("Vega", 60.0, 90.0);
        panel.setStars(List.of(star));
    }

    @Test
    void minimumZoomIsOneAndZoomStaysWithinItsLimits() {
        wheel(CENTRE, CENTRE, 100);
        assertEquals(1.0, panel.getZoom(), 1e-9);

        wheel(CENTRE, CENTRE, -100);
        assertEquals(6.0, panel.getZoom(), 1e-9);

        wheel(CENTRE, CENTRE, 100);
        assertEquals(1.0, panel.getZoom(), 1e-9);
    }

    @Test
    void calculatedInitialZoomPlacesTheHorizonOutsideThePanel() {
        final double effectiveRadius = BASE_RADIUS * panel.getZoom();
        final double farthestCorner =
                Math.hypot(PANEL_SIZE / 2.0, PANEL_SIZE / 2.0);

        assertTrue(panel.getZoom() > 1.0);
        assertTrue(effectiveRadius > farthestCorner);
        assertEquals(0.0, panel.getPanOffsetX(), 1e-9);
        assertEquals(0.0, panel.getPanOffsetY(), 1e-9);
    }

    @Test
    void resetViewRestoresTheCalculatedCentredView() {
        final double initialZoom = panel.getZoom();
        wheel(CENTRE, CENTRE, -2);
        drag(100, 100, 135, 120);

        panel.resetView();

        assertEquals(initialZoom, panel.getZoom(), 1e-9);
        assertEquals(0.0, panel.getPanOffsetX(), 1e-9);
        assertEquals(0.0, panel.getPanOffsetY(), 1e-9);
    }

    @Test
    void mouseWheelChangesZoom() {
        final double originalZoom = panel.getZoom();

        wheel(CENTRE, CENTRE, -1);

        assertTrue(panel.getZoom() > originalZoom);
    }

    @Test
    void draggingChangesPanOffsets() {
        drag(100, 100, 130, 120);

        assertEquals(30.0, panel.getPanOffsetX(), 1e-9);
        assertEquals(20.0, panel.getPanOffsetY(), 1e-9);
    }

    @Test
    void fullyZoomedOutViewCannotBePanned() {
        wheel(CENTRE, CENTRE, 100);

        drag(100, 100, 180, 160);

        assertEquals(1.0, panel.getZoom(), 1e-9);
        assertEquals(0.0, panel.getPanOffsetX(), 1e-9);
        assertEquals(0.0, panel.getPanOffsetY(), 1e-9);
    }

    @Test
    void panningCannotMoveTheMapBeyondTheFullCircleBoundary() {
        final double maximumPanDistance =
                BASE_RADIUS * panel.getZoom() - BASE_RADIUS;

        drag(CENTRE, CENTRE, CENTRE + 1000, CENTRE + 1000);

        final double actualPanDistance = Math.hypot(
                panel.getPanOffsetX(),
                panel.getPanOffsetY());
        assertEquals(maximumPanDistance, actualPanDistance, 1e-9);
    }

    @Test
    void draggingDoesNotSelectAnObject() {
        final SkyVisualization.ScreenPosition position = projectedPosition(star);

        drag(position.x, position.y, position.x + 30, position.y + 20);

        assertNull(panel.getSelectedObject());
        assertNull(selectedByListener);
    }

    @Test
    void clickingNearAProjectedObjectSelectsItAfterZoomAndPan() {
        wheel(CENTRE + 30, CENTRE, -1);
        drag(100, 100, 125, 115);
        final SkyVisualization.ScreenPosition position = projectedPosition(star);

        click(position.x + 3, position.y - 2);

        assertSame(star, panel.getSelectedObject());
        assertSame(star, selectedByListener);
    }

    @Test
    void clickingAwayFromObjectsClearsTheSelection() {
        final SkyVisualization.ScreenPosition position = projectedPosition(star);
        click(position.x, position.y);

        click(10, 10);

        assertNull(panel.getSelectedObject());
        assertNull(selectedByListener);
    }

    @Test
    void selectionIsHighlightedUntilTheDisplayedListIsReplaced() {
        final SkyVisualization.ScreenPosition position = projectedPosition(star);
        click(position.x, position.y);

        assertSame(star, panel.getSelectedObject());
        assertTrue(renderContainsSelectionColor());

        wheel(CENTRE, CENTRE, -1);
        assertSame(star, panel.getSelectedObject());

        final double zoomBeforeReplacement = panel.getZoom();
        final double panXBeforeReplacement = panel.getPanOffsetX();
        final double panYBeforeReplacement = panel.getPanOffsetY();
        panel.setStars(List.of(createObservedStar("Altair", 60.0, 180.0)));

        assertNull(panel.getSelectedObject());
        assertEquals(zoomBeforeReplacement, panel.getZoom(), 1e-9);
        assertEquals(panXBeforeReplacement, panel.getPanOffsetX(), 1e-9);
        assertEquals(panYBeforeReplacement, panel.getPanOffsetY(), 1e-9);
    }

    @Test
    void celestialBodiesUseCompactWhiteMarkersExceptForTheOrangeSun() {
        assertMarkerRendering(
                CelestialBodyType.SUN,
                new Color(255, 190, 60),
                5);
        assertMarkerRendering(CelestialBodyType.MOON, Color.WHITE, 4);
        assertMarkerRendering(CelestialBodyType.PLANET, Color.WHITE, 3);
    }

    private SkyVisualization.ScreenPosition projectedPosition(final Star object) {
        final int centreX =
                (int) Math.round(CENTRE + panel.getPanOffsetX());
        final int centreY =
                (int) Math.round(CENTRE + panel.getPanOffsetY());
        final int effectiveRadius =
                (int) Math.round(BASE_RADIUS * panel.getZoom());
        return SkyVisualization.project(object, centreX, centreY, effectiveRadius);
    }

    private boolean renderContainsSelectionColor() {
        final BufferedImage image = renderPanel();

        final int selectionRgb = new Color(255, 210, 40).getRGB();
        boolean found = false;

        for (int x = 0; x < image.getWidth() && !found; x++) {
            for (int y = 0; y < image.getHeight() && !found; y++) {
                found = image.getRGB(x, y) == selectionRgb;
            }
        }

        return found;
    }

    private void assertMarkerRendering(
            final CelestialBodyType type,
            final Color expectedColor,
            final int markerRadius) {
        final Star object = createObservedObject(type);
        panel.setStars(List.of(object));

        final BufferedImage image = renderPanel();

        assertEquals(expectedColor.getRGB(), image.getRGB(CENTRE, CENTRE));
        assertEquals(Color.BLACK.getRGB(), image.getRGB(CENTRE + markerRadius, CENTRE));
    }

    private BufferedImage renderPanel() {
        final BufferedImage image =
                new BufferedImage(PANEL_SIZE, PANEL_SIZE, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = image.createGraphics();
        panel.paint(graphics);
        graphics.dispose();
        return image;
    }

    private void wheel(
            final int x,
            final int y,
            final int wheelRotation) {
        panel.dispatchEvent(new MouseWheelEvent(
                panel,
                MouseEvent.MOUSE_WHEEL,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                1,
                wheelRotation));
    }

    private void drag(
            final int startX,
            final int startY,
            final int endX,
            final int endY) {
        dispatchMouse(MouseEvent.MOUSE_PRESSED, startX, startY, MouseEvent.BUTTON1);
        dispatchMouse(MouseEvent.MOUSE_DRAGGED, endX, endY, MouseEvent.NOBUTTON);
        dispatchMouse(MouseEvent.MOUSE_RELEASED, endX, endY, MouseEvent.BUTTON1);
        dispatchMouse(MouseEvent.MOUSE_CLICKED, endX, endY, MouseEvent.BUTTON1);
    }

    private void click(final int x, final int y) {
        dispatchMouse(MouseEvent.MOUSE_PRESSED, x, y, MouseEvent.BUTTON1);
        dispatchMouse(MouseEvent.MOUSE_RELEASED, x, y, MouseEvent.BUTTON1);
        dispatchMouse(MouseEvent.MOUSE_CLICKED, x, y, MouseEvent.BUTTON1);
    }

    private void dispatchMouse(
            final int eventId,
            final int x,
            final int y,
            final int button) {
        panel.dispatchEvent(new MouseEvent(
                panel,
                eventId,
                System.currentTimeMillis(),
                0,
                x,
                y,
                1,
                false,
                button));
    }

    private Star createObservedStar(
            final String name,
            final double altitude,
            final double azimuth) {
        final Star observedStar = new Star(
                "HIP test",
                name,
                12.0,
                20.0,
                1.0,
                "region",
                "spectral",
                "description");
        observedStar.updateHorizontalPosition(altitude, azimuth);
        return observedStar;
    }

    private Star createObservedObject(final CelestialBodyType type) {
        final Star observedObject = new Star(
                type.name(),
                type.name(),
                12.0,
                20.0,
                1.0,
                "region",
                "spectral",
                "description",
                type);
        observedObject.updateHorizontalPosition(90.0, 0.0);
        return observedObject;
    }
}
