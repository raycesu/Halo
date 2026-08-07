package interface_adapter.lookup_location;

import use_case.lookup_location.LookupLocationInputBoundary;
import use_case.lookup_location.LookupLocationInputData;

public class LookupLocationController {

    private final LookupLocationInputBoundary inputBoundary;

    public LookupLocationController(final LookupLocationInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    /**
     * Forwards a location search request to the lookup-location use case.
     *
     * @param query the partial or complete location name to search for
     * @param limit the maximum number of suggestions to return
     */
    public void lookupLocation(final String query, final int limit) {
        inputBoundary.execute(new LookupLocationInputData(query, limit));
    }
}
