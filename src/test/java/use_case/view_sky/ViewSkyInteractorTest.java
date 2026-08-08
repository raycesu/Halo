package use_case.view_sky;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import astronomy.AltAzCalculator;
import astronomy.JulianDateCalculator;
import astronomy.SiderealTimeCalculator;
import entity.CelestialBodyType;
import entity.ConstellationLine;
import entity.ObserverLocation;
import entity.Star;
import entity.StaticConstellationDefinition;
import entity.StaticConstellationSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.view_sky.CelestialBodyDataAccessInterface;
import use_case.view_sky.CelestialDataUnavailableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Both data sources are faked here, so these tests never touch the network and never depend on
 * what happens to be in the sky today.
 */
class ViewSkyInteractorTest {

    private static final ZoneId TORONTO_ZONE =
            ZoneId.of("America/Toronto");
    private static final double TORONTO_LATITUDE = 43.6532;
    private static final double TORONTO_LONGITUDE = -79.3832;
    private static final LocalDateTime OBSERVED_AT =
            LocalDateTime.of(2026, 7, 30, 23, 0);

    private FakeStarCatalog catalogue;
    private FakeStaticConstellations staticConstellations;
    private FakeCelestialBodies ephemeris;
    private FakeViewSkyOutputBoundary presenter;
    private HorizontalCoordinateCalculator coordinateCalculator;

    @BeforeEach
    void setUp() {
        catalogue = new FakeStarCatalog();
        staticConstellations = new FakeStaticConstellations();
        ephemeris = new FakeCelestialBodies();
        presenter = new FakeViewSkyOutputBoundary();

        coordinateCalculator = new AltAzCalculator(
                new JulianDateCalculator(),
                new SiderealTimeCalculator());
    }

    @Test
    void passesObservationDetailsThroughToTheOutput() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        execute();

        assertTrue(presenter.successCalled);
        assertEquals("Toronto", presenter.outputData.getLocation());
        assertEquals("2026-07-30", presenter.outputData.getDate());
        assertEquals("23:00", presenter.outputData.getTime());
        assertEquals(TORONTO_LATITUDE, presenter.outputData.getLatitude(), 1e-9);
        assertEquals(TORONTO_LONGITUDE, presenter.outputData.getLongitude(), 1e-9);
        assertNull(presenter.warningMessage);
    }

    @Test
    void passesCatalogueStarsAndObservationToCoordinateCalculator() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        final FakeHorizontalCoordinateCalculator fakeCalculator =
                new FakeHorizontalCoordinateCalculator();
        coordinateCalculator = fakeCalculator;

        execute();

        assertTrue(fakeCalculator.wasCalled);
        assertEquals(
                TORONTO_LATITUDE,
                fakeCalculator.observerLocation.getLatitude(),
                1e-9);
        assertEquals(
                TORONTO_LONGITUDE,
                fakeCalculator.observerLocation.getLongitude(),
                1e-9);
        assertEquals(
                OBSERVED_AT.atZone(TORONTO_ZONE),
                fakeCalculator.observationTime);
        assertEquals(1, fakeCalculator.stars.size());

        assertEquals(
                20.0,
                presenter.outputData.getStars().get(0).getAltitude(),
                1e-9);
        assertEquals(
                120.0,
                presenter.outputData.getStars().get(0).getAzimuth(),
                1e-9);
    }

    @Test
    void resolvesConstellationsUsingPositionedStarsBeforeVisibilityFiltering() {
        catalogue.stars.add(catalogueStar("HR1", "First", 1.0, 10.0, 1.0));
        catalogue.stars.add(catalogueStar("HR2", "Second", 2.0, 20.0, 2.0));
        staticConstellations.definitions.add(new StaticConstellationDefinition(
                "Test",
                List.of(new StaticConstellationSegment("HR1", "HR2"))));
        coordinateCalculator = (stars, location, time) -> {
            stars.get(0).updateHorizontalPosition(30.0, 40.0);
            stars.get(1).updateHorizontalPosition(-20.0, 220.0);
        };

        execute();

        assertEquals(1, presenter.outputData.getStars().size());
        final ConstellationLine line = presenter.outputData
                .getStaticConstellations().get(0).getLines().get(0);
        assertEquals("HR1", line.getStartStar().getCatalogueId());
        assertEquals(30.0, line.getStartStar().getAltitude(), 1e-9);
        assertEquals("HR2", line.getEndStar().getCatalogueId());
        assertEquals(-20.0, line.getEndStar().getAltitude(), 1e-9);
        assertFalse(line.getEndStar().isAboveHorizon());
    }

    @Test
    void skipsAStaticSegmentWhoseEndpointIsMissing() {
        catalogue.stars.add(catalogueStar("HR1", "First", 1.0, 10.0, 1.0));
        staticConstellations.definitions.add(new StaticConstellationDefinition(
                "Test",
                List.of(new StaticConstellationSegment("HR1", "HR404"))));
        coordinateCalculator = new FakeHorizontalCoordinateCalculator();

        execute();

        assertTrue(presenter.successCalled);
        assertEquals(1, presenter.outputData.getStaticConstellations().size());
        assertTrue(presenter.outputData.getStaticConstellations().get(0).getLines().isEmpty());
    }

    @Test
    void positionsCatalogueStarsForTheObserver() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        execute();

        final Star vega = presenter.outputData.getStars().get(0);

        // Vega is high in the east over Toronto on a late July evening.
        assertTrue(
                vega.getAltitude() > 40.0,
                "altitude was " + vega.getAltitude());
        assertTrue(
                vega.getAzimuth() > 0.0
                        && vega.getAzimuth() < 180.0);
        assertEquals(
                18.61561111,
                vega.getRightAscension(),
                1e-9);
    }

    @Test
    void leavesTheCatalogueUnpositioned() {
        final Star vega =
                catalogueStar("Vega", 18.61561111, 38.784, 0.03);
        catalogue.stars.add(vega);

        execute();

        // The interactor must work on copies, or a second request from elsewhere would inherit
        // the first observer's positions.
        assertTrue(Double.isNaN(vega.getAltitude()));
        assertTrue(Double.isNaN(vega.getAzimuth()));
    }

    @Test
    void dropsObjectsBelowTheHorizon() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        // Deep southern declination never rises for a northern observer.
        catalogue.stars.add(
                catalogueStar("Acrux", 12.44333333, -63.099, 0.77));

        execute();

        assertEquals(List.of("Vega"), displayedNames());
    }

    @Test
    void ordersByBrightnessWithUnknownMagnitudesLast() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));
        catalogue.stars.add(
                catalogueStar("Deneb", 20.69053333, 45.280, 1.25));
        catalogue.stars.add(
                catalogueStar("Altair", 19.84640000, 8.868, 0.77));

        ephemeris.bodies.add(
                observedBody(
                        "Jupiter",
                        CelestialBodyType.PLANET,
                        50.0,
                        120.0));

        execute();

        assertEquals(
                List.of("Vega", "Altair", "Deneb", "Jupiter"),
                displayedNames());
    }

    @Test
    void includesMovingBodiesFromTheEphemeris() {
        ephemeris.bodies.add(
                observedBody(
                        "Saturn",
                        CelestialBodyType.PLANET,
                        30.0,
                        150.0));

        execute();

        final Star saturn = presenter.outputData.getStars().get(0);

        assertEquals("Saturn", saturn.getDisplayName());
        assertEquals(CelestialBodyType.PLANET, saturn.getType());
        assertEquals(30.0, saturn.getAltitude(), 1e-9);
        assertEquals(150.0, saturn.getAzimuth(), 1e-9);
    }

    @Test
    void passesTheObserverAndInstantToTheEphemeris() {
        execute();

        assertEquals(
                TORONTO_LATITUDE,
                ephemeris.requestedLocation.getLatitude(),
                1e-9);
        assertEquals(
                TORONTO_LONGITUDE,
                ephemeris.requestedLocation.getLongitude(),
                1e-9);
        assertEquals(
                OBSERVED_AT.atZone(TORONTO_ZONE).toInstant(),
                ephemeris.requestedInstant);
    }

    /**
     * The ephemeris service and the catalogue both carry the bright stars, so without this the
     * map would draw Vega twice.
     */
    @Test
    void prefersTheCatalogueEntryOverADuplicateFromTheEphemeris() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        // Same star, differently cased, and with no magnitude, as the service reports it.
        ephemeris.bodies.add(
                observedBody(
                        "VEGA",
                        CelestialBodyType.STAR,
                        41.96,
                        72.12));

        execute();

        assertEquals(List.of("Vega"), displayedNames());

        // Keeping the catalogue entry preserves the magnitude and description.
        assertEquals(
                0.03,
                presenter.outputData
                        .getStars()
                        .get(0)
                        .getApparentMagnitude(),
                1e-9);
    }

    @Test
    void stillProducesAMapWhenTheEphemerisIsUnavailable() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        ephemeris.failureMessage =
                "Could not reach the celestial data service.";

        execute();

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals(List.of("Vega"), displayedNames());
        assertTrue(
                presenter.warningMessage.contains(
                        "Could not reach the celestial data service."));
    }

    @Test
    void skipsCatalogueEntriesWithUnusableCoordinates() {
        catalogue.stars.add(
                catalogueStar("Broken", Double.NaN, 10.0, 1.0));
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        execute();

        assertEquals(List.of("Vega"), displayedNames());
    }

    @Test
    void failsWhenTheObserverCoordinatesAreOutOfRange() {
        interactor().execute(
                new ViewSkyInputData(
                        "Nowhere",
                        91.0,
                        0.0,
                        TORONTO_ZONE,
                        OBSERVED_AT));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertNull(presenter.outputData);
        assertTrue(presenter.errorMessage.contains("Latitude"));
    }

    @Test
    void succeedsWithAnEmptySkyRatherThanFailing() {
        execute();

        assertTrue(presenter.successCalled);
        assertTrue(presenter.outputData.getStars().isEmpty());
    }

    /**
     * The output carries entities rather than a separate boundary type, so these two tests cover
     * what is left protecting the domain: the list cannot be restructured, and the objects in it
     * are copies whose mutation cannot reach the catalogue.
     */
    @Test
    void returnsAListThatCannotBeRestructured() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        execute();

        assertThrows(
                UnsupportedOperationException.class,
                () -> presenter.outputData.getStars().clear());
    }

    @Test
    void repositioningAReturnedStarDoesNotAffectLaterRequests() {
        catalogue.stars.add(
                catalogueStar("Vega", 18.61561111, 38.784, 0.03));

        execute();

        final double originalAltitude =
                presenter.outputData.getStars().get(0).getAltitude();

        // A misbehaving presenter or view repositioning what it was handed.
        presenter.outputData
                .getStars()
                .get(0)
                .updateHorizontalPosition(1.0, 1.0);

        presenter = new FakeViewSkyOutputBoundary();
        execute();

        assertEquals(
                originalAltitude,
                presenter.outputData.getStars().get(0).getAltitude(),
                1e-9);
    }

    @Test
    void handlesAMovingBodyWithNullName() {
        ephemeris.bodies.add(
                observedBody(
                        null,
                        CelestialBodyType.PLANET,
                        30.0,
                        150.0
                )
        );
        execute();
        assertTrue(presenter.successCalled);
        assertEquals(1, presenter.outputData.getStars().size());
        assertNull(
                presenter.outputData
                        .getStars()
                        .get(0)
                        .getDisplayName()
        );
    }

    private void execute() {
        interactor().execute(
                new ViewSkyInputData(
                        "Toronto",
                        TORONTO_LATITUDE,
                        TORONTO_LONGITUDE,
                        TORONTO_ZONE,
                        OBSERVED_AT));
    }

    private ViewSkyInteractor interactor() {
        return new ViewSkyInteractor(
                catalogue,
                staticConstellations,
                ephemeris,
                coordinateCalculator,
                presenter);
    }

    private List<String> displayedNames() {
        final List<String> names = new ArrayList<>();

        for (final Star object : presenter.outputData.getStars()) {
            names.add(object.getDisplayName());
        }

        return names;
    }

    private static Star catalogueStar(
            final String name,
            final double rightAscension,
            final double declination,
            final double magnitude) {
        return catalogueStar("HIP test", name, rightAscension, declination, magnitude);
    }

    private static Star catalogueStar(
            final String catalogueId,
            final String name,
            final double rightAscension,
            final double declination,
            final double magnitude) {

        return new Star.Builder()
                .catalogueId(catalogueId)
                .displayName(name)
                .rightAscension(rightAscension)
                .declination(declination)
                .apparentMagnitude(magnitude)
                .constellationRegion("region")
                .spectralType("spectral")
                .description("description")
                .build();
    }

    private static Star observedBody(
            final String name,
            final CelestialBodyType type,
            final double altitude,
            final double azimuth) {

        final Star body = new Star.Builder()
                .displayName(name)
                .apparentMagnitude(Double.NaN)
                .type(type)
                .build();

        body.updateHorizontalPosition(altitude, azimuth);
        return body;
    }

    private static class FakeStarCatalog
            implements StarCatalogDataAccessInterface {

        private final List<Star> stars = new ArrayList<>();

        @Override
        public List<Star> findAll() {
            return stars;
        }
    }

    private static class FakeCelestialBodies
            implements CelestialBodyDataAccessInterface {

        private final List<Star> bodies = new ArrayList<>();
        private String failureMessage;
        private ObserverLocation requestedLocation;
        private Instant requestedInstant;

        @Override
        public List<Star> getCelestialBodies(
                final ObserverLocation location,
                final Instant instant)
                throws CelestialDataUnavailableException {

            requestedLocation = location;
            requestedInstant = instant;

            if (failureMessage != null) {
                throw new CelestialDataUnavailableException(
                        failureMessage);
            }

            return bodies;
        }
    }

    private static class FakeStaticConstellations
            implements StaticConstellationDataAccessInterface {

        private final List<StaticConstellationDefinition> definitions = new ArrayList<>();

        @Override
        public List<StaticConstellationDefinition> findAll() {
            return definitions;
        }
    }

    private static class FakeHorizontalCoordinateCalculator
            implements HorizontalCoordinateCalculator {

        private boolean wasCalled;
        private List<Star> stars;
        private ObserverLocation observerLocation;
        private ZonedDateTime observationTime;

        @Override
        public void updateHorizontalPositions(
                final List<Star> stars,
                final ObserverLocation observerLocation,
                final ZonedDateTime observationTime) {

            wasCalled = true;
            this.stars = stars;
            this.observerLocation = observerLocation;
            this.observationTime = observationTime;

            for (final Star star : stars) {
                star.updateHorizontalPosition(20.0, 120.0);
            }
        }
    }

    private static class FakeViewSkyOutputBoundary
            implements ViewSkyOutputBoundary {

        private boolean successCalled;
        private boolean failCalled;
        private ViewSkyOutputData outputData;
        private String errorMessage;
        private String warningMessage;

        @Override
        public void prepareSuccessView(
                final ViewSkyOutputData outputData) {

            successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(
                final String errorMessage) {

            failCalled = true;
            this.errorMessage = errorMessage;
        }

        @Override
        public void prepareWarning(
                final String warningMessage) {

            this.warningMessage = warningMessage;
        }
    }
}
