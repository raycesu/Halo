package use_case.custom_constellation;

import java.util.List;

import entity.Star;

public class ConstellationInputData {
    private final String name;
    private final List<Star> selectedStars;

    public ConstellationInputData(final String name, final List<Star> selectedStars) {
        this.name = name;
        this.selectedStars = List.copyOf(selectedStars);
    }

    public String getName() {
        return name;
    }

    public List<Star> getSelectedStars() {
        return selectedStars;
    }
}
