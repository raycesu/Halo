package interface_adapter.lookup_location;

import use_case.lookup_location.LookupLocationInputBoundary;
import use_case.lookup_location.LookupLocationInputData;

public class LookupLocationController {

    private final LookupLocationInputBoundary inputBoundary;

    public LookupLocationController(final LookupLocationInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void lookupLocation(final String query, final int limit) {
        inputBoundary.execute(new LookupLocationInputData(query, limit));
    }
}
