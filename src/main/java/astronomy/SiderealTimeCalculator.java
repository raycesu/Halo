package astronomy;

public final class SiderealTimeCalculator {

    private static final double HOURS_IN_DAY = 24.0;

    /**
     * Calculates the local sidereal time for a given Julian Date and longitude.
     *
     * @param julianDate the Julian Date of the observation
     * @param longitudeDegrees the observer's longitude in decimal degrees, east positive
     * @return the local sidereal time, in decimal hours in [0, 24)
     */
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
        double normalizedHours = hours % HOURS_IN_DAY;
        if (normalizedHours < 0.0) {
            normalizedHours += HOURS_IN_DAY;
        }
        return normalizedHours;
    }
}
