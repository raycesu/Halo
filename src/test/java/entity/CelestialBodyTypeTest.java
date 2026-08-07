package entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CelestialBodyTypeTest {

    @Test
    void containsAllSupportedCelestialBodyTypes() {
        final CelestialBodyType[] expectedTypes = {
                CelestialBodyType.SUN,
                CelestialBodyType.MOON,
                CelestialBodyType.PLANET,
                CelestialBodyType.STAR
        };

        assertArrayEquals(expectedTypes, CelestialBodyType.values());
    }

    @Test
    void resolvesATypeFromItsName() {
        assertEquals(
                CelestialBodyType.PLANET,
                CelestialBodyType.valueOf("PLANET")
        );
    }
}