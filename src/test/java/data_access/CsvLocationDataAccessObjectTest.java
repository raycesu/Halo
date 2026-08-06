package data_access;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import entity.ObserverLocation;
import use_case.lookup_location.LocationDataAccessInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvLocationDataAccessObjectTest {

    private static final double COORDINATE_TOLERANCE = 0.05;

    private static LocationDataAccessInterface locationDataAccess;

    @BeforeAll
    static void loadDataset() {
        locationDataAccess = new CsvLocationDataAccessObject();
    }

    @Test
    void resolvesACityToItsCoordinatesAndZone() {
        final List<ObserverLocation> matches = locationDataAccess.findByName("Toronto", 5);

        assertFalse(matches.isEmpty(), "Toronto should be in the bundled dataset.");
        final ObserverLocation toronto = matches.get(0);
        assertEquals("Toronto, Ontario, Canada", toronto.getDisplayName());
        assertEquals(43.70, toronto.getLatitude(), COORDINATE_TOLERANCE);
        assertEquals(-79.40, toronto.getLongitude(), COORDINATE_TOLERANCE);
        assertEquals(ZoneId.of("America/Toronto"), toronto.getZoneId());
    }

    @Test
    void matchingIgnoresCaseAndSurroundingSpace() {
        final ObserverLocation typed = locationDataAccess.findByName("  tOrOnTo ", 1).get(0);
        final ObserverLocation canonical = locationDataAccess.findByName("Toronto", 1).get(0);

        assertEquals(canonical.getDisplayName(), typed.getDisplayName());
    }

    /**
     * The dataset spells names the local way, but a user on an English keyboard types them the
     * reachable way. Without accent folding the suggestion list fails exactly the names that most
     * need suggesting.
     */
    @Test
    void findsAccentedNamesTypedWithoutAccents() {
        final List<ObserverLocation> saoPaulo = locationDataAccess.findByName("sao paulo", 1);
        assertFalse(saoPaulo.isEmpty(), "\"sao paulo\" should find São Paulo.");
        assertTrue(saoPaulo.get(0).getDisplayName().startsWith("São Paulo"));

        final List<ObserverLocation> zurich = locationDataAccess.findByName("zurich", 1);
        assertFalse(zurich.isEmpty(), "\"zurich\" should find Zürich.");
    }

    /** Folding runs on the stored name too, so the accented spelling still works. */
    @Test
    void stillFindsAccentedNamesTypedWithAccents() {
        final List<ObserverLocation> matches = locationDataAccess.findByName("São Paulo", 1);

        assertFalse(matches.isEmpty());
        assertTrue(matches.get(0).getDisplayName().startsWith("São Paulo"));
    }

    /**
     * The ranking that makes an as-you-type list usable: what was typed exactly comes first, so
     * "Ottawa" offers Ottawa itself rather than leading with Ottawa South.
     */
    @Test
    void exactMatchesOutrankPartialOnes() {
        final List<ObserverLocation> matches = locationDataAccess.findByName("Ottawa", 5);

        assertEquals("Ottawa, Ontario, Canada", matches.get(0).getDisplayName(),
                "Expected the exact match first.");
        assertTrue(matches.size() > 1, "Ottawa South should still be offered, just later.");
    }

    @Test
    void ranksTheLargestCityFirstAmongEqualQualityMatches() {
        final List<ObserverLocation> matches = locationDataAccess.findByName("London", 3);

        assertEquals("London, England, United Kingdom", matches.get(0).getDisplayName());
    }

    /** Ambiguous names are what the region and country in the label exist to resolve. */
    @Test
    void distinguishesPlacesThatShareAName() {
        final List<ObserverLocation> matches = locationDataAccess.findByName("Kingston", 8);
        final Set<String> displayNames = new HashSet<>();
        for (final ObserverLocation match : matches) {
            displayNames.add(match.getDisplayName());
        }

        assertTrue(matches.size() > 1, "There is more than one Kingston.");
        assertEquals(matches.size(), displayNames.size(),
                "Every suggestion should be readable as distinct.");
    }

    /**
     * The dataset leans towards North America, where the application's users are, so a mid-sized
     * Canadian city has to resolve and not just the handful of largest ones.
     */
    @Test
    void coversMidSizedCanadianCities() {
        for (final String city : List.of("Hamilton", "Markham", "Kitchener", "Halifax", "Guelph")) {
            assertFalse(locationDataAccess.findByName(city, 1).isEmpty(),
                    city + " should be in the bundled dataset.");
        }
    }

    @Test
    void neverReturnsMoreThanTheRequestedLimit() {
        assertEquals(3, locationDataAccess.findByName("San", 3).size());
    }

    @Test
    void returnsNothingForBlankOrUnmatchedQueries() {
        assertTrue(locationDataAccess.findByName("", 5).isEmpty());
        assertTrue(locationDataAccess.findByName("   ", 5).isEmpty());
        assertTrue(locationDataAccess.findByName(null, 5).isEmpty());
        assertTrue(locationDataAccess.findByName("Nowhereville", 5).isEmpty());
    }

    @Test
    void returnsNothingWhenNoResultsWereAskedFor() {
        assertTrue(locationDataAccess.findByName("Toronto", 0).isEmpty());
        assertTrue(locationDataAccess.findByName("Toronto", -1).isEmpty());
    }

    /** Callers hold onto results, so the list they get must not be something they can corrupt. */
    @Test
    void returnsAnUnmodifiableList() {
        final List<ObserverLocation> matches = locationDataAccess.findByName("Toronto", 5);

        assertThrows(UnsupportedOperationException.class, () -> matches.add(null));
    }

    /**
     * The whole dataset is parsed once at construction, so a row with an unusable coordinate or an
     * unrecognised zone would surface here rather than at some later keystroke.
     */
    @Test
    void loadsTheWholeDatasetWithoutFailing() {
        final LocationDataAccessInterface freshInstance = new CsvLocationDataAccessObject();

        assertNotNull(freshInstance.findByName("Tokyo", 1));
        assertFalse(freshInstance.findByName("Tokyo", 1).isEmpty());
    }

    /** Locations are immutable, so handing out the same instance twice is safe and expected. */
    @Test
    void repeatedLookupsReturnTheSameInstance() {
        assertSame(
                locationDataAccess.findByName("Toronto", 1).get(0),
                locationDataAccess.findByName("Toronto", 1).get(0));
    }
}
