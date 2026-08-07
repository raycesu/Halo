package use_case.custom_constellation;

import java.util.List;

import entity.CustomConstellation;

public interface ConstellationDataAccessInterface {

    /**
     * Checks whether a constellation with the given name already exists.
     *
     * @param name the constellation name to check
     * @return true if a constellation with this name already exists
     */
    boolean existsByName(String name);

    /**
     * Persists a custom constellation.
     *
     * @param constellation the constellation to save
     */
    void save(CustomConstellation constellation);

    /**
     * Returns every saved custom constellation.
     *
     * @return all saved custom constellations
     */
    List<CustomConstellation> findAll();
}
