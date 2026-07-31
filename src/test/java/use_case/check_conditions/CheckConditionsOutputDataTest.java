package use_case.check_conditions;

import entity.weather.ViewingQualityRating;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckConditionsOutputDataTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final CheckConditionsOutputData outputData = new CheckConditionsOutputData(
                40.0, 9000.0, 15.0, 2, 78.5, ViewingQualityRating.EXCELLENT);

        assertEquals(40.0, outputData.getCloudCoverPercent(), 1e-9);
        assertEquals(9000.0, outputData.getVisibilityMeters(), 1e-9);
        assertEquals(15.0, outputData.getPrecipitationProbabilityPercent(), 1e-9);
        assertEquals(2, outputData.getWeatherCode());
        assertEquals(78.5, outputData.getOverallScore(), 1e-9);
        assertEquals(ViewingQualityRating.EXCELLENT, outputData.getRating());
    }
}
