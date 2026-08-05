package astronomy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiderealTimeCalculatorTest {

    private static final double tolerance = 0.000001;

    @Test
    void calculatesGreenwichSiderealTimeAtJ2000() {
        final SiderealTimeCalculator calculator = new SiderealTimeCalculator();

        final double siderealTime =
                calculator.calculateLocalSiderealTime(2451545.0, 0.0);

        assertEquals(18.6973749, siderealTime, tolerance);
    }

    @Test
    void adjustsSiderealTimeForEastLongitude() {
        final SiderealTimeCalculator calculator = new SiderealTimeCalculator();

        final double siderealTime =
                calculator.calculateLocalSiderealTime(2451545.0, 15.0);

        assertEquals(19.6973749, siderealTime, tolerance);
    }
}
