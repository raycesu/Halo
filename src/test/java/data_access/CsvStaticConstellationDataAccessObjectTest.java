package data_access;

import java.util.List;

import entity.StaticConstellationDefinition;
import entity.StaticConstellationSegment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvStaticConstellationDataAccessObjectTest {

    private static final int EXPECTED_CONSTELLATION_COUNT = 88;
    private static final int EXPECTED_SEGMENT_COUNT = 674;
    private static final int EXPECTED_ORION_SEGMENT_COUNT = 21;

    @Test
    void loadsAllConstellationSegmentsWithHrCatalogueIds() {
        final List<StaticConstellationDefinition> definitions =
                new CsvStaticConstellationDataAccessObject().findAll();

        assertEquals(EXPECTED_CONSTELLATION_COUNT, definitions.size());
        assertEquals(
                EXPECTED_SEGMENT_COUNT,
                definitions.stream()
                        .mapToInt(definition -> definition.getSegments().size())
                        .sum());

        final StaticConstellationDefinition orion = definitions.stream()
                .filter(definition -> "Ori".equals(definition.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(EXPECTED_ORION_SEGMENT_COUNT, orion.getSegments().size());

        for (final StaticConstellationDefinition definition : definitions) {
            for (final StaticConstellationSegment segment : definition.getSegments()) {
                assertTrue(segment.getStartCatalogueId().matches("HR\\d+"));
                assertTrue(segment.getEndCatalogueId().matches("HR\\d+"));
            }
        }
    }
}
