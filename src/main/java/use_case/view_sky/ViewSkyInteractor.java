package use_case.view_sky;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import entity.ObserverLocation;
import entity.Star;

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
    private final CelestialBodyDataAccessInterface celestialBodyDataAccess;
    private final HorizontalCoordinateCalculator coordinateCalculator;
    private final ViewSkyOutputBoundary outputBoundary;

    public ViewSkyInteractor(
            final StarCatalogDataAccessInterface starCatalogDataAccess,
            final CelestialBodyDataAccessInterface celestialBodyDataAccess,
            final HorizontalCoordinateCalculator coordinateCalculator,
            final ViewSkyOutputBoundary outputBoundary) {
        this.starCatalogDataAccess = starCatalogDataAccess;
        this.celestialBodyDataAccess = celestialBodyDataAccess;
        this.coordinateCalculator = coordinateCalculator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(final ViewSkyInputData inputData) {
        final ObserverLocation location;
        final ZonedDateTime observationTime;
        final Instant instant;

        try {
            location = new ObserverLocation(
                    inputData.getLocationName(),
                    inputData.getLatitude(),
                    inputData.getLongitude(),
                    inputData.getZoneId());

            observationTime = inputData.getObservationDateTime()
                    .atZone(inputData.getZoneId());

            instant = observationTime.toInstant();
        }
        catch (IllegalArgumentException | NullPointerException exception) {
            outputBoundary.prepareFailView(
                    "Could not read the observation details: "
                            + exception.getMessage());
            return;
        }

        final List<Star> observed = new ArrayList<>(
                positionCatalogueStars(
                        starCatalogDataAccess.findAll(),
                        location,
                        observationTime));

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
                stars);

        outputBoundary.prepareSuccessView(outputData);

        if (!warningMessage.isEmpty()) {
            outputBoundary.prepareWarning(warningMessage);
        }
    }

    /**
     * Positions catalogue stars using copies so cached catalogue objects are not mutated.
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

    /**
     * Fetches moving bodies while dropping any objects already supplied by the catalogue.
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

        for (final Star body :
                celestialBodyDataAccess.getCelestialBodies(location, instant)) {
            if (knownNames.add(normaliseName(body.getDisplayName()))) {
                bodies.add(body);
            }
        }

        return bodies;
    }

    /**
     * Keeps objects above the horizon and orders them brightest first.
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
