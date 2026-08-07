package view;

import entity.Star;

public class SkyVisualization {

    private static final double DEGREES_FROM_ZENITH_TO_HORIZON = 90.0;

    /**
     * Projects a star's altitude/azimuth onto the circular horizon map as a screen position.
     *
     * @param star the star whose current altitude and azimuth should be projected
     * @param centerX the x coordinate of the map's center (the zenith)
     * @param centerY the y coordinate of the map's center (the zenith)
     * @param mapRadius the pixel radius of the horizon circle
     * @return the temporary screen position for this star
     */
    public static ScreenPosition project(
            final Star star, final int centerX, final int centerY, final int mapRadius) {
        final double altitude = star.getAltitude();
        final double azimuth = star.getAzimuth();
        final double r = ((DEGREES_FROM_ZENITH_TO_HORIZON - altitude)
                / DEGREES_FROM_ZENITH_TO_HORIZON) * mapRadius;
        final double theta = Math.toRadians(azimuth);
        final int screenX = centerX + (int) (r * Math.sin(theta));
        final int screenY = centerY - (int) (r * Math.cos(theta));
        return new ScreenPosition(screenX, screenY);
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
}
