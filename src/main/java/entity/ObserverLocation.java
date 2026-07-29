package entity;

import java.time.ZoneId;

public final class ObserverLocation {

    private final String displayName;
    private final double latitude;
    private final double longitude;
    private final ZoneId zoneId;

    public ObserverLocation(
            final String displayName,
            final double latitude,
            final double longitude,
            final ZoneId zoneId) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be finite and in [-90, 90].");
        }
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be finite and in [-180, 180].");
        }
        if (zoneId == null) {
            throw new IllegalArgumentException("Zone ID cannot be null.");
        }

        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoneId = zoneId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}
