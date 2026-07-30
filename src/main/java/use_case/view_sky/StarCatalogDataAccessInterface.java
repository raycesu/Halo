package use_case.view_sky;

import java.util.List;

import entity.Star;

public interface StarCatalogDataAccessInterface {

    List<Star> findAll();
}
