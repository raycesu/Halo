package use_case.custom_constellation;
import java.util.ArrayList;
import java.util.List;
import entity.Star;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstellationInputDataTest {

    @Test
    void exposesNameAndSelectedStars() {
        final Star star = createStar("1", "Vega");
        final ConstellationInputData inputData =
                new ConstellationInputData(
                        "My Pattern",
                        List.of(star)
                );
        assertEquals("My Pattern", inputData.getName());
        assertEquals(1, inputData.getSelectedStars().size());
        assertSame(star, inputData.getSelectedStars().get(0));
    }

    @Test
    void copiesAndProtectsTheSelectedStarsList() {
        final List<Star> originalStars = new ArrayList<>();
        originalStars.add(createStar("1", "Vega"));
        final ConstellationInputData inputData =
                new ConstellationInputData(
                        "My Pattern",
                        originalStars
                );
        originalStars.clear();
        assertEquals(1, inputData.getSelectedStars().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> inputData.getSelectedStars().clear()
        );
    }

    private Star createStar(
            final String id,
            final String name) {
        return new Star.Builder()
                .catalogueId("Sirius")
                .displayName("Sirius")
                .rightAscension(6.7525)
                .declination(-16.7161)
                .apparentMagnitude(-1.46)
                .constellationRegion("CMA")
                .spectralType("A1V")
                .description("Brightest star")
                .build();
    }
}
