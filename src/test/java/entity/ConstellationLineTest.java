package entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstellationLineTest {

    @Test
    void exposesStartAndEndStars() {
        final Star startStar = createStar("start");
        final Star endStar = createStar("end");

        final ConstellationLine line =
                new ConstellationLine(startStar, endStar);

        assertSame(startStar, line.getStartStar());
        assertSame(endStar, line.getEndStar());
    }

    @Test
    void rejectsNullStartStar() {
        final Star endStar = createStar("end");

        assertThrows(
                NullPointerException.class,
                () -> new ConstellationLine(null, endStar)
        );
    }

    @Test
    void rejectsNullEndStar() {
        final Star startStar = createStar("start");

        assertThrows(
                NullPointerException.class,
                () -> new ConstellationLine(startStar, null)
        );
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
