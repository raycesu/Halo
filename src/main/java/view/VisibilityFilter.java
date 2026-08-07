package view;

import java.util.ArrayList;
import java.util.List;

import entity.Star;

public class VisibilityFilter {
    private static final double MIN_VISIBLE_ALTITUDE = 0.0;

    /**
     * Filters a star list down to the objects currently above the horizon.
     *
     * @param allstars the stars to filter
     * @return the stars with a calculated altitude at or above zero degrees
     */
    public static List<Star> filterVisible(List<Star> allstars) {
        List<Star> visible = new ArrayList<>();
        for (Star star : allstars) {
            double altitude = star.getAltitude();
            if (Double.isNaN(altitude)) {
                continue;
            }
            if (altitude < MIN_VISIBLE_ALTITUDE) {
                continue;
            }
            visible.add(star);
        }
        return visible;
    }

}
