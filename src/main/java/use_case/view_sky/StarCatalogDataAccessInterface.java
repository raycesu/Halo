package use_case.view_sky;

import java.util.List;

import entity.Star;

public interface StarCatalogDataAccessInterface {

    /**
     * Returns every catalogue star, unpositioned.
     *
     * @return the complete star catalogue
     */
    List<Star> findAll();
}
