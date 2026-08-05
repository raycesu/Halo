package entity.weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherConditionTest {

    @Test
    void exposesEveryFieldPassedToTheConstructor() {
        final WeatherCondition condition = new WeatherCondition(42.0, 9000.0, 15.0, 3);

        assertEquals(42.0, condition.getCloudCoverPercent(), 1e-9);
        assertEquals(9000.0, condition.getVisibilityMeters(), 1e-9);
        assertEquals(15.0, condition.getPrecipitationProbabilityPercent(), 1e-9);
        assertEquals(3, condition.getWeatherCode());
    }
}
