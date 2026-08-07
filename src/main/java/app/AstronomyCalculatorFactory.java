package app;

import astronomy.AltAzCalculator;
import astronomy.JulianDateCalculator;
import astronomy.SiderealTimeCalculator;
import use_case.view_sky.HorizontalCoordinateCalculator;

/** Builds the concrete horizontal-coordinate calculator used to position stars. */
final class AstronomyCalculatorFactory {

    private AstronomyCalculatorFactory() {
    }

    static HorizontalCoordinateCalculator build() {
        return new AltAzCalculator(new JulianDateCalculator(), new SiderealTimeCalculator());
    }
}
