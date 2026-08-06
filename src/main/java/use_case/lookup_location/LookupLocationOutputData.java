package use_case.lookup_location;

import java.util.List;

import entity.ObserverLocation;

public class LookupLocationOutputData {

    private final List<ObserverLocation> results;

    public LookupLocationOutputData(final List<ObserverLocation> results) {
        this.results = results;
    }

    public List<ObserverLocation> getResults() {
        return results;
    }
}
