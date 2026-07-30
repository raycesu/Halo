package use_case.view_sky;

public interface ViewSkyOutputBoundary {

    void prepareSuccessView(ViewSkyOutputData outputData);

    /**
     * Called instead of {@link #prepareSuccessView} when no map could be produced at all, for
     * example because the observer's coordinates are out of range.
     */
    void prepareFailView(String errorMessage);

    /**
     * Called alongside {@link #prepareSuccessView} when the map was produced but something is
     * missing from it, such as the planets when the ephemeris service cannot be reached.
     *
     * <p>Separate from the failure path because the user still has a usable sky, and separate
     * from the output data so that the result stays exactly the shape the catalogue use case
     * defines.
     */
    void prepareWarning(String warningMessage);
}
