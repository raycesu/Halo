package entity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstellationTest {

    @Test
    void exposesNameAndLines() {
        final ConstellationLine line = createLine();
        final Constellation constellation =
                new Constellation("Orion", List.of(line));

        assertEquals("Orion", constellation.getName());
        assertEquals(1, constellation.getLines().size());
        assertSame(line, constellation.getLines().get(0));
    }

    @Test
    void copiesTheProvidedList() {
        final List<ConstellationLine> originalLines =
                new ArrayList<>();
        originalLines.add(createLine());

        final Constellation constellation =
                new Constellation("Orion", originalLines);
        originalLines.clear();

        assertEquals(1, constellation.getLines().size());
    }

    @Test
    void returnedLinesCannotBeModified() {
        final Constellation constellation =
                new Constellation("Orion", List.of(createLine()));

        assertThrows(
                UnsupportedOperationException.class,
                () -> constellation.getLines().clear()
        );
    }

    @Test
    void rejectsNullLinesList() {
        assertThrows(
                NullPointerException.class,
                () -> new Constellation("Orion", null)
        );
    }

    private ConstellationLine createLine() {
        final Star startStar = createStar("start");
        final Star endStar = createStar("end");
        return new ConstellationLine(startStar, endStar);
    }

    private Star createStar(final String id) {
        return new Star.Builder()
                .catalogueId(id)
                .displayName(id)
                .rightAscension(1.0)
                .declination(2.0)
                .apparentMagnitude(3.0)
                .constellationRegion("ORI")
                .spectralType("A")
                .description("Test star")
                .build();
    }
}
