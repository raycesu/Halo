package astronomy;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import entity.ObserverLocation;
import entity.Star;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AltAzCalculatorTest {

    private static final double tolerance = 0.000001;

    @Test
    void calculatesZenithAndHorizonPositions() {
        final JulianDateCalculator julianDateCalculator =
                new JulianDateCalculator();
        final SiderealTimeCalculator siderealTimeCalculator =
                new SiderealTimeCalculator();
        final AltAzCalculator calculator = new AltAzCalculator(
                julianDateCalculator,
                siderealTimeCalculator);
        final ObserverLocation observerLocation =
                new ObserverLocation("Equator", 0.0, 0.0, ZoneId.of("UTC"));
        final ZonedDateTime observationTime =
                ZonedDateTime.parse("2000-01-01T12:00:00Z");
        final double julianDate =
                julianDateCalculator.calculate(observationTime.toInstant());
        final double localSiderealTime =
                siderealTimeCalculator.calculateLocalSiderealTime(
                        julianDate,
                        observerLocation.getLongitude());

        final Star zenithStar = createStar(
                "Zenith",
                normalizeHours(localSiderealTime));
        final Star eastHorizonStar = createStar(
                "East",
                normalizeHours(localSiderealTime + 6.0));
        final Star westHorizonStar = createStar(
                "West",
                normalizeHours(localSiderealTime - 6.0));

        calculator.updateHorizontalPositions(
                List.of(zenithStar, eastHorizonStar, westHorizonStar),
                observerLocation,
                observationTime);

        assertEquals(90.0, zenithStar.getAltitude(), tolerance);
        assertEquals(0.0, eastHorizonStar.getAltitude(), tolerance);
        assertEquals(90.0, eastHorizonStar.getAzimuth(), tolerance);
        assertEquals(0.0, westHorizonStar.getAltitude(), tolerance);
        assertEquals(270.0, westHorizonStar.getAzimuth(), tolerance);
    }

    private Star createStar(final String name, final double rightAscension) {
        return new Star.Builder()
                .catalogueId(name)
                .displayName(name)
                .rightAscension(rightAscension)
                .apparentMagnitude(1.0)
                .build();
    }

    private double normalizeHours(final double hours) {
        double normalizedHours = hours % 24.0;
        if (normalizedHours < 0.0) {
            normalizedHours += 24.0;
        }
        return normalizedHours;
    }
}
