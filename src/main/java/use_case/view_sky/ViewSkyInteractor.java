package use_case.view_sky;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import entity.Constellation;
import entity.ConstellationLine;
import entity.ObserverLocation;
import entity.Star;
import entity.StaticConstellationDefinition;
import entity.StaticConstellationSegment;

/**
 * Builds the sky as seen from one place at one moment.
 *
 * <p>The star catalogue contains fixed equatorial coordinates, so catalogue stars are converted
 * to altitude and azimuth for the chosen observer and time. The ephemeris service supplies moving
 * bodies such as the Sun, Moon, and planets with their observed positions already calculated.
 *
 * <p>The two sources may overlap, so duplicates are removed in favour of the catalogue entry,
 * which carries richer catalogue information.
 */
public class ViewSkyInteractor implements ViewSkyInputBoundary {

    private final StarCatalogDataAccessInterface starCatalogDataAccess;
    private final StaticConstellationDataAccessInterface staticConstellationDataAccess;
    private final CelestialBodyDataAccessInterface celestialBodyDataAccess;
    private final HorizontalCoordinateCalculator coordinateCalculator;
    private final ViewSkyOutputBoundary outputBoundary;

    public ViewSkyInteractor(
            final StarCatalogDataAccessInterface starCatalogDataAccess,
            final StaticConstellationDataAccessInterface staticConstellationDataAccess,
            final CelestialBodyDataAccessInterface celestialBodyDataAccess,
            final HorizontalCoordinateCalculator coordinateCalculator,
            final ViewSkyOutputBoundary outputBoundary) {
        this.starCatalogDataAccess = starCatalogDataAccess;
        this.staticConstellationDataAccess = staticConstellationDataAccess;
        this.celestialBodyDataAccess = celestialBodyDataAccess;
        this.coordinateCalculator = coordinateCalculator;
        this.outputBoundary = outputBoundary;
    }

    /**
     * Compatibility constructor for callers that do not provide built-in constellations.
     *
     * @param starCatalogDataAccess fixed-star catalogue gateway
     * @param celestialBodyDataAccess moving-body gateway
     * @param coordinateCalculator horizontal-coordinate calculator
     * @param outputBoundary output presenter
     */
    public ViewSkyInteractor(
            final StarCatalogDataAccessInterface starCatalogDataAccess,
            final CelestialBodyDataAccessInterface celestialBodyDataAccess,
            final HorizontalCoordinateCalculator coordinateCalculator,
            final ViewSkyOutputBoundary outputBoundary) {
        this(
                starCatalogDataAccess,
                List::of,
                celestialBodyDataAccess,
                coordinateCalculator,
                outputBoundary);
    }

    @Override
    public void execute(final ViewSkyInputData inputData) {
        if (inputData == null || inputData.getObservationDateTime() == null) {
            outputBoundary.prepareFailView(
                    "Could not read the observation details: missing required fields.");
        }
        else {
            ObserverLocation location = null;
            ZonedDateTime observationTime = null;

            try {
                location = new ObserverLocation(
                        inputData.getLocationName(),
                        inputData.getLatitude(),
                        inputData.getLongitude(),
                        inputData.getZoneId());

                observationTime = inputData.getObservationDateTime()
                        .atZone(inputData.getZoneId());
            }
            catch (IllegalArgumentException exception) {
                outputBoundary.prepareFailView(
                        "Could not read the observation details: "
                                + exception.getMessage());
            }

            if (location != null) {
                completeViewSky(location, observationTime);
            }
        }
    }

    /**
     * Finishes the sky computation once the location and observation time are known good.
     *
     * @param location the resolved observer location
     * @param observationTime the resolved observation instant, in the observer's zone
     */
    private void completeViewSky(
            final ObserverLocation location, final ZonedDateTime observationTime) {

        final Instant instant = observationTime.toInstant();

        final List<Star> positionedCatalogueStars = positionCatalogueStars(
                starCatalogDataAccess.findAll(),
                location,
                observationTime);
        final List<Constellation> staticConstellations = resolveStaticConstellations(
                staticConstellationDataAccess.findAll(),
                positionedCatalogueStars);
        final List<Star> observed = new ArrayList<>(positionedCatalogueStars);

        // Losing the ephemeris costs the moving bodies, not the whole map.
        String warningMessage = "";
        try {
            observed.addAll(fetchMovingBodies(location, instant, observed));
        }
        catch (CelestialDataUnavailableException exception) {
            warningMessage =
                    "Showing catalogue stars only: " + exception.getMessage();
        }

        final List<Star> stars = visibleBrightestFirst(observed);

        final ViewSkyOutputData outputData = new ViewSkyOutputData(
                location.getDisplayName(),
                observationTime.toLocalDate().toString(),
                observationTime.toLocalTime().toString(),
                location,
                stars,
                staticConstellations);

        outputBoundary.prepareSuccessView(outputData);

        if (!warningMessage.isEmpty()) {
            outputBoundary.prepareWarning(warningMessage);
        }
    }

    /**
     * Positions catalogue stars using copies so cached catalogue objects are not mutated.
     *
     * @param catalogue the catalogue stars to position
     * @param location the observer location
     * @param observationTime the observation instant, in the observer's zone
     * @return fresh star copies with calculated altitude and azimuth
     */
    private List<Star> positionCatalogueStars(
            final List<Star> catalogue,
            final ObserverLocation location,
            final ZonedDateTime observationTime) {

        final List<Star> positioned =
                new ArrayList<>(catalogue.size());

        for (final Star star : catalogue) {
            positioned.add(star.copyForObservation());
        }

        coordinateCalculator.updateHorizontalPositions(
                positioned,
                location,
                observationTime);

        return positioned;
    }

    private List<Constellation> resolveStaticConstellations(
            final List<StaticConstellationDefinition> definitions,
            final List<Star> positionedStars) {
        final Map<String, Star> starsByCatalogueId = new HashMap<>();
        for (final Star star : positionedStars) {
            starsByCatalogueId.put(star.getCatalogueId(), star);
        }

        final List<Constellation> constellations = new ArrayList<>(definitions.size());
        for (final StaticConstellationDefinition definition : definitions) {
            final List<ConstellationLine> lines = new ArrayList<>();
            for (final StaticConstellationSegment segment : definition.getSegments()) {
                final Star start = starsByCatalogueId.get(segment.getStartCatalogueId());
                final Star end = starsByCatalogueId.get(segment.getEndCatalogueId());
                if (start != null && end != null) {
                    lines.add(new ConstellationLine(start, end));
                }
            }
            constellations.add(new Constellation(definition.getName(), lines));
        }
        return List.copyOf(constellations);
    }

    /**
     * Fetches moving bodies while dropping any objects already supplied by the catalogue.
     *
     * @param location the observer location
     * @param instant the observation instant
     * @param alreadyPresent stars already present, whose names should not be duplicated
     * @return the moving bodies not already present in {@code alreadyPresent}
     * @throws CelestialDataUnavailableException if the ephemeris service could not be reached
     */
    private List<Star> fetchMovingBodies(
            final ObserverLocation location,
            final Instant instant,
            final List<Star> alreadyPresent)
            throws CelestialDataUnavailableException {

        final Set<String> knownNames = new HashSet<>();

        for (final Star star : alreadyPresent) {
            knownNames.add(normaliseName(star.getDisplayName()));
        }

        final List<Star> bodies = new ArrayList<>();

        for (final Star body
                : celestialBodyDataAccess.getCelestialBodies(location, instant)) {
            if (knownNames.add(normaliseName(body.getDisplayName()))) {
                bodies.add(body);
            }
        }

        return bodies;
    }

    /**
     * Keeps objects above the horizon and orders them brightest first.
     *
     * @param observed all observed stars and bodies, whether above or below the horizon
     * @return the visible objects, sorted brightest first
     */
    private List<Star> visibleBrightestFirst(
            final List<Star> observed) {

        final List<Star> visible = new ArrayList<>();

        for (final Star star : observed) {
            if (star.isAboveHorizon()) {
                visible.add(star);
            }
        }

        visible.sort(Comparator
                .comparingDouble(ViewSkyInteractor::sortableMagnitude)
                .thenComparing(Star::getDisplayName));

        return visible;
    }

    private static double sortableMagnitude(final Star star) {
        final double magnitude;

        if (Double.isNaN(star.getApparentMagnitude())) {
            magnitude = Double.POSITIVE_INFINITY;
        }
        else {
            magnitude = star.getApparentMagnitude();
        }

        return magnitude;
    }

    private String normaliseName(final String name) {
        final String normalised;

        if (name == null) {
            normalised = "";
        }
        else {
            normalised = name.trim().toUpperCase(Locale.US);
        }

        return normalised;
    }
}
