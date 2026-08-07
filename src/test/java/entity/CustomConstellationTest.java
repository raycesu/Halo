package entity;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomConstellationTest {

    @Test
    void createsAConstellationWithTheProvidedNameAndLines() {
        final Star startStar = createStar("start");
        final Star endStar = createStar("end");
        final ConstellationLine line =
                new ConstellationLine(startStar, endStar);

        final CustomConstellation constellation =
                new CustomConstellation(
                        "My Constellation",
                        List.of(line)
                );

        assertTrue(constellation instanceof Constellation);
        assertEquals(
                "My Constellation",
                constellation.getName()
        );
        assertEquals(1, constellation.getLines().size());
        assertSame(line, constellation.getLines().get(0));
    }

    private Star createStar(final String id) {
        return new Star(
                id,
                id,
                1.0,
                2.0,
                3.0,
                "ORI",
                "A",
                "Test star"
        );
    }
}
