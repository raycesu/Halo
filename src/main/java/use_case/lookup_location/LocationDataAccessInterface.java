package use_case.lookup_location;

import java.util.List;

import entity.ObserverLocation;

/**
 * Resolves the name of a place into the coordinates and time zone the rest of the application
 * works in.
 *
 * <p>Every data source Halo talks to is addressed by latitude and longitude: the weather service,
 * the ephemeris service, and the coordinate conversion behind the star map. A place name is what
 * the user has, so something has to bridge the two, and this is it.
 */
public interface LocationDataAccessInterface {

    /**
     * Returns the places whose name matches {@code query}, best match first, so a caller that
     * wants a single answer can take the first element and one offering a list can show them all.
     *
     * <p>Matching is deliberately forgiving: it is driven by what the user has typed so far, which
     * for an as-you-type search is usually an unfinished word.
     *
     * @param query what the user typed; blank returns an empty list rather than everything
     * @param limit the most results to return; values below one return an empty list
     * @return matching places, never null, at most {@code limit} long
     */
    List<ObserverLocation> findByName(String query, int limit);
}
