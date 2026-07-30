package use_case.view_sky;

import java.util.List;

import entity.Star;

public class ViewSkyInteractor implements ViewSkyInputBoundary {

    private final StarCatalogDataAccessInterface starCatalogDataAccess;
    private final ViewSkyOutputBoundary outputBoundary;

    public ViewSkyInteractor(
            final StarCatalogDataAccessInterface starCatalogDataAccess,
            final ViewSkyOutputBoundary outputBoundary) {
        this.starCatalogDataAccess = starCatalogDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(final ViewSkyInputData inputData) {
        final List<Star> stars = starCatalogDataAccess.findAll();
        final ViewSkyOutputData outputData = new ViewSkyOutputData(
                inputData.getLocation(),
                inputData.getDate(),
                inputData.getTime(),
                stars);
        outputBoundary.prepareSuccessView(outputData);
    }
}
