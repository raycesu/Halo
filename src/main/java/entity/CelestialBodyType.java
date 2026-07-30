package entity;

/**
 * The kind of object being plotted. The sky map renders each kind differently (for example
 * planets larger than stars, the Sun and Moon larger still), so this is part of the domain
 * rather than a presentation detail.
 */
public enum CelestialBodyType {

    SUN,
    MOON,
    PLANET,
    STAR
}
