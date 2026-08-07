package entity;

import java.time.ZoneId;

public final class ObserverLocation {

    private static final double MIN_LATITUDE_DEGREES = -90.0;
    private static final double MAX_LATITUDE_DEGREES = 90.0;
    private static final double MIN_LONGITUDE_DEGREES = -180.0;
    private static final double MAX_LONGITUDE_DEGREES = 180.0;

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
        if (!Double.isFinite(latitude)
                || latitude < MIN_LATITUDE_DEGREES
                || latitude > MAX_LATITUDE_DEGREES) {
            throw new IllegalArgumentException("Latitude must be finite and in [-90, 90].");
        }
        if (!Double.isFinite(longitude)
                || longitude < MIN_LONGITUDE_DEGREES
                || longitude > MAX_LONGITUDE_DEGREES) {
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
