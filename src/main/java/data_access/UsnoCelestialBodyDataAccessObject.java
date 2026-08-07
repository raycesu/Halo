package data_access;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.CelestialBodyType;
import entity.ObserverLocation;
import entity.Star;
import use_case.view_sky.CelestialBodyDataAccessInterface;
import use_case.view_sky.CelestialDataUnavailableException;

/**
 * Fetches celestial positions from the US Naval Observatory Astronomical Applications API.
 *
 * <p>This is the only class in the project that knows the USNO service exists. Everything above
 * it depends on {@link CelestialBodyDataAccessInterface}, so swapping the ephemeris source or
 * substituting a fake for testing touches nothing but the wiring in the app builder.
 *
 * <p>The endpoint used is {@code /api/celnav}, a celestial navigation service. That shapes what
 * comes back in three ways worth knowing:
 *
 * <ul>
 *   <li>Coverage is the Sun, Moon, the currently observable planets, and the 57 navigational
 *       stars. It is not a general star catalogue, so a local dataset is still needed for the
 *       thousands of fainter stars.</li>
 *   <li>Altitude and azimuth arrive ready to use, so no coordinate conversion happens here. They
 *       are geometric: the {@code altitude_corrections} block the service also returns is what a
 *       navigator would apply to a sextant reading, and is deliberately ignored. Cross-checking
 *       against {@link entity.CoordinateConverter} confirms this, the two agreeing to within
 *       0.01 degrees for the navigational stars.</li>
 *   <li>In practice the service returns only bodies currently above the horizon, and which
 *       planets appear varies with the requested instant. Callers must not rely on that: the
 *       interface promises nothing of the sort, and the local catalogue will not pre-filter.</li>
 * </ul>
 */
public class UsnoCelestialBodyDataAccessObject implements CelestialBodyDataAccessInterface {

    private static final String CELNAV_URL = "https://aa.usno.navy.mil/api/celnav";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US).withZone(ZoneOffset.UTC);

    private static final int HTTP_OK = 200;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);
    private static final double DEGREES_IN_CIRCLE = 360.0;

    /** Right ascension is carried in hours; the sky turns 15 degrees per hour. */
    private static final double DEGREES_PER_HOUR = 15.0;

    private static final Set<String> PLANET_NAMES = Set.of(
            "MERCURY", "VENUS", "MARS", "JUPITER", "SATURN", "URANUS", "NEPTUNE");

    /**
     * The service lists the First Point of Aries alongside the real bodies. It is not an object
     * in the sky but the reference direction hour angles are measured from, so it is never
     * returned as a body. Its Greenwich hour angle is Greenwich apparent sidereal time, which is
     * what makes {@link #rightAscensionFrom} possible.
     */
    private static final String ARIES = "ARIES";
    private static final String ERROR_FIELD = "error";

    private final HttpClient httpClient;

    public UsnoCelestialBodyDataAccessObject() {
        this(HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build());
    }

    public UsnoCelestialBodyDataAccessObject(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public List<Star> getCelestialBodies(final ObserverLocation location, final Instant instant)
            throws CelestialDataUnavailableException {

        if (location == null || instant == null) {
            throw new CelestialDataUnavailableException(
                    "Observer location and instant are required.");
        }

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildRequestUrl(location, instant)))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        final HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException exception) {
            throw new CelestialDataUnavailableException(
                    "Could not reach the celestial data service.", exception);
        }
        catch (InterruptedException exception) {
            // Restore the flag so callers higher up can still observe the interruption.
            Thread.currentThread().interrupt();
            throw new CelestialDataUnavailableException(
                    "The celestial data request was interrupted.", exception);
        }

        if (response.statusCode() != HTTP_OK) {
            throw new CelestialDataUnavailableException(
                    "Celestial data service returned status " + response.statusCode()
                            + ": " + extractErrorMessage(response.body()));
        }

        return parseCelestialBodies(response.body());
    }

    /**
     * Builds the celnav query. The endpoint works in universal time (it reports {@code tz: 0}),
     * so the instant is formatted at UTC rather than in the observer's zone.
     *
     * <p>{@code Locale.US} is not optional here: under a locale that uses a comma as the decimal
     * separator, formatting the coordinates with the default locale would produce a query the
     * service silently misreads.
     *
     * @param location the observer's location
     * @param instant the observation instant
     * @return the fully-built celnav request URL
     */
    private String buildRequestUrl(final ObserverLocation location, final Instant instant) {
        return String.format(
                Locale.US,
                "%s?date=%s&time=%s&coords=%.6f,%.6f",
                CELNAV_URL,
                DATE_FORMAT.format(instant),
                TIME_FORMAT.format(instant),
                location.getLatitude(),
                location.getLongitude());
    }

    /**
     * Converts a celnav response body into domain objects.
     *
     * <p>Package-private so the parsing can be tested against a recorded response without a
     * network call, which is where the interesting failure modes live.
     *
     * @param responseBody the raw celnav JSON response body
     * @return the parsed celestial bodies
     * @throws CelestialDataUnavailableException if the response is malformed or has no usable bodies
     */
    List<Star> parseCelestialBodies(final String responseBody)
            throws CelestialDataUnavailableException {

        final JSONArray dataArray;
        try {
            final JSONObject root = new JSONObject(responseBody);

            // The service can report a problem in the body even on a 200 response.
            if (root.has(ERROR_FIELD)) {
                throw new CelestialDataUnavailableException(
                        "Celestial data service reported: " + root.getString(ERROR_FIELD));
            }

            dataArray = root.getJSONObject("properties").getJSONArray("data");
        }
        catch (JSONException exception) {
            throw new CelestialDataUnavailableException(
                    "Celestial data response was not in the expected format.", exception);
        }

        final double siderealTimeDegrees = findAriesHourAngle(dataArray);

        final List<Star> bodies = new ArrayList<>(dataArray.length());
        for (int index = 0; index < dataArray.length(); index++) {
            final Star body = parseBody(dataArray.optJSONObject(index), siderealTimeDegrees);
            if (body != null) {
                bodies.add(body);
            }
        }

        if (bodies.isEmpty()) {
            throw new CelestialDataUnavailableException(
                    "Celestial data response contained no usable bodies.");
        }
        return bodies;
    }

    /**
     * Returns the Greenwich hour angle of the First Point of Aries, which equals Greenwich
     * apparent sidereal time expressed in degrees, or NaN if the response omits it.
     *
     * @param dataArray the celnav response's {@code properties.data} array
     * @return the Greenwich hour angle of Aries in degrees, or NaN if not found
     */
    private double findAriesHourAngle(final JSONArray dataArray) {
        double hourAngle = Double.NaN;
        for (int index = 0; index < dataArray.length() && Double.isNaN(hourAngle); index++) {
            final JSONObject entry = dataArray.optJSONObject(index);
            if (entry != null && ARIES.equalsIgnoreCase(entry.optString("object", ""))) {
                final JSONObject almanacData = entry.optJSONObject("almanac_data");
                if (almanacData != null) {
                    hourAngle = almanacData.optDouble("gha", Double.NaN);
                }
            }
        }
        return hourAngle;
    }

    /**
     * Converts one entry, or returns null if the entry is the Aries reference point or is
     * unusable. A malformed entry is skipped rather than failing the whole request, so one
     * unexpected object does not cost the user all the others.
     *
     * @param entry one celnav {@code properties.data} array entry
     * @param siderealTimeDegrees the Greenwich apparent sidereal time in degrees
     * @return the parsed body, or null if the entry is unusable
     */
    private Star parseBody(final JSONObject entry, final double siderealTimeDegrees) {
        Star body = null;
        if (entry != null) {
            final String name = entry.optString("object", "").trim();
            final JSONObject almanacData = entry.optJSONObject("almanac_data");

            if (!name.isEmpty() && !ARIES.equalsIgnoreCase(name) && almanacData != null) {
                body = buildBody(name, entry, almanacData, siderealTimeDegrees);
            }
        }
        return body;
    }

    private Star buildBody(
            final String name,
            final JSONObject entry,
            final JSONObject almanacData,
            final double siderealTimeDegrees) {

        Star body = null;
        final double declination = almanacData.optDouble("dec", Double.NaN);
        final double greenwichHourAngle = almanacData.optDouble("gha", Double.NaN);
        final double altitude = almanacData.optDouble("hc", Double.NaN);
        final double azimuth = almanacData.optDouble("zn", Double.NaN);

        if (Double.isFinite(declination) && Double.isFinite(altitude) && Double.isFinite(azimuth)) {
            try {
                body = new Star.Builder()
                        .catalogueId(catalogueIdOf(entry))
                        .displayName(name)
                        .rightAscension(rightAscensionFrom(greenwichHourAngle, siderealTimeDegrees))
                        .declination(declination)
                        // celnav carries no photometry, so brightness is genuinely unknown here
                        // rather than zero. The local catalogue supplies it for its own stars.
                        .apparentMagnitude(Double.NaN)
                        .type(classify(name))
                        .build();
                body.updateHorizontalPosition(altitude, wrapIntoCircle(azimuth));
            }
            catch (IllegalArgumentException exception) {
                // An out-of-range value from the service; skip this body rather than crash.
                body = null;
            }
        }
        return body;
    }

    /**
     * Converts a Greenwich hour angle to right ascension using Greenwich apparent sidereal time,
     * since {@code gha = gast - rightAscension}. Returns NaN when sidereal time is unavailable,
     * which leaves the object still fully usable for drawing: the map needs altitude and azimuth,
     * and those are supplied directly by the service.
     *
     * <p>The result is in hours, matching {@link Star#getRightAscension()} and the catalogue
     * dataset. It is apparent right ascension of date, so it differs from a J2000 catalogue value
     * by accumulated precession, currently a few tenths of a degree.
     *
     * @param greenwichHourAngle the body's Greenwich hour angle in degrees
     * @param siderealTimeDegrees the Greenwich apparent sidereal time in degrees
     * @return the right ascension in hours, or NaN if either input is not finite
     */
    private double rightAscensionFrom(
            final double greenwichHourAngle, final double siderealTimeDegrees) {

        double rightAscension = Double.NaN;
        if (Double.isFinite(greenwichHourAngle) && Double.isFinite(siderealTimeDegrees)) {
            rightAscension =
                    wrapIntoCircle(siderealTimeDegrees - greenwichHourAngle) / DEGREES_PER_HOUR;
        }
        return rightAscension;
    }

    /**
     * Wraps an angle into [0, 360) so that a value the service reports as exactly 360, or as a
     * small negative number, still satisfies the entity's invariants.
     *
     * @param degrees the angle to wrap, in degrees
     * @return the equivalent angle in [0, 360) degrees
     */
    private double wrapIntoCircle(final double degrees) {
        final double wrapped = degrees % DEGREES_IN_CIRCLE;
        final double result;
        if (wrapped < 0.0) {
            result = wrapped + DEGREES_IN_CIRCLE;
        }
        else {
            result = wrapped;
        }
        return result;
    }

    /**
     * Navigational stars carry a number that identifies them across requests, which is the only
     * stable identifier celnav offers. Planets and the Sun have none.
     *
     * @param entry one celnav {@code properties.data} array entry
     * @return the stable catalogue ID, or an empty string if the body has none
     */
    private String catalogueIdOf(final JSONObject entry) {
        final String catalogueId;
        if (entry.has("nav_star_number")) {
            catalogueId = "USNO nav star " + entry.optInt("nav_star_number");
        }
        else {
            catalogueId = "";
        }
        return catalogueId;
    }

    private CelestialBodyType classify(final String name) {
        final String upperCaseName = name.toUpperCase(Locale.US);
        final CelestialBodyType type;
        if ("SUN".equals(upperCaseName)) {
            type = CelestialBodyType.SUN;
        }
        else if ("MOON".equals(upperCaseName)) {
            type = CelestialBodyType.MOON;
        }
        else if (PLANET_NAMES.contains(upperCaseName)) {
            type = CelestialBodyType.PLANET;
        }
        else {
            // Everything else celnav returns is one of the navigational stars.
            type = CelestialBodyType.STAR;
        }
        return type;
    }

    private String extractErrorMessage(final String responseBody) {
        String message = "no details provided";
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                message = new JSONObject(responseBody).optString(ERROR_FIELD, responseBody);
            }
            catch (JSONException exception) {
                message = responseBody;
            }
        }
        return message;
    }
}
