package interface_adapter.view_sky;

import java.util.Objects;

public final class StarDisplayData {

    private final String catalogueId;
    private final String displayName;
    private final double rightAscension;
    private final double declination;
    private final double apparentMagnitude;
    private final String constellationRegion;
    private final String spectralType;
    private final String description;
    private final String type;
    private final double altitude;
    private final double azimuth;
    private final boolean aboveHorizon;

    public StarDisplayData(
            final String catalogueId,
            final String displayName,
            final double rightAscension,
            final double declination,
            final double apparentMagnitude,
            final String constellationRegion,
            final String spectralType,
            final String description,
            final String type,
            final double altitude,
            final double azimuth,
            final boolean aboveHorizon) {
        this.catalogueId = catalogueId;
        this.displayName = displayName;
        this.rightAscension = rightAscension;
        this.declination = declination;
        this.apparentMagnitude = apparentMagnitude;
        this.constellationRegion = constellationRegion;
        this.spectralType = spectralType;
        this.description = description;
        this.type = type;
        this.altitude = altitude;
        this.azimuth = azimuth;
        this.aboveHorizon = aboveHorizon;
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

    public String getType() {
        return type;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public boolean isAboveHorizon() {
        return aboveHorizon;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StarDisplayData)) {
            return false;
        }
        return Objects.equals(catalogueId, ((StarDisplayData) other).catalogueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(catalogueId);
    }
}
