package use_case.custom_constellation;

import entity.CustomConstellation;
import use_case.view_sky.CustomConstellationDataAccessInterface;

public interface ConstellationDataAccessInterface extends CustomConstellationDataAccessInterface {

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

}
