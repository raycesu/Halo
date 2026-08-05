package entity;

/**
 * The kind of object being plotted. The sky map can render each kind differently, so this is
 * part of the domain rather than a presentation detail.
 */
public enum CelestialBodyType {

    SUN,
    MOON,
    PLANET,
    STAR
}
