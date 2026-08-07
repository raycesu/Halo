package use_case.lookup_location;

import entity.ObserverLocation;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class LookupLocationOutputDataTest {

    @Test
    void exposesResultsPassedToTheConstructor() {
        final List<ObserverLocation> results = List.of(
                new ObserverLocation(
                        "Toronto",
                        43.6532,
                        -79.3832,
                        ZoneId.of("America/Toronto")
                )
        );
        final LookupLocationOutputData outputData =
                new LookupLocationOutputData(results);
        assertSame(results, outputData.getResults());
    }
}