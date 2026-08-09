package use_case.view_sky;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import entity.ObserverLocation;
import entity.Star;
import entity.Constellation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewSkyOutputDataTest {

    @Test
    void basicConstructorUsesUnknownCoordinates() {
        final Star star = createStar();
        final ViewSkyOutputData outputData =
                new ViewSkyOutputData(
                        "Toronto",
                        "2026-08-07",
                        "23:30",
                        List.of(star)
                );
        assertEquals("Toronto", outputData.getLocation());
        assertEquals("2026-08-07", outputData.getDate());
        assertEquals("23:30", outputData.getTime());
        assertTrue(Double.isNaN(outputData.getLatitude()));
        assertTrue(Double.isNaN(outputData.getLongitude()));
        assertNull(outputData.getObserverLocation());
        assertSame(star, outputData.getStars().get(0));
    }

    @Test
    void coordinateConstructorExposesCoordinates() {
        final ViewSkyOutputData outputData =
                new ViewSkyOutputData(
                        "Toronto",
                        "2026-08-07",
                        "23:30",
                        43.6532,
                        -79.3832,
                        List.of()
                );
        assertEquals(43.6532, outputData.getLatitude(), 1e-9);
        assertEquals(-79.3832, outputData.getLongitude(), 1e-9);
        assertNull(outputData.getObserverLocation());
    }

    @Test
    void observerLocationConstructorCarriesWholeLocation() {
        final ObserverLocation location =
                new ObserverLocation(
                        "Toronto",
                        43.6532,
                        -79.3832,
                        ZoneId.of("America/Toronto")
                );
        final ViewSkyOutputData outputData =
                new ViewSkyOutputData(
                        "Toronto",
                        "2026-08-07",
                        "23:30",
                        location,
                        List.of()
                );
        assertSame(location, outputData.getObserverLocation());
        assertEquals(43.6532, outputData.getLatitude(), 1e-9);
        assertEquals(-79.3832, outputData.getLongitude(), 1e-9);
        assertEquals(
                ZoneId.of("America/Toronto"),
                outputData.getObserverLocation().getZoneId()
        );
    }

    @Test
    void copiesAndProtectsTheStarsList() {
        final List<Star> originalStars = new ArrayList<>();
        originalStars.add(createStar());
        final ViewSkyOutputData outputData =
                new ViewSkyOutputData(
                        "Toronto",
                        "2026-08-07",
                        "23:30",
                        originalStars
                );
        originalStars.clear();
        assertEquals(1, outputData.getStars().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> outputData.getStars().clear()
        );
    }

    @Test
    void observerLocationAndStaticConstellationsConstructorUsesEmptyCustomList() {
        final ObserverLocation location =
                new ObserverLocation(
                        "Toronto",
                        43.6532,
                        -79.3832,
                        ZoneId.of("America/Toronto"));

        final Constellation constellation =
                new Constellation("Test", List.of());

        final ViewSkyOutputData outputData =
                new ViewSkyOutputData(
                        "Toronto",
                        "2026-08-07",
                        "23:30",
                        location,
                        List.of(),
                        List.of(constellation));

        assertSame(location, outputData.getObserverLocation());
        assertEquals(
                List.of(constellation),
                outputData.getStaticConstellations());
        assertTrue(outputData.getCustomConstellations().isEmpty());
    }

    private Star createStar() {
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
