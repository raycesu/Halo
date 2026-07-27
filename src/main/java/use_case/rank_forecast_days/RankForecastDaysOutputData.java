package use_case.rank_forecast_days;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankForecastDaysOutputData {

    private final List<RankedDayResult> rankedDays;

    public RankForecastDaysOutputData(final List<RankedDayResult> rankedDays) {
        if (rankedDays == null) {
            this.rankedDays = Collections.emptyList();
        }
        else {
            this.rankedDays = Collections.unmodifiableList(new ArrayList<>(rankedDays));
        }
    }

    public List<RankedDayResult> getRankedDays() {
        return rankedDays;
    }
}
