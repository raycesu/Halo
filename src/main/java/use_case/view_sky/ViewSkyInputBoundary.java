package use_case.view_sky;

public interface ViewSkyInputBoundary {

    /**
     * Calculates the sky for the observation described by {@code inputData}.
     *
     * @param inputData the observer location and time to build the sky for
     */
    void execute(ViewSkyInputData inputData);
}
