package interface_adapter.custom_constellation;

import java.util.ArrayList;
import java.util.List;

import entity.CelestialBodyType;
import entity.Star;
import interface_adapter.view_sky.StarDisplayData;
import use_case.custom_constellation.ConstellationInputBoundary;
import use_case.custom_constellation.ConstellationInputData;

public class ConstellationController {
    private final ConstellationInputBoundary inputBoundary;

    public ConstellationController(final ConstellationInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    /**
     * Forwards a custom-constellation creation request to the use case.
     *
     * @param name the name for the new constellation
     * @param selectedStars the stars to connect, in selection order
     */
    public void createConstellation(
            final String name,
            final List<StarDisplayData> selectedStars) {
        final List<Star> entities = new ArrayList<>(selectedStars.size());
        for (final StarDisplayData data : selectedStars) {
            final Star star = new Star.Builder()
                    .catalogueId(data.getCatalogueId())
                    .displayName(data.getDisplayName())
                    .rightAscension(data.getRightAscension())
                    .declination(data.getDeclination())
                    .apparentMagnitude(data.getApparentMagnitude())
                    .constellationRegion(data.getConstellationRegion())
                    .spectralType(data.getSpectralType())
                    .description(data.getDescription())
                    .type(CelestialBodyType.valueOf(data.getType()))
                    .build();
            star.updateHorizontalPosition(data.getAltitude(), data.getAzimuth());
            entities.add(star);
        }
        inputBoundary.execute(new ConstellationInputData(name, entities));
    }
}
