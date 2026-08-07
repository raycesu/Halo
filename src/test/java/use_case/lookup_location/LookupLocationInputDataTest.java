package use_case.lookup_location;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LookupLocationInputDataTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final LookupLocationInputData inputData =
                new LookupLocationInputData("Toronto", 5);
        assertEquals("Toronto", inputData.getQuery());
        assertEquals(5, inputData.getLimit());
    }
}