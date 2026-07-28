package view;

import entity.Star;

public class SkyVisualization {

    public static class ScreenPosition{
        public final int x;
        public final int y;
        public ScreenPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static ScreenPosition project(Star star, int centerX, int centerY, int mapRadius) {
        double altitude = star.getAltitude();
        double azimuth = star.getAzimuth();
        double r =((90.0 - altitude) / 90.0) * mapRadius;
        double theta = Math.toRadians(azimuth);
        int x = centerX + (int)(r * Math.sin(theta) );
        int y = centerY - (int)(r * Math.cos(theta));
        return new ScreenPosition(x, y);
    }
}
