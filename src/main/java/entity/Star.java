package entity;

/**
 * A celestial object and, once observed, where it appears in an observer's sky.
 *
 * <p>Despite the name this represents any point-like body the map can draw, including the Sun,
 * the Moon and the planets; {@link #getType()} says which. A planet differs from a star only in
 * that its equatorial coordinates change with time, which is a property of where the data came
 * from rather than of what the object is, so one class serves both.
 *
 * <p>The catalogue fields are fixed facts about the object. {@code altitude} and {@code azimuth}
 * are not: they are only meaningful for one observer at one instant, and stay
 * {@link Double#NaN} until {@link #updateHorizontalPosition} supplies them. Use
 * {@link #copyForObservation()} to get a fresh, unpositioned copy before observing from a
 * different place or time.
 */
public final class Star {

    private static final double MIN_ALTITUDE_DEGREES = -90.0;
    private static final double MAX_ALTITUDE_DEGREES = 90.0;
    private static final double DEGREES_IN_CIRCLE = 360.0;

    private final String catalogueId;
    private final String displayName;

    /**
     * Right ascension in <em>hours</em> east of the vernal equinox, in the range [0, 24).
     *
     * <p>Hours rather than degrees because that is the convention of the Bright Star Catalogue
     * the project's dataset comes from. Multiply by 15 for degrees.
     */
    private final double rightAscension;

    /** Declination in degrees north of the celestial equator, in the range [-90, 90]. */
    private final double declination;

    private final double apparentMagnitude;
    private final String constellationRegion;
    private final String spectralType;
    private final String description;
    private final CelestialBodyType type;

    /** Degrees above the horizon; NaN until observed. */
    private double altitude = Double.NaN;

    /** Degrees clockwise from true north; NaN until observed. */
    private double azimuth = Double.NaN;

    /**
     * Creates a fixed star. Equivalent to the full constructor with a type of
     * {@link CelestialBodyType#STAR}.
     */
    public Star(
            final String catalogueId,
            final String displayName,
            final double rightAscension,
            final double declination,
            final double apparentMagnitude,
            final String constellationRegion,
            final String spectralType,
            final String description) {

        this(catalogueId, displayName, rightAscension, declination, apparentMagnitude,
                constellationRegion, spectralType, description, CelestialBodyType.STAR);
    }

    public Star(
            final String catalogueId,
            final String displayName,
            final double rightAscension,
            final double declination,
            final double apparentMagnitude,
            final String constellationRegion,
            final String spectralType,
            final String description,
            final CelestialBodyType type) {

        this.catalogueId = catalogueId;
        this.displayName = displayName;
        this.rightAscension = rightAscension;
        this.declination = declination;
        this.apparentMagnitude = apparentMagnitude;
        this.constellationRegion = constellationRegion;
        this.spectralType = spectralType;
        this.description = description;
        this.type = type;
    }

    public String getCatalogueId() {
        return catalogueId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getRightAscension() {
        return rightAscension;
    }

    public double getDeclination() {
        return declination;
    }

    public double getApparentMagnitude() {
        return apparentMagnitude;
    }

    public String getConstellationRegion() {
        return constellationRegion;
    }

    public String getSpectralType() {
        return spectralType;
    }

    public String getDescription() {
        return description;
    }

    public CelestialBodyType getType() {
        return type;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getAzimuth() {
        return azimuth;
    }

    /**
     * Records where this object appears for one observer at one instant.
     *
     * @param altitude degrees above the horizon, in [-90, 90]
     * @param azimuth degrees clockwise from true north, in [0, 360)
     * @throws IllegalArgumentException if either value is not finite or is out of range
     */
    public void updateHorizontalPosition(
            final double altitude,
            final double azimuth) {

        if (!Double.isFinite(altitude)
                || altitude < MIN_ALTITUDE_DEGREES
                || altitude > MAX_ALTITUDE_DEGREES) {
            throw new IllegalArgumentException(
                    "Altitude must be finite and in [-90, 90] but was " + altitude);
        }
        if (!Double.isFinite(azimuth) || azimuth < 0.0 || azimuth >= DEGREES_IN_CIRCLE) {
            throw new IllegalArgumentException(
                    "Azimuth must be finite and in [0, 360) but was " + azimuth);
        }
        this.altitude = altitude;
        this.azimuth = azimuth;
    }

    /**
     * Whether this object is above the observer's horizon, and so a candidate for being drawn.
     * An object that has not been observed yet counts as not above the horizon.
     *
     * <p>This is a domain rule rather than a rendering detail: a body below the horizon is
     * blocked by the Earth however the map chooses to draw it.
     */
    public boolean isAboveHorizon() {
        return !Double.isNaN(altitude) && altitude > 0.0;
    }

    /**
     * Returns a copy carrying the same catalogue facts but no observed position, for use when
     * observing from a different location or instant.
     */
    public Star copyForObservation() {
        return new Star(
                catalogueId,
                displayName,
                rightAscension,
                declination,
                apparentMagnitude,
                constellationRegion,
                spectralType,
                description,
                type);
    }
}
