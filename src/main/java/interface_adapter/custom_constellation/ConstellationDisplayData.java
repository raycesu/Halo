package interface_adapter.custom_constellation;

import java.util.List;

public final class ConstellationDisplayData {
    private static final String DEFAULT_COLOR_HEX = "#50B4FF";

    private final String name;
    private final String colorHex;
    private final List<Line> lines;

    public ConstellationDisplayData(final String name, final List<Line> lines) {
        this(name, DEFAULT_COLOR_HEX, lines);
    }

    public ConstellationDisplayData(
            final String name,
            final String colorHex,
            final List<Line> lines) {
        this.name = name;
        this.colorHex = colorHex;
        this.lines = List.copyOf(lines);
    }

    public String getName() {
        return name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public List<Line> getLines() {
        return lines;
    }

    public static final class Line {

        private final double startAltitude;
        private final double startAzimuth;
        private final boolean startAboveHorizon;
        private final double endAltitude;
        private final double endAzimuth;
        private final boolean endAboveHorizon;

        public Line(
                final double startAltitude,
                final double startAzimuth,
                final boolean startAboveHorizon,
                final double endAltitude,
                final double endAzimuth,
                final boolean endAboveHorizon) {
            this.startAltitude = startAltitude;
            this.startAzimuth = startAzimuth;
            this.startAboveHorizon = startAboveHorizon;
            this.endAltitude = endAltitude;
            this.endAzimuth = endAzimuth;
            this.endAboveHorizon = endAboveHorizon;
        }

        public double getStartAltitude() {
            return startAltitude;
        }

        public double getStartAzimuth() {
            return startAzimuth;
        }

        public boolean isStartAboveHorizon() {
            return startAboveHorizon;
        }

        public double getEndAltitude() {
            return endAltitude;
        }

        public double getEndAzimuth() {
            return endAzimuth;
        }

        public boolean isEndAboveHorizon() {
            return endAboveHorizon;
        }
    }
}
