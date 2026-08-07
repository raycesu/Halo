package use_case.lookup_location;

public interface LookupLocationOutputBoundary {

    /**
     * Publishes the location search results.
     *
     * @param outputData the matching locations to present
     */
    void present(LookupLocationOutputData outputData);
}
