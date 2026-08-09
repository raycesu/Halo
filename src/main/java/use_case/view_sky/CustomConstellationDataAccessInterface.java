package use_case.view_sky;

import java.util.List;

import entity.CustomConstellation;

/** Provides saved custom constellations for rebuilding them at a new observation. */
public interface CustomConstellationDataAccessInterface {

    /**
     * Returns every saved custom constellation.
     *
     * @return all saved custom constellations
     */
    List<CustomConstellation> findAll();
}
