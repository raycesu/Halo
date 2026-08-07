package use_case.view_sky;

import java.time.ZonedDateTime;
import java.util.List;

import entity.ObserverLocation;
import entity.Star;

public interface HorizontalCoordinateCalculator {

    /**
     * Calculates and stores altitude and azimuth for every star, for one observer and instant.
     *
     * @param stars the stars to position, mutated in place
     * @param observerLocation the observer's location
     * @param observationTime the observation instant, in the observer's zone
     */
    void updateHorizontalPositions(
            List<Star> stars,
            ObserverLocation observerLocation,
            ZonedDateTime observationTime);
}
