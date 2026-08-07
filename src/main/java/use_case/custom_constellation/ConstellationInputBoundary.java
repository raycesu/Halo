package use_case.custom_constellation;

public interface ConstellationInputBoundary {

    /**
     * Creates and saves a new custom constellation.
     *
     * @param inputData the constellation name and selected stars
     */
    void execute(ConstellationInputData inputData);
}
