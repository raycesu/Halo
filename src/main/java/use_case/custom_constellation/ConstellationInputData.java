package use_case.custom_constellation;

import java.util.List;

import entity.Star;

public class ConstellationInputData {
    private static final String DEFAULT_COLOR_HEX = "#50B4FF";

    private final String name;
    private final String colorHex;
    private final List<Star> selectedStars;

    public ConstellationInputData(final String name, final List<Star> selectedStars) {
        this(name, DEFAULT_COLOR_HEX, selectedStars);
    }

    public ConstellationInputData(
            final String name,
            final String colorHex,
            final List<Star> selectedStars) {
        this.name = name;
        this.colorHex = colorHex;
        this.selectedStars = List.copyOf(selectedStars);
    }

    public String getName() {
        return name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public List<Star> getSelectedStars() {
        return selectedStars;
    }
}
