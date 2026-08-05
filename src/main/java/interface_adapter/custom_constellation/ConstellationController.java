package interface_adapter.custom_constellation;

import java.util.List;
import entity.Star;
import use_case.custom_constellation.ConstellationInputBoundary;
import use_case.custom_constellation.ConstellationInputData;

public class ConstellationController {
    private final ConstellationInputBoundary inputBoundary;

    public ConstellationController(final ConstellationInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }
    public void createConstellation(final String name, final List<Star> selectedStars) {
        inputBoundary.execute(new ConstellationInputData(name, selectedStars));
    }

}
