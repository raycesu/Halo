package interface_adapter.view_sky;

import java.util.ArrayList;
import java.util.List;

import entity.Constellation;
import entity.ConstellationLine;
import entity.ObserverLocation;
import entity.Star;
import interface_adapter.check_conditions.CheckConditionsViewModel;
import interface_adapter.custom_constellation.ConstellationDisplayData;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel;
import use_case.view_sky.ViewSkyOutputBoundary;
import use_case.view_sky.ViewSkyOutputData;

public class ViewSkyPresenter implements ViewSkyOutputBoundary {

    private final SkyViewModel skyViewModel;
    private final ObservationSetupViewModel observationSetupViewModel;
    private final CheckConditionsViewModel checkConditionsViewModel;
    private final RankForecastDaysViewModel rankForecastDaysViewModel;

    public ViewSkyPresenter(
            final SkyViewModel skyViewModel,
            final ObservationSetupViewModel observationSetupViewModel,
            final CheckConditionsViewModel checkConditionsViewModel,
            final RankForecastDaysViewModel rankForecastDaysViewModel) {
        this.skyViewModel = skyViewModel;
        this.observationSetupViewModel = observationSetupViewModel;
        this.checkConditionsViewModel = checkConditionsViewModel;
        this.rankForecastDaysViewModel = rankForecastDaysViewModel;
    }

    @Override
    public void prepareSuccessView(final ViewSkyOutputData outputData) {
        skyViewModel.setDisplayedLocation(outputData.getLocation());
        skyViewModel.setDisplayedDate(outputData.getDate());
        skyViewModel.setDisplayedTime(outputData.getTime());
        skyViewModel.setLatitude(outputData.getLatitude());
        skyViewModel.setLongitude(outputData.getLongitude());

        final ObserverLocation observer = outputData.getObserverLocation();
        if (observer != null) {
            skyViewModel.setZoneId(observer.getZoneId().getId());
        }
        else {
            skyViewModel.setZoneId("");
        }

        skyViewModel.setStaticConstellations(
                toConstellationDisplayDataList(outputData.getStaticConstellations()));
        skyViewModel.setStars(toStarDisplayDataList(outputData.getStars()));
        skyViewModel.setSelectedObject(null);
        skyViewModel.setSelectedObjectDetails("");
        skyViewModel.setWarningMessage("");
        skyViewModel.setErrorMessage("");
        observationSetupViewModel.setErrorMessage("");

        checkConditionsViewModel.reset();
        rankForecastDaysViewModel.reset();
    }

    @Override
    public void prepareFailView(final String errorMessage) {
        observationSetupViewModel.setErrorMessage(errorMessage);
    }

    @Override
    public void prepareWarning(final String warningMessage) {
        skyViewModel.setWarningMessage(warningMessage);
    }

    private static List<StarDisplayData> toStarDisplayDataList(final List<Star> stars) {
        final List<StarDisplayData> result = new ArrayList<>(stars.size());
        for (final Star star : stars) {
            result.add(toStarDisplayData(star));
        }
        return result;
    }

    private static List<ConstellationDisplayData> toConstellationDisplayDataList(
            final List<Constellation> constellations) {
        final List<ConstellationDisplayData> result = new ArrayList<>(constellations.size());
        for (final Constellation constellation : constellations) {
            final List<ConstellationDisplayData.Line> lines = new ArrayList<>();
            for (final ConstellationLine line : constellation.getLines()) {
                final Star start = line.getStartStar();
                final Star end = line.getEndStar();
                lines.add(new ConstellationDisplayData.Line(
                        start.getAltitude(),
                        start.getAzimuth(),
                        start.isAboveHorizon(),
                        end.getAltitude(),
                        end.getAzimuth(),
                        end.isAboveHorizon()));
            }
            result.add(new ConstellationDisplayData(constellation.getName(), lines));
        }
        return result;
    }

    private static StarDisplayData toStarDisplayData(final Star star) {
        return new StarDisplayData(
                star.getCatalogueId(),
                star.getDisplayName(),
                star.getRightAscension(),
                star.getDeclination(),
                star.getApparentMagnitude(),
                star.getConstellationRegion(),
                star.getSpectralType(),
                star.getDescription(),
                star.getType().name(),
                star.getAltitude(),
                star.getAzimuth(),
                star.isAboveHorizon());
    }
}
