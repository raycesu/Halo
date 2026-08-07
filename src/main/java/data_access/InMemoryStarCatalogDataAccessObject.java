package data_access;

import java.util.ArrayList;
import java.util.List;

import entity.Star;
import use_case.view_sky.StarCatalogDataAccessInterface;

public class InMemoryStarCatalogDataAccessObject implements StarCatalogDataAccessInterface {

    private final List<Star> stars = List.of(
            star("HR2491", "Sirius", 6.75247222, -16.71611111, -1.46, "CMa", "A1Vm"),
            star("HR7001", "Vega", 18.61561111, 38.78361111, 0.03, "Lyr", "A0Va"),
            star("HR2061", "Betelgeuse", 5.91952778, 7.40694444, 0.50, "Ori", "M1-2Ia-Iab"),
            star("HR424", "Polaris", 2.53069444, 89.26416667, 2.02, "UMi", "F7:Ib-IIv"),
            star("HR1713", "Rigel", 5.24227778, -8.20166667, 0.13, "Ori", "B8Iae:"));

    /**
     * Builds a fixed-star catalogue entry. Every catalogue entry here has an empty description,
     * so that field is fixed at "".
     *
     * @param catalogueId the stable catalogue identifier
     * @param displayName the human-readable name
     * @param rightAscension right ascension in decimal hours
     * @param declination declination in decimal degrees
     * @param apparentMagnitude the apparent magnitude
     * @param constellationRegion the constellation region name
     * @param spectralType the spectral type
     * @return a new catalogue {@link Star}
     */
    private static Star star(
            final String catalogueId,
            final String displayName,
            final double rightAscension,
            final double declination,
            final double apparentMagnitude,
            final String constellationRegion,
            final String spectralType) {
        return new Star.Builder()
                .catalogueId(catalogueId)
                .displayName(displayName)
                .rightAscension(rightAscension)
                .declination(declination)
                .apparentMagnitude(apparentMagnitude)
                .constellationRegion(constellationRegion)
                .spectralType(spectralType)
                .build();
    }

    @Override
    public List<Star> findAll() {
        final List<Star> observationStars = new ArrayList<>();
        for (Star star : stars) {
            observationStars.add(star.copyForObservation());
        }
        return List.copyOf(observationStars);
    }
}
