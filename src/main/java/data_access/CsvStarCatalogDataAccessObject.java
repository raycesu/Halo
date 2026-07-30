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

    private static final String resourcePath = "/data/bright_stars.csv";
    private static final String expectedHeader =
            "catalogue_id,display_name,right_ascension,declination,"
                    + "apparent_magnitude,constellation_region,spectral_type,description";

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
                CsvStarCatalogDataAccessObject.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException(
                    "Star catalogue resource was not found: " + resourcePath);
        }

        final List<Star> stars = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            final String header = reader.readLine();
            if (!expectedHeader.equals(header)) {
                throw new IllegalStateException(
                        "Unexpected header in star catalogue: " + resourcePath);
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
                    "Could not read star catalogue resource: " + resourcePath,
                    exception);
        }
        return List.copyOf(stars);
    }

    private Star parseStar(final String line, final int lineNumber) {
        final List<String> fields = parseCsvLine(line);
        if (fields.size() != 8) {
            throw new IllegalStateException(
                    "Expected 8 columns in star catalogue at line " + lineNumber
                            + " but found " + fields.size());
        }

        try {
            return new Star(
                    fields.get(0),
                    fields.get(1),
                    Double.parseDouble(fields.get(2)),
                    Double.parseDouble(fields.get(3)),
                    Double.parseDouble(fields.get(4)),
                    fields.get(5),
                    fields.get(6),
                    fields.get(7));
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

        for (int index = 0; index < line.length(); index++) {
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
        }
        fields.add(field.toString());
        return fields;
    }
}
