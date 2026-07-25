package interface_adapter.check_conditions;

// implements CheckConditionsOutputBoundary. Takes CheckConditionsOutputData,
// formats it into display-ready strings/values (e.g. "72% cloud cover", "Rating: Good"),
// and pushes them into the ViewModel.

import java.util.Locale;

import entity.weather.ViewingQualityRating;
import use_case.check_conditions.CheckConditionsOutputBoundary;
import use_case.check_conditions.CheckConditionsOutputData;

public class CheckConditionsPresenter implements CheckConditionsOutputBoundary {

    private static final String EXCELLENT_COLOR = "#2E7D32";
    private static final String GOOD_COLOR = "#66BB6A";
    private static final String FAIR_COLOR = "#FBC02D";
    private static final String POOR_COLOR = "#C62828";

    private final CheckConditionsViewModel viewModel;

    public CheckConditionsPresenter(final CheckConditionsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentConditions(final CheckConditionsOutputData outputData) {
        viewModel.setCloudCoverText(
                String.format(Locale.US, "%.0f%% cloud cover", outputData.getCloudCoverPercent()));
        viewModel.setVisibilityText(
                String.format(Locale.US, "%.0f m visibility", outputData.getVisibilityMeters()));
        viewModel.setPrecipitationText(
                String.format(Locale.US, "%.0f%% chance of precipitation",
                        outputData.getPrecipitationProbabilityPercent()));
        viewModel.setWeatherCodeText("Weather code: " + outputData.getWeatherCode());
        viewModel.setOverallScoreText(
                String.format(Locale.US, "Score: %.0f/100", outputData.getOverallScore()));
        viewModel.setRatingText("Rating: " + capitalize(outputData.getRating()));
        viewModel.setRatingColor(colorFor(outputData.getRating()));
        viewModel.setErrorMessage("");
    }

    @Override
    public void presentError(final String errorMessage) {
        viewModel.setErrorMessage(errorMessage);
    }

    private String capitalize(final ViewingQualityRating rating) {
        final String name = rating.name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.US);
    }

    private String colorFor(final ViewingQualityRating rating) {
        final String color;
        switch (rating) {
            case EXCELLENT:
                color = EXCELLENT_COLOR;
                break;
            case GOOD:
                color = GOOD_COLOR;
                break;
            case FAIR:
                color = FAIR_COLOR;
                break;
            default:
                color = POOR_COLOR;
                break;
        }
        return color;
    }
}
