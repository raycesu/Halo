package data_access;

//This class handles the API call for weather at a given location.
// implements WeatherDataAccessInterface. This is the only file that knows Open-Meteo exists: builds the HTTP request,
// parses the JSON response, maps it into whatever return type the interface expects

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import entity.weather.WeatherCondition;
import use_case.weather.WeatherDataAccessInterface;
import use_case.weather.WeatherUnavailableException;

public class OpenMeteoWeatherDataAccessObject implements WeatherDataAccessInterface {

    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String HOURLY_FIELDS =
            "cloud_cover,visibility,precipitation_probability,weather_code";
    private static final DateTimeFormatter HOUR_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.US);
    private static final int HTTP_OK = 200;
    private static final int MINUTES_TO_ROUND_UP = 30;

    private final HttpClient httpClient;

    public OpenMeteoWeatherDataAccessObject() {
        this(HttpClient.newHttpClient());
    }

    public OpenMeteoWeatherDataAccessObject(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public WeatherCondition getWeatherCondition(
            final double latitude, final double longitude, final LocalDateTime dateTime)
            throws WeatherUnavailableException {
        final String responseBody = sendRequest(buildRequestUrl(latitude, longitude));

        final List<String> times = extractStringArray(responseBody, "time");
        final String targetHour = roundToNearestHour(dateTime).format(HOUR_FORMAT);
        final int index = times.indexOf(targetHour);
        if (index < 0) {
            throw new WeatherUnavailableException(
                    "No forecast data available for " + targetHour + " at the requested location.");
        }

        final List<String> cloudCover = extractNumberArray(responseBody, "cloud_cover");
        final List<String> visibility = extractNumberArray(responseBody, "visibility");
        final List<String> precipitation = extractNumberArray(responseBody, "precipitation_probability");
        final List<String> weatherCode = extractNumberArray(responseBody, "weather_code");

        return new WeatherCondition(
                parseDoubleAt(cloudCover, index, "cloud_cover"),
                parseDoubleAt(visibility, index, "visibility"),
                parseDoubleAt(precipitation, index, "precipitation_probability"),
                (int) parseDoubleAt(weatherCode, index, "weather_code"));
    }

    private String buildRequestUrl(final double latitude, final double longitude) {
        return FORECAST_URL
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&hourly=" + HOURLY_FIELDS
                + "&timezone=auto";
    }

    private String sendRequest(final String requestUrl) throws WeatherUnavailableException {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl)).GET().build();
        final HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException exception) {
            throw new WeatherUnavailableException("Could not reach the weather service.", exception);
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WeatherUnavailableException("Weather request was interrupted.", exception);
        }
        if (response.statusCode() != HTTP_OK) {
            throw new WeatherUnavailableException(
                    "Weather service returned an error (HTTP " + response.statusCode() + ").");
        }
        return response.body();
    }

    private LocalDateTime roundToNearestHour(final LocalDateTime dateTime) {
        final LocalDateTime truncated = dateTime.withSecond(0).withNano(0);
        final LocalDateTime rounded;
        if (truncated.getMinute() >= MINUTES_TO_ROUND_UP) {
            rounded = truncated.plusHours(1).withMinute(0);
        }
        else {
            rounded = truncated.withMinute(0);
        }
        return rounded;
    }

    // The "time" array holds quoted ISO strings (e.g. "2026-07-25T00:00"); strip the quotes.
    private List<String> extractStringArray(final String json, final String fieldName)
            throws WeatherUnavailableException {
        final List<String> values = new ArrayList<>();
        for (final String rawValue : extractRawArray(json, fieldName)) {
            values.add(rawValue.replace("\"", ""));
        }
        return values;
    }

    // The numeric arrays (cloud_cover, visibility, etc.) hold bare numbers, no quotes to strip.
    private List<String> extractNumberArray(final String json, final String fieldName)
            throws WeatherUnavailableException {
        return extractRawArray(json, fieldName);
    }

    // Open-Meteo's hourly block is a flat "fieldName":[v1,v2,...] shape, so a small string scan
    // is enough to pull out each array without pulling in a JSON library dependency.
    private List<String> extractRawArray(final String json, final String fieldName)
            throws WeatherUnavailableException {
        final String marker = "\"" + fieldName + "\":[";
        final int start = json.indexOf(marker);
        if (start < 0) {
            throw new WeatherUnavailableException(
                    "Weather service response is missing '" + fieldName + "'.");
        }
        final int valuesStart = start + marker.length();
        final int valuesEnd = json.indexOf(']', valuesStart);
        if (valuesEnd < 0) {
            throw new WeatherUnavailableException(
                    "Weather service response is malformed for '" + fieldName + "'.");
        }
        final String body = json.substring(valuesStart, valuesEnd).trim();
        final List<String> values = new ArrayList<>();
        if (!body.isEmpty()) {
            for (final String part : body.split(",")) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private double parseDoubleAt(final List<String> values, final int index, final String fieldName)
            throws WeatherUnavailableException {
        if (index >= values.size() || "null".equals(values.get(index))) {
            throw new WeatherUnavailableException(
                    "Weather service did not return '" + fieldName + "' for the requested time.");
        }
        try {
            return Double.parseDouble(values.get(index));
        }
        catch (NumberFormatException exception) {
            throw new WeatherUnavailableException(
                    "Weather service returned an invalid value for '" + fieldName + "'.", exception);
        }
    }
}
