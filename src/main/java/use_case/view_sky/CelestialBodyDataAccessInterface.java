package use_case.view_sky;

import java.time.Instant;
import java.util.List;

import entity.ObserverLocation;
import entity.Star;
import entity.CelestialBodyType;

/**
 * Supplies the positions of moving celestial objects (the Sun, Moon and planets, plus any bright
 * stars the source happens to include) as seen from one observer at one instant.
 *
 * <p>Declared here and implemented in the data access layer so that use cases depend on this
 * abstraction rather than on any particular ephemeris service.
 *
 * <p>This is deliberately separate from a star catalogue gateway. A catalogue answers "what
 * objects exist and where are they fixed on the celestial sphere", which is time-independent and
 * can be served from a local dataset. This interface answers "where is everything right now for
 * this observer", which changes hour to hour.
 *
 * <p>Results are returned as {@link Star} because that entity already models a point-like body
 * with equatorial and horizontal coordinates; {@link CelestialBodyType} distinguishes a planet
 * from a fixed star.
 */
public interface CelestialBodyDataAccessInterface {

    /**
     * Returns the bodies known to the source for the given observer and instant, each already
     * carrying its observed altitude and azimuth.
     *
     * <p>The returned list is not guaranteed to be filtered by horizon, so callers that only
     * want what is currently visible should filter on {@link Star#isAboveHorizon()}. Deciding
     * what to draw is a use case concern, not a gateway one.
     *
     * @param location the observer's position on Earth
     * @param instant the moment of observation
     * @return the bodies, in no guaranteed order; never null and never empty
     * @throws CelestialDataUnavailableException if the data cannot be obtained or understood
     */
    List<Star> getCelestialBodies(ObserverLocation location, Instant instant)
            throws CelestialDataUnavailableException;
}
