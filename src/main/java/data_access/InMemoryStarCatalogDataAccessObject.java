package data_access;

import java.util.ArrayList;
import java.util.List;

import entity.Star;
import use_case.view_sky.StarCatalogDataAccessInterface;

public class InMemoryStarCatalogDataAccessObject implements StarCatalogDataAccessInterface {

    private final List<Star> stars = List.of(
            new Star("HR2491", "Sirius", 6.75247222, -16.71611111,
                    -1.46, "CMa", "A1Vm", ""),
            new Star("HR7001", "Vega", 18.61561111, 38.78361111,
                    0.03, "Lyr", "A0Va", ""),
            new Star("HR2061", "Betelgeuse", 5.91952778, 7.40694444,
                    0.50, "Ori", "M1-2Ia-Iab", ""),
            new Star("HR424", "Polaris", 2.53069444, 89.26416667,
                    2.02, "UMi", "F7:Ib-IIv", ""),
            new Star("HR1713", "Rigel", 5.24227778, -8.20166667,
                    0.13, "Ori", "B8Iae:", ""));

    @Override
    public List<Star> findAll() {
        final List<Star> observationStars = new ArrayList<>();
        for (Star star : stars) {
            observationStars.add(star.copyForObservation());
        }
        return List.copyOf(observationStars);
    }
}
