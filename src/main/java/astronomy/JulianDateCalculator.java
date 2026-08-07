package astronomy;

import java.time.Instant;

public final class JulianDateCalculator {

    private static final double UNIX_EPOCH_AS_JULIAN_DATE = 2440587.5;
    private static final double SECONDS_PER_DAY = 86400.0;
    private static final double NANOSECONDS_PER_DAY = 86_400_000_000_000.0;

    /**
     * Converts an instant to its Julian Date.
     *
     * @param observationTime the instant to convert
     * @return the Julian Date corresponding to {@code observationTime}
     */
    public double calculate(final Instant observationTime) {
        return UNIX_EPOCH_AS_JULIAN_DATE
                + observationTime.getEpochSecond() / SECONDS_PER_DAY
                + observationTime.getNano() / NANOSECONDS_PER_DAY;
    }
}
