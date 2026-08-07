package use_case.lookup_location;

public interface LookupLocationInputBoundary {

    /**
     * Searches for locations matching the given query.
     *
     * @param inputData the search query and result limit
     */
    void execute(LookupLocationInputData inputData);
}
