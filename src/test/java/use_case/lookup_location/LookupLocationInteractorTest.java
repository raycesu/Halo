package use_case.lookup_location;

import entity.ObserverLocation;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LookupLocationInteractorTest {

    @Test
    void passesInputToDataAccessAndPresentsResults() {
        final List<ObserverLocation> expectedResults = List.of(
                new ObserverLocation(
                        "Toronto",
                        43.6532,
                        -79.3832,
                        ZoneId.of("America/Toronto")
                )
        );
        final FakeLocationDataAccess dataAccess =
                new FakeLocationDataAccess(expectedResults);
        final FakeLookupLocationOutputBoundary outputBoundary =
                new FakeLookupLocationOutputBoundary();
        final LookupLocationInteractor interactor =
                new LookupLocationInteractor(dataAccess, outputBoundary);
        interactor.execute(new LookupLocationInputData("Toronto", 5));
        assertEquals("Toronto", dataAccess.requestedQuery);
        assertEquals(5, dataAccess.requestedLimit);
        assertSame(
                expectedResults,
                outputBoundary.outputData.getResults()
        );
    }

    @Test
    void presentsAnEmptyResultList() {
        final List<ObserverLocation> expectedResults = List.of();
        final FakeLocationDataAccess dataAccess =
                new FakeLocationDataAccess(expectedResults);
        final FakeLookupLocationOutputBoundary outputBoundary =
                new FakeLookupLocationOutputBoundary();
        final LookupLocationInteractor interactor =
                new LookupLocationInteractor(dataAccess, outputBoundary);
        interactor.execute(new LookupLocationInputData("", 0));
        assertEquals("", dataAccess.requestedQuery);
        assertEquals(0, dataAccess.requestedLimit);
        assertSame(
                expectedResults,
                outputBoundary.outputData.getResults()
        );
    }

    private static class FakeLocationDataAccess
            implements LocationDataAccessInterface {
        private final List<ObserverLocation> resultsToReturn;
        private String requestedQuery;
        private int requestedLimit;

        FakeLocationDataAccess(
                final List<ObserverLocation> resultsToReturn) {
            this.resultsToReturn = resultsToReturn;
        }

        @Override
        public List<ObserverLocation> findByName(
                final String query,
                final int limit) {
            requestedQuery = query;
            requestedLimit = limit;
            return resultsToReturn;
        }
    }

    private static class FakeLookupLocationOutputBoundary
            implements LookupLocationOutputBoundary {
        private LookupLocationOutputData outputData;

        @Override
        public void present(
                final LookupLocationOutputData outputData) {
            this.outputData = outputData;
        }
    }
}