package astronomy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JulianDateCalculatorTest {

    private static final double tolerance = 0.000000001;

    @Test
    void calculatesJulianDateAtUnixEpoch() {
        final JulianDateCalculator calculator = new JulianDateCalculator();

        final double julianDate =
                calculator.calculate(Instant.parse("1970-01-01T00:00:00Z"));

        assertEquals(2440587.5, julianDate, tolerance);
    }

    @Test
    void calculatesJulianDateAtJ2000() {
        final JulianDateCalculator calculator = new JulianDateCalculator();

        final double julianDate =
                calculator.calculate(Instant.parse("2000-01-01T12:00:00Z"));

        assertEquals(2451545.0, julianDate, tolerance);
    }
}
