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

    /**
     * Forwards a custom-constellation creation request to the use case.
     *
     * @param name the name for the new constellation
     * @param selectedStars the stars to connect, in selection order
     */
    public void createConstellation(final String name, final List<Star> selectedStars) {
        inputBoundary.execute(new ConstellationInputData(name, selectedStars));
    }

}
