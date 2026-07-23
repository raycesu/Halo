package use_case.view_sky;

import java.util.List;

public interface StarCatalogDataAccessInterface {

    // String catalogue data is temporary until the domain entities are ready.
    List<String> getDemoStarNames();
}
