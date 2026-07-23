package data_access;

import java.util.List;

import use_case.view_sky.StarCatalogDataAccessInterface;

public class InMemoryStarCatalogDataAccessObject implements StarCatalogDataAccessInterface {

    private final List<String> demoStarNames =
            List.of("Sirius", "Vega", "Betelgeuse", "Polaris", "Rigel");

    @Override
    public List<String> getDemoStarNames() {
        return List.copyOf(demoStarNames);
    }
}
