package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entity.CelestialBodyType;
import entity.Star;
import use_case.sky.CelestialDataUnavailableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the celnav response parsing against a response recorded from the live USNO service
 * on 2026-07-30 for Toronto, so the expectations reflect the real payload rather than a guess at
 * its shape. No network access is required to run these.
 */
class UsnoCelestialBodyDataAccessObjectTest {

    private static final String FIXTURE = "/celnav-toronto-2026-07-30.json";

    private UsnoCelestialBodyDataAccessObject dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new UsnoCelestialBodyDataAccessObject();
    }

    @Test
    void parsesEveryBodyExceptAries() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        // The recorded response holds 29 entries, one of which is the First Point of Aries.
        assertEquals(28, bodies.size());
        assertTrue(bodies.stream().noneMatch(body -> "ARIES".equalsIgnoreCase(body.getDisplayName())));
    }

    @Test
    void classifiesSunPlanetsAndStars() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        assertEquals(CelestialBodyType.SUN, findBody(bodies, "Sun").getType());
        assertEquals(CelestialBodyType.PLANET, findBody(bodies, "Venus").getType());
        assertEquals(CelestialBodyType.PLANET, findBody(bodies, "Jupiter").getType());
        assertEquals(CelestialBodyType.STAR, findBody(bodies, "VEGA").getType());
        assertEquals(CelestialBodyType.STAR, findBody(bodies, "Schedar").getType());
    }

    @Test
    void readsAlmanacValuesForTheSun() throws Exception {
        final Star sun = findBody(dataAccess.parseCelestialBodies(readFixture()), "Sun");

        assertEquals(18.319706, sun.getDeclination(), 1e-6);
        assertEquals(16.780519, sun.getAltitude(), 1e-6);
        assertEquals(279.558322, sun.getAzimuth(), 1e-6);
    }

    /**
     * Right ascension is not in the response: celnav reports Greenwich hour angle, and the
     * conversion leans on the Aries entry's hour angle being sidereal time. Checking the result
     * against published catalogue positions is what makes that inference safe to rely on.
     *
     * <p>The tolerance covers precession between the J2000 catalogue epoch and the 2026 date of
     * the recorded response, which is a few tenths of a degree. Polaris is deliberately excluded:
     * right ascension converges near the pole, so its offset is far larger without anything being
     * wrong.
     */
    @Test
    void derivesRightAscensionMatchingPublishedPositions() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        // Catalogue positions in hours, the unit the dataset and Star both use.
        assertRightAscension(bodies, "VEGA", 18.6156);
        assertRightAscension(bodies, "ARCTURUS", 14.2610);
        assertRightAscension(bodies, "ANTARES", 16.4901);
        assertRightAscension(bodies, "SPICA", 13.4199);
        assertRightAscension(bodies, "REGULUS", 10.1395);
        assertRightAscension(bodies, "ALTAIR", 19.8464);
    }

    @Test
    void keepsRightAscensionInHoursRange() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        assertTrue(bodies.stream().allMatch(
                body -> body.getRightAscension() >= 0.0 && body.getRightAscension() < 24.0));
    }

    /**
     * Without the Aries entry there is no sidereal time, so right ascension is unknowable. The
     * body must still come back usable, because altitude and azimuth are what the map draws.
     */
    @Test
    void leavesRightAscensionUnknownWhenAriesIsMissing() throws Exception {
        final String body = "{\"properties\": {\"data\": ["
                + "{\"object\": \"Vega\", \"almanac_data\": "
                + "{\"dec\": 38.8, \"gha\": 14.1, \"hc\": 60.0, \"zn\": 90.0}}"
                + "]}}";

        final Star vega = dataAccess.parseCelestialBodies(body).get(0);

        assertTrue(Double.isNaN(vega.getRightAscension()));
        assertEquals(60.0, vega.getAltitude(), 1e-9);
        assertTrue(vega.isAboveHorizon());
    }

    @Test
    void recordsNavigationalStarNumberAsCatalogueId() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        assertEquals("USNO nav star 3", findBody(bodies, "Schedar").getCatalogueId());
        // Planets and the Sun carry no navigational star number.
        assertEquals("", findBody(bodies, "Venus").getCatalogueId());
    }

    @Test
    void leavesMagnitudeUnknownBecauseTheServiceOmitsIt() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        assertTrue(bodies.stream().allMatch(body -> Double.isNaN(body.getApparentMagnitude())));
    }

    @Test
    void recordedResponseContainsOnlyBodiesAboveTheHorizon() throws Exception {
        final List<Star> bodies = dataAccess.parseCelestialBodies(readFixture());

        // Observed behaviour of the service rather than a guarantee of the gateway contract:
        // celnav omits bodies that have set. The interactor must still apply its own horizon
        // filter, because the local star catalogue will not pre-filter anything.
        assertTrue(bodies.stream().allMatch(Star::isAboveHorizon));
    }

    @Test
    void reportsServiceErrorBody() {
        final CelestialDataUnavailableException exception = assertThrows(
                CelestialDataUnavailableException.class,
                () -> dataAccess.parseCelestialBodies("{\"error\": \"Missing 'time' parameter\"}"));

        assertTrue(exception.getMessage().contains("Missing 'time' parameter"));
    }

    @Test
    void reportsMalformedResponse() {
        assertThrows(
                CelestialDataUnavailableException.class,
                () -> dataAccess.parseCelestialBodies("not json at all"));
    }

    @Test
    void reportsResponseMissingExpectedStructure() {
        assertThrows(
                CelestialDataUnavailableException.class,
                () -> dataAccess.parseCelestialBodies("{\"apiversion\": \"4.0.1\"}"));
    }

    @Test
    void skipsUnusableEntriesRatherThanFailing() throws Exception {
        final String body = "{\"properties\": {\"data\": ["
                + "{\"object\": \"Sun\", \"almanac_data\": "
                + "{\"dec\": 18.3, \"gha\": 163.4, \"hc\": 16.8, \"zn\": 279.6}},"
                // Missing almanac_data entirely.
                + "{\"object\": \"Broken\"},"
                // Altitude outside the range the entity accepts.
                + "{\"object\": \"AlsoBroken\", \"almanac_data\": "
                + "{\"dec\": 1.0, \"gha\": 1.0, \"hc\": 999.0, \"zn\": 10.0}}"
                + "]}}";

        final List<Star> bodies = dataAccess.parseCelestialBodies(body);

        assertEquals(1, bodies.size());
        assertEquals("Sun", bodies.get(0).getDisplayName());
    }

    @Test
    void rejectsResponseWithNoUsableBodies() {
        assertThrows(
                CelestialDataUnavailableException.class,
                () -> dataAccess.parseCelestialBodies("{\"properties\": {\"data\": []}}"));
    }

    @Test
    void wrapsAzimuthReportedAsFullCircle() throws Exception {
        final String body = "{\"properties\": {\"data\": ["
                + "{\"object\": \"Polaris\", \"almanac_data\": "
                + "{\"dec\": 89.0, \"gha\": 100.0, \"hc\": 43.0, \"zn\": 360.0}}"
                + "]}}";

        final List<Star> bodies = dataAccess.parseCelestialBodies(body);

        assertEquals(0.0, bodies.get(0).getAzimuth(), 1e-9);
    }

    @Test
    void horizonRuleTreatsExactlyZeroAltitudeAsBelow() throws Exception {
        final String body = "{\"properties\": {\"data\": ["
                + "{\"object\": \"Rising\", \"almanac_data\": "
                + "{\"dec\": 0.0, \"gha\": 0.0, \"hc\": 0.0, \"zn\": 90.0}}"
                + "]}}";

        final List<Star> bodies = dataAccess.parseCelestialBodies(body);

        assertFalse(bodies.get(0).isAboveHorizon());
    }

    private void assertRightAscension(
            final List<Star> bodies, final String name, final double catalogueHours) {

        final double actual = findBody(bodies, name).getRightAscension();
        final double difference = Math.abs(((actual - catalogueHours + 36.0) % 24.0) - 12.0);
        // One degree of precession slack, expressed in hours.
        assertTrue(
                difference < 1.0 / 15.0,
                name + " right ascension was " + actual + " but expected near " + catalogueHours);
    }

    private Star findBody(final List<Star> bodies, final String name) {
        final Optional<Star> match = bodies.stream()
                .filter(body -> name.equalsIgnoreCase(body.getDisplayName()))
                .findFirst();
        assertTrue(match.isPresent(), "Expected the response to contain " + name);
        return match.get();
    }

    private String readFixture() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(FIXTURE)) {
            if (stream == null) {
                throw new IOException("Missing test fixture " + FIXTURE);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
