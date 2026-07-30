package astronomy;

import java.time.Instant;

public final class JulianDateCalculator {

    public double calculate(final Instant observationTime) {
        return 2440587.5
                + observationTime.getEpochSecond() / 86400.0
                + observationTime.getNano() / 86_400_000_000_000.0;
    }
}
