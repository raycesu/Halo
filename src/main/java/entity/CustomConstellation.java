package entity;

import java.util.List;

public class CustomConstellation extends Constellation {
    private static final String DEFAULT_COLOR_HEX = "#50B4FF";

    private final String colorHex;

    public CustomConstellation(final String name, final List<ConstellationLine> lines) {
        this(name, DEFAULT_COLOR_HEX, lines);
    }

    public CustomConstellation(
            final String name,
            final String colorHex,
            final List<ConstellationLine> lines) {
        super(name, lines);
        this.colorHex = colorHex;
    }

    public String getColorHex() {
        return colorHex;
    }
}
