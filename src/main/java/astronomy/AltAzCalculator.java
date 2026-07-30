package astronomy;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import entity.ObserverLocation;
import entity.Star;
import use_case.view_sky.HorizontalCoordinateCalculator;

public final class AltAzCalculator
        implements HorizontalCoordinateCalculator {

    private final JulianDateCalculator julianDateCalculator;
    private final SiderealTimeCalculator siderealTimeCalculator;

    public AltAzCalculator(
            final JulianDateCalculator julianDateCalculator,
            final SiderealTimeCalculator siderealTimeCalculator) {
        this.julianDateCalculator = julianDateCalculator;
        this.siderealTimeCalculator = siderealTimeCalculator;
    }

    @Override
    public void updateHorizontalPositions(
            final List<Star> stars,
            final ObserverLocation observerLocation,
            final ZonedDateTime observationTime) {
        final Instant instant = observationTime.toInstant();
        final double julianDate = julianDateCalculator.calculate(instant);
        final double localSiderealTime =
                siderealTimeCalculator.calculateLocalSiderealTime(
                        julianDate,
                        observerLocation.getLongitude());
        final double latitudeRadians =
                Math.toRadians(observerLocation.getLatitude());

        for (Star star : stars) {
            final double hourAngleDegrees = normalizeHourAngle(
                    (localSiderealTime - star.getRightAscension()) * 15.0);
            final double hourAngleRadians =
                    Math.toRadians(hourAngleDegrees);
            final double declinationRadians =
                    Math.toRadians(star.getDeclination());

            final double sinAltitude =
                    Math.cos(hourAngleRadians)
                            * Math.cos(declinationRadians)
                            * Math.cos(latitudeRadians)
                            + Math.sin(declinationRadians)
                            * Math.sin(latitudeRadians);
            final double clampedSinAltitude =
                    Math.max(-1.0, Math.min(1.0, sinAltitude));
            final double altitudeRadians = Math.asin(clampedSinAltitude);
            final double altitudeDegrees = Math.toDegrees(altitudeRadians);

            final double azimuthRadians = Math.atan2(
                    -Math.sin(hourAngleRadians),
                    Math.tan(declinationRadians) * Math.cos(latitudeRadians)
                            - Math.sin(latitudeRadians)
                            * Math.cos(hourAngleRadians));
            final double azimuthDegrees =
                    normalizeAzimuth(Math.toDegrees(azimuthRadians));

            star.updateHorizontalPosition(altitudeDegrees, azimuthDegrees);
        }
    }

    private double normalizeHourAngle(final double hourAngleDegrees) {
        double normalizedHourAngle = (hourAngleDegrees + 180.0) % 360.0;
        if (normalizedHourAngle < 0.0) {
            normalizedHourAngle += 360.0;
        }
        return normalizedHourAngle - 180.0;
    }

    private double normalizeAzimuth(final double azimuthDegrees) {
        double normalizedAzimuth = azimuthDegrees % 360.0;
        if (normalizedAzimuth < 0.0) {
            normalizedAzimuth += 360.0;
        }
        return normalizedAzimuth;
    }
}
