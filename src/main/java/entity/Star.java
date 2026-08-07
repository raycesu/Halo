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

    private Star(final Builder builder) {
        this.catalogueId = builder.catalogueId;
        this.displayName = builder.displayName;
        this.rightAscension = builder.rightAscension;
        this.declination = builder.declination;
        this.apparentMagnitude = builder.apparentMagnitude;
        this.constellationRegion = builder.constellationRegion;
        this.spectralType = builder.spectralType;
        this.description = builder.description;
        this.type = builder.type;
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
     * @param newAltitude degrees above the horizon, in [-90, 90]
     * @param newAzimuth degrees clockwise from true north, in [0, 360)
     * @throws IllegalArgumentException if either value is not finite or is out of range
     */
    public void updateHorizontalPosition(
            final double newAltitude,
            final double newAzimuth) {

        if (!Double.isFinite(newAltitude)
                || newAltitude < MIN_ALTITUDE_DEGREES
                || newAltitude > MAX_ALTITUDE_DEGREES) {
            throw new IllegalArgumentException(
                    "Altitude must be finite and in [-90, 90] but was " + newAltitude);
        }
        if (!Double.isFinite(newAzimuth) || newAzimuth < 0.0 || newAzimuth >= DEGREES_IN_CIRCLE) {
            throw new IllegalArgumentException(
                    "Azimuth must be finite and in [0, 360) but was " + newAzimuth);
        }
        this.altitude = newAltitude;
        this.azimuth = newAzimuth;
    }

    /**
     * Whether this object is above the observer's horizon, and so a candidate for being drawn.
     * An object that has not been observed yet counts as not above the horizon.
     *
     * <p>This is a domain rule rather than a rendering detail: a body below the horizon is
     * blocked by the Earth however the map chooses to draw it.
     *
     * @return true if this object's calculated altitude is above zero degrees
     */
    public boolean isAboveHorizon() {
        return !Double.isNaN(altitude) && altitude > 0.0;
    }

    /**
     * Returns a copy carrying the same catalogue facts but no observed position, for use when
     * observing from a different location or instant.
     *
     * @return a fresh {@link Star} with the same catalogue fields and no calculated position
     */
    public Star copyForObservation() {
        return new Builder()
                .catalogueId(catalogueId)
                .displayName(displayName)
                .rightAscension(rightAscension)
                .declination(declination)
                .apparentMagnitude(apparentMagnitude)
                .constellationRegion(constellationRegion)
                .spectralType(spectralType)
                .description(description)
                .type(type)
                .build();
    }

    /**
     * Builds a {@link Star} one catalogue field at a time instead of through a long
     * constructor argument list. {@link #type} defaults to {@link CelestialBodyType#STAR}
     * when not supplied, matching the previous fixed-star convenience constructor.
     */
    public static final class Builder {

        private String catalogueId = "";
        private String displayName = "";
        private double rightAscension;
        private double declination;
        private double apparentMagnitude;
        private String constellationRegion = "";
        private String spectralType = "";
        private String description = "";
        private CelestialBodyType type = CelestialBodyType.STAR;

        /**
         * Sets the catalogue ID.
         *
         * @param catalogueIdValue the stable catalogue identifier
         * @return this builder
         */
        public Builder catalogueId(final String catalogueIdValue) {
            this.catalogueId = catalogueIdValue;
            return this;
        }

        /**
         * Sets the display name.
         *
         * @param displayNameValue the human-readable name
         * @return this builder
         */
        public Builder displayName(final String displayNameValue) {
            this.displayName = displayNameValue;
            return this;
        }

        /**
         * Sets the right ascension.
         *
         * @param rightAscensionValue right ascension in decimal hours
         * @return this builder
         */
        public Builder rightAscension(final double rightAscensionValue) {
            this.rightAscension = rightAscensionValue;
            return this;
        }

        /**
         * Sets the declination.
         *
         * @param declinationValue declination in decimal degrees
         * @return this builder
         */
        public Builder declination(final double declinationValue) {
            this.declination = declinationValue;
            return this;
        }

        /**
         * Sets the apparent magnitude.
         *
         * @param apparentMagnitudeValue the apparent magnitude
         * @return this builder
         */
        public Builder apparentMagnitude(final double apparentMagnitudeValue) {
            this.apparentMagnitude = apparentMagnitudeValue;
            return this;
        }

        /**
         * Sets the constellation region.
         *
         * @param constellationRegionValue the constellation region name
         * @return this builder
         */
        public Builder constellationRegion(final String constellationRegionValue) {
            this.constellationRegion = constellationRegionValue;
            return this;
        }

        /**
         * Sets the spectral type.
         *
         * @param spectralTypeValue the spectral type
         * @return this builder
         */
        public Builder spectralType(final String spectralTypeValue) {
            this.spectralType = spectralTypeValue;
            return this;
        }

        /**
         * Sets the description.
         *
         * @param descriptionValue the local description text
         * @return this builder
         */
        public Builder description(final String descriptionValue) {
            this.description = descriptionValue;
            return this;
        }

        /**
         * Sets the celestial body type.
         *
         * @param typeValue the celestial body type
         * @return this builder
         */
        public Builder type(final CelestialBodyType typeValue) {
            this.type = typeValue;
            return this;
        }

        /**
         * Builds the {@link Star} from the values collected so far.
         *
         * @return a new {@link Star} instance
         */
        public Star build() {
            return new Star(this);
        }
    }
}
