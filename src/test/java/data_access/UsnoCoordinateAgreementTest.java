package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import entity.CelestialBodyType;
import entity.CoordinateConverter;
import entity.ObserverLocation;
import entity.Star;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks {@link CoordinateConverter} against positions computed independently by the US Naval
 * Observatory.
 *
 * <p>This is the test that makes the converter trustworthy. Its formulae are easy to get subtly
 * wrong in ways that still produce plausible numbers, and a sign error in the azimuth term would
 * mirror the whole sky without any single value looking absurd. Comparing against an outside
 * authority catches what self-consistent tests cannot.
 *
 * <p>The recorded response supplies right ascension and declination for twenty-five navigational
 * stars, along with the altitude and azimuth USNO computed for the same observer and instant.
 * Recomputing those positions locally and requiring agreement pins the conversion end to end.
 *
 * <p>Only fixed stars are compared. Planets move in ways that cannot be derived from a single
 * response, which is exactly why the ephemeris service is worth calling for them at all.
 */
class UsnoCoordinateAgreementTest {

    private static final String FIXTURE = "/celnav-toronto-2026-07-30.json";

    /** The observer and instant the recorded response was requested for. */
    private static final ObserverLocation TORONTO = new ObserverLocation(
            "Toronto", 43.6532, -79.3832, ZoneOffset.UTC.normalized());
    private static final Instant OBSERVED_AT =
            LocalDateTime.of(2026, 7, 30, 23, 0, 0).toInstant(ZoneOffset.UTC);

    /**
     * Observed agreement is within 0.002 degrees in altitude and 0.009 in azimuth. The threshold
     * sits an order of magnitude above that: tight enough that a real error cannot hide beneath
     * it, loose enough not to fail on the last digit of a rounded response.
     */
    private static final double TOLERANCE_DEGREES = 0.05;

    @Test
    void locallyComputedPositionsAgreeWithUsno() throws Exception {
        final List<Star> bodies =
                new UsnoCelestialBodyDataAccessObject().parseCelestialBodies(readFixture());

        int compared = 0;
        for (final Star body : bodies) {
            if (body.getType() != CelestialBodyType.STAR
                    || Double.isNaN(body.getRightAscension())) {
                continue;
            }

            // copyForObservation drops USNO's altitude and azimuth, so the converter cannot
            // accidentally be handed the answer it is meant to derive.
            final Star recomputed = body.copyForObservation();
            CoordinateConverter.applyHorizontalPosition(recomputed, TORONTO, OBSERVED_AT);

            assertTrue(
                    Math.abs(recomputed.getAltitude() - body.getAltitude()) < TOLERANCE_DEGREES,
                    body.getDisplayName() + " altitude: USNO " + body.getAltitude()
                            + " but computed " + recomputed.getAltitude());
            assertTrue(
                    angularDifference(recomputed.getAzimuth(), body.getAzimuth())
                            < TOLERANCE_DEGREES,
                    body.getDisplayName() + " azimuth: USNO " + body.getAzimuth()
                            + " but computed " + recomputed.getAzimuth());
            compared++;
        }

        // Guards against the loop silently comparing nothing if parsing or classification breaks.
        assertTrue(compared >= 20, "Expected at least 20 stars to compare but had " + compared);
    }

    private double angularDifference(final double first, final double second) {
        return Math.abs(((first - second + 540.0) % 360.0) - 180.0);
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
