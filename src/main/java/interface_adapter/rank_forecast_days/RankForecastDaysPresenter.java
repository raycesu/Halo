package interface_adapter.rank_forecast_days;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import entity.weather.ViewingQualityRating;
import interface_adapter.rank_forecast_days.RankForecastDaysViewModel.RankedDayDisplayItem;
import use_case.rank_forecast_days.RankForecastDaysOutputBoundary;
import use_case.rank_forecast_days.RankForecastDaysOutputData;
import use_case.rank_forecast_days.RankedDayResult;

public class RankForecastDaysPresenter implements RankForecastDaysOutputBoundary {

    private static final String EXCELLENT_COLOR = "#2E7D32";
    private static final String GOOD_COLOR = "#66BB6A";
    private static final String FAIR_COLOR = "#FBC02D";
    private static final String POOR_COLOR = "#C62828";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US);

    private final RankForecastDaysViewModel viewModel;

    public RankForecastDaysPresenter(final RankForecastDaysViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentRankedDays(final RankForecastDaysOutputData outputData) {
        final List<RankedDayDisplayItem> displayItems = new ArrayList<>();
        for (final RankedDayResult result : outputData.getRankedDays()) {
            displayItems.add(new RankedDayDisplayItem(
                    result.getRank(),
                    result.getDate().format(DATE_FORMAT),
                    "Rating: " + capitalize(result.getRating()),
                    colorFor(result.getRating()),
                    String.format(Locale.US, "Score: %.0f/100", result.getOverallScore())));
        }
        viewModel.setRankedDays(displayItems);
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
