package entity;

public final class Star {

    private final String catalogueId;
    private final String displayName;
    private final double rightAscension;
    private final double declination;
    private final double apparentMagnitude;
    private final String constellationRegion;
    private final String spectralType;
    private final String description;

    private double altitude = Double.NaN;
    private double azimuth = Double.NaN;

    public Star(
            final String catalogueId,
            final String displayName,
            final double rightAscension,
            final double declination,
            final double apparentMagnitude,
            final String constellationRegion,
            final String spectralType,
            final String description) {

        this.catalogueId = catalogueId;
        this.displayName = displayName;
        this.rightAscension = rightAscension;
        this.declination = declination;
        this.apparentMagnitude = apparentMagnitude;
        this.constellationRegion = constellationRegion;
        this.spectralType = spectralType;
        this.description = description;
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

    public double getAltitude() {
        return altitude;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void updateHorizontalPosition(
            final double altitude,
            final double azimuth) {
        this.altitude = altitude;
        this.azimuth = azimuth;
    }

    public Star copyForObservation() {
        return new Star(
                catalogueId,
                displayName,
                rightAscension,
                declination,
                apparentMagnitude,
                constellationRegion,
                spectralType,
                description);
    }
}