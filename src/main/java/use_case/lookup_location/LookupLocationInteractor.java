package use_case.lookup_location;

import java.util.List;

import entity.ObserverLocation;

public class LookupLocationInteractor implements LookupLocationInputBoundary {

    private final LocationDataAccessInterface locationDataAccess;
    private final LookupLocationOutputBoundary outputBoundary;

    public LookupLocationInteractor(
            final LocationDataAccessInterface locationDataAccess,
            final LookupLocationOutputBoundary outputBoundary) {
        this.locationDataAccess = locationDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(final LookupLocationInputData inputData) {
        final List<ObserverLocation> results =
                locationDataAccess.findByName(inputData.getQuery(), inputData.getLimit());
        outputBoundary.present(new LookupLocationOutputData(results));
    }
}
