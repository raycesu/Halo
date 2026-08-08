package use_case.view_sky;

import java.util.List;

import entity.StaticConstellationDefinition;

/** Provides the built-in constellation definitions without exposing their storage format. */
public interface StaticConstellationDataAccessInterface {

    /**
     * Returns every built-in constellation definition.
     *
     * @return immutable constellation definitions
     */
    List<StaticConstellationDefinition> findAll();
}
