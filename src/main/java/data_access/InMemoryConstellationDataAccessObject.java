package data_access;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import entity.CustomConstellation;
import use_case.custom_constellation.ConstellationDataAccessInterface;

public class InMemoryConstellationDataAccessObject implements ConstellationDataAccessInterface {
    private final Map<String, CustomConstellation> constellations =
            new LinkedHashMap<>();

    @Override
    public boolean existsByName(final String name) {
        return constellations.containsKey(normalizeName(name));
    }

    @Override
    public void save(final CustomConstellation constellation) {
        constellations.put(normalizeName(constellation.getName()), constellation);
    }

    @Override
    public List<CustomConstellation> findAll() {
        return new ArrayList<>(constellations.values());
    }

    private String normalizeName(final String name) {
        return name.trim().toLowerCase(Locale.US);
    }
}