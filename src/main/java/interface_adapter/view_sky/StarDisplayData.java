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
        boolean equal = this == other;
        if (!equal && other instanceof StarDisplayData) {
            final StarDisplayData otherStar = (StarDisplayData) other;
            if (hasCatalogueId() && otherStar.hasCatalogueId()) {
                equal = Objects.equals(catalogueId, otherStar.catalogueId);
            }
            else if (!hasCatalogueId() && !otherStar.hasCatalogueId()) {
                equal = Objects.equals(displayName, otherStar.displayName)
                        && Objects.equals(type, otherStar.type);
            }
        }
        return equal;
    }

    @Override
    public int hashCode() {
        final int hashCode;
        if (hasCatalogueId()) {
            hashCode = Objects.hashCode(catalogueId);
        }
        else {
            hashCode = Objects.hash(displayName, type);
        }
        return hashCode;
    }

    private boolean hasCatalogueId() {
        return catalogueId != null && !catalogueId.isBlank();
    }
}
