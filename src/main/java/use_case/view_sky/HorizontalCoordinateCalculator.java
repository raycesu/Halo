package use_case.view_sky;

import java.time.ZonedDateTime;
import java.util.List;

import entity.ObserverLocation;
import entity.Star;

public interface HorizontalCoordinateCalculator {

    void updateHorizontalPositions(
            List<Star> stars,
            ObserverLocation observerLocation,
            ZonedDateTime observationTime);
}