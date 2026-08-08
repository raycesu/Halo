package interface_adapter.custom_constellation;

import java.util.List;

import entity.ConstellationLine;
import entity.CustomConstellation;
import entity.Star;
import org.junit.jupiter.api.Test;
import use_case.custom_constellation.ConstellationOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstellationPresenterTest {

    @Test
    void presentsTheSelectedConstellationColor() {
        final Star first = observedStar("HR1", 40.0, 80.0);
        final Star second = observedStar("HR2", 30.0, 120.0);
        final CustomConstellation constellation = new CustomConstellation(
                "My Pattern",
                "#55D187",
                List.of(new ConstellationLine(first, second)));
        final ConstellationViewModel viewModel = new ConstellationViewModel();
        final ConstellationPresenter presenter = new ConstellationPresenter(viewModel);

        presenter.prepareSuccessView(new ConstellationOutputData(constellation));

        assertEquals("#55D187", viewModel.getConstellations().get(0).getColorHex());
    }

    private Star observedStar(
            final String catalogueId,
            final double altitude,
            final double azimuth) {
        final Star star = new Star.Builder().catalogueId(catalogueId).build();
        star.updateHorizontalPosition(altitude, azimuth);
        return star;
    }
}
