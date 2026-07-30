package astronomy;

public final class SiderealTimeCalculator {

    public double calculateLocalSiderealTime(
            final double julianDate,
            final double longitudeDegrees) {
        final double jd0 = Math.floor(julianDate - 0.5) + 0.5;
        final double hoursSinceMidnight = (julianDate - jd0) * 24.0;
        final double daysSinceJ2000 = jd0 - 2451545.0;
        final double centuriesSinceJ2000 =
                (julianDate - 2451545.0) / 36525.0;

        final double gmst = 6.697375
                + 0.065709824279 * daysSinceJ2000
                + 1.0027379 * hoursSinceMidnight
                + 0.0000258 * centuriesSinceJ2000 * centuriesSinceJ2000;
        final double localSiderealTime = gmst + longitudeDegrees / 15.0;

        return normalizeHours(localSiderealTime);
    }

    private double normalizeHours(final double hours) {
        double normalizedHours = hours % 24.0;
        if (normalizedHours < 0.0) {
            normalizedHours += 24.0;
        }
        return normalizedHours;
    }
}
