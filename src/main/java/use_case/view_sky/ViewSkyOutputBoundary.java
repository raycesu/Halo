package use_case.view_sky;

public interface ViewSkyOutputBoundary {

    /**
     * Called when the sky was calculated successfully.
     *
     * @param outputData the completed observation results
     */
    void prepareSuccessView(ViewSkyOutputData outputData);

    /**
     * Called instead of {@link #prepareSuccessView} when no map could be produced at all, for
     * example because the observer's coordinates are out of range.
     *
     * @param errorMessage a message describing why the sky could not be calculated
     */
    void prepareFailView(String errorMessage);

    /**
     * Called alongside {@link #prepareSuccessView} when the map was produced but something is
     * missing from it, such as the planets when the ephemeris service cannot be reached.
     *
     * <p>Separate from the failure path because the user still has a usable sky, and separate
     * from the output data so that the result stays exactly the shape the catalogue use case
     * defines.
     *
     * @param warningMessage a message describing what is missing from the produced sky
     */
    void prepareWarning(String warningMessage);
}
