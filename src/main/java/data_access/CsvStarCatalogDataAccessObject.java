package data_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import entity.Star;
import use_case.view_sky.StarCatalogDataAccessInterface;

public class CsvStarCatalogDataAccessObject implements StarCatalogDataAccessInterface {

    private static final String RESOURCE_PATH = "/data/bright_stars.csv";
    private static final String EXPECTED_HEADER =
            "catalogue_id,display_name,right_ascension,declination,"
                    + "apparent_magnitude,constellation_region,spectral_type,description";

    private static final int EXPECTED_COLUMN_COUNT = 8;
    private static final int RIGHT_ASCENSION_COLUMN = 2;
    private static final int DECLINATION_COLUMN = 3;
    private static final int APPARENT_MAGNITUDE_COLUMN = 4;
    private static final int CONSTELLATION_REGION_COLUMN = 5;
    private static final int SPECTRAL_TYPE_COLUMN = 6;
    private static final int DESCRIPTION_COLUMN = 7;

    private final List<Star> catalogue;

    public CsvStarCatalogDataAccessObject() {
        catalogue = loadCatalogue();
    }

    @Override
    public List<Star> findAll() {
        final List<Star> observationStars = new ArrayList<>();
        for (Star star : catalogue) {
            observationStars.add(star.copyForObservation());
        }
        return List.copyOf(observationStars);
    }

    private List<Star> loadCatalogue() {
        final InputStream inputStream =
                CsvStarCatalogDataAccessObject.class.getResourceAsStream(RESOURCE_PATH);
        if (inputStream == null) {
            throw new IllegalStateException(
                    "Star catalogue resource was not found: " + RESOURCE_PATH);
        }

        final List<Star> stars = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            final String header = reader.readLine();
            if (!EXPECTED_HEADER.equals(header)) {
                throw new IllegalStateException(
                        "Unexpected header in star catalogue: " + RESOURCE_PATH);
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isEmpty()) {
                    stars.add(parseStar(line, lineNumber));
                }
            }
        }
        catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read star catalogue resource: " + RESOURCE_PATH,
                    exception);
        }
        return List.copyOf(stars);
    }

    private Star parseStar(final String line, final int lineNumber) {
        final List<String> fields = parseCsvLine(line);
        if (fields.size() != EXPECTED_COLUMN_COUNT) {
            throw new IllegalStateException(
                    "Expected 8 columns in star catalogue at line " + lineNumber
                            + " but found " + fields.size());
        }

        try {
            return new Star.Builder()
                    .catalogueId(fields.get(0))
                    .displayName(fields.get(1))
                    .rightAscension(Double.parseDouble(fields.get(RIGHT_ASCENSION_COLUMN)))
                    .declination(Double.parseDouble(fields.get(DECLINATION_COLUMN)))
                    .apparentMagnitude(Double.parseDouble(fields.get(APPARENT_MAGNITUDE_COLUMN)))
                    .constellationRegion(fields.get(CONSTELLATION_REGION_COLUMN))
                    .spectralType(fields.get(SPECTRAL_TYPE_COLUMN))
                    .description(fields.get(DESCRIPTION_COLUMN))
                    .build();
        }
        catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid numeric field in star catalogue at line " + lineNumber,
                    exception);
        }
    }

    private List<String> parseCsvLine(final String line) {
        final List<String> fields = new ArrayList<>();
        final StringBuilder field = new StringBuilder();
        boolean insideQuotes = false;

        int index = 0;
        while (index < line.length()) {
            final char character = line.charAt(index);
            if (character == '"') {
                if (insideQuotes
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                }
                else {
                    insideQuotes = !insideQuotes;
                }
            }
            else if (character == ',' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            }
            else {
                field.append(character);
            }
            index++;
        }
        fields.add(field.toString());
        return fields;
    }
}
