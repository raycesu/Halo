package use_case.custom_constellation;

public interface ConstellationOutputBoundary {

    /**
     * Presents a successfully saved constellation.
     *
     * @param outputData the saved constellation
     */
    void prepareSuccessView(ConstellationOutputData outputData);

    /**
     * Presents an error in place of a saved constellation.
     *
     * @param errorMessage a message describing why the constellation could not be saved
     */
    void prepareFailureView(String errorMessage);
}
