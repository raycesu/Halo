package use_case.view_sky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import entity.CoordinateConverter;
import entity.ObserverLocation;
import entity.Star;
import use_case.sky.CelestialBodyDataAccessInterface;
import use_case.sky.CelestialDataUnavailableException;

/**
 * Builds the sky as seen from one place at one moment.
 *
 * <p>Draws on two sources that answer different questions. The star catalogue knows where fixed
 * stars sit on the celestial sphere but nothing about the observer, so those entries are
 * positioned here with {@link CoordinateConverter}. The ephemeris service knows where the Sun,
 * Moon and planets are, which cannot be derived from a catalogue at all, and reports their
 * altitude and azimuth directly.
 *
 * <p>The two overlap: the ephemeris service also covers several dozen bright stars that the
 * catalogue already holds, so duplicates are removed in favour of the catalogue entry, which
 * carries the richer description and a magnitude.
 */
public class ViewSkyInteractor implements ViewSkyInputBoundary {

    private final StarCatalogDataAccessInterface starCatalogDataAccess;
    private final CelestialBodyDataAccessInterface celestialBodyDataAccess;
    private final ViewSkyOutputBoundary outputBoundary;

    public ViewSkyInteractor(
            final StarCatalogDataAccessInterface starCatalogDataAccess,
            final CelestialBodyDataAccessInterface celestialBodyDataAccess,
            final ViewSkyOutputBoundary outputBoundary) {
        this.starCatalogDataAccess = starCatalogDataAccess;
        this.celestialBodyDataAccess = celestialBodyDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(final ViewSkyInputData inputData) {
        final ObserverLocation location;
        final Instant instant;
        try {
            location = new ObserverLocation(
                    inputData.getLocationName(),
                    inputData.getLatitude(),
                    inputData.getLongitude(),
                    inputData.getZoneId());
            instant = inputData.getObservationDateTime()
                    .atZone(inputData.getZoneId())
                    .toInstant();
        }
        catch (IllegalArgumentException | NullPointerException exception) {
            outputBoundary.prepareFailView(
                    "Could not read the observation details: " + exception.getMessage());
            return;
        }

        final List<Star> observed = new ArrayList<>(
                positionCatalogueStars(starCatalogDataAccess.findAll(), location, instant));

        // Losing the ephemeris costs the planets, not the map, so this is a warning alongside a
        // successful result rather than a trip down the failure path.
        String warningMessage = "";
        try {
            observed.addAll(fetchMovingBodies(location, instant, observed));
        }
        catch (CelestialDataUnavailableException exception) {
            warningMessage = "Showing catalogue stars only: " + exception.getMessage();
        }

        final List<Star> stars = visibleBrightestFirst(observed);
        final ViewSkyOutputData outputData = new ViewSkyOutputData(
                inputData.getLocation(),
                inputData.getDate(),
                inputData.getTime(),
                stars);
        outputBoundary.prepareSuccessView(outputData);

        if (!warningMessage.isEmpty()) {
            outputBoundary.prepareWarning(warningMessage);
        }
    }

    /**
     * Positions every catalogue star for this observer and instant, working on copies so the
     * catalogue's own entries are never given an observed position that would then be wrong for
     * the next request.
     */
    private List<Star> positionCatalogueStars(
            final List<Star> catalogue, final ObserverLocation location, final Instant instant) {

        final List<Star> positioned = new ArrayList<>(catalogue.size());
        for (final Star star : catalogue) {
            final Star observed = star.copyForObservation();
            try {
                CoordinateConverter.applyHorizontalPosition(observed, location, instant);
                positioned.add(observed);
            }
            catch (IllegalArgumentException exception) {
                // A catalogue entry with unusable coordinates is left out rather than costing
                // the user the rest of the map. The add above never ran, so there is nothing
                // to undo here.
            }
        }
        return positioned;
    }

    /**
     * Fetches the moving bodies, dropping any the catalogue has already supplied. The ephemeris
     * service reports names inconsistently cased, so matching is case-insensitive.
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
        for (final Star body : celestialBodyDataAccess.getCelestialBodies(location, instant)) {
            if (knownNames.add(normaliseName(body.getDisplayName()))) {
                bodies.add(body);
            }
        }
        return bodies;
    }

    /**
     * Keeps what is above the horizon and orders it brightest first, so a view that draws or
     * labels only the first so many objects gets the ones that matter.
     *
     * <p>Objects of unknown magnitude sort last. That currently puts the planets behind the
     * stars despite several of them being among the brightest things in the sky, because the
     * ephemeris service reports no photometry.
     */
    private List<Star> visibleBrightestFirst(final List<Star> observed) {
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
