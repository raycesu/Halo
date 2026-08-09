package data_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import entity.StaticConstellationDefinition;
import entity.StaticConstellationSegment;
import use_case.view_sky.StaticConstellationDataAccessInterface;

/** Loads the built-in constellation line definitions from the classpath CSV resource. */
public final class CsvStaticConstellationDataAccessObject
        implements StaticConstellationDataAccessInterface {

    private static final String RESOURCE_PATH = "/data/constellation_lines.csv";
    private static final String EXPECTED_HEADER = "constellation,segments";

    private final List<StaticConstellationDefinition> definitions;

    public CsvStaticConstellationDataAccessObject() {
        definitions = loadDefinitions();
    }

    @Override
    public List<StaticConstellationDefinition> findAll() {
        return definitions;
    }

    private List<StaticConstellationDefinition> loadDefinitions() {
        final InputStream inputStream = CsvStaticConstellationDataAccessObject.class
                .getResourceAsStream(RESOURCE_PATH);
        if (inputStream == null) {
            throw new IllegalStateException(
                    "Constellation resource was not found: " + RESOURCE_PATH);
        }

        final List<StaticConstellationDefinition> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            final String header = reader.readLine();
            if (!EXPECTED_HEADER.equals(header)) {
                throw new IllegalStateException(
                        "Unexpected header in constellation resource: " + RESOURCE_PATH);
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    result.add(parseDefinition(line, lineNumber));
                }
            }
        }
        catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read constellation resource: " + RESOURCE_PATH,
                    exception);
        }
        return List.copyOf(result);
    }

    private StaticConstellationDefinition parseDefinition(
            final String line,
            final int lineNumber) {
        final int separator = line.indexOf(',');
        if (separator <= 0 || separator == line.length() - 1
                || line.indexOf(',', separator + 1) >= 0) {
            throw malformedLine(lineNumber);
        }

        final String name = line.substring(0, separator).trim();
        final String segmentText = line.substring(separator + 1).trim();
        final List<StaticConstellationSegment> segments = new ArrayList<>();
        for (final String value : segmentText.split(";")) {
            segments.add(parseSegment(value, lineNumber));
        }
        return new StaticConstellationDefinition(name, segments);
    }

    private StaticConstellationSegment parseSegment(
            final String value,
            final int lineNumber) {
        final int separator = value.indexOf('-');
        if (separator <= 0 || separator == value.length() - 1
                || value.indexOf('-', separator + 1) >= 0) {
            throw malformedLine(lineNumber);
        }

        final String startId = value.substring(0, separator).trim();
        final String endId = value.substring(separator + 1).trim();
        if (startId.isEmpty() || endId.isEmpty()) {
            throw malformedLine(lineNumber);
        }
        return new StaticConstellationSegment(startId, endId);
    }

    private IllegalStateException malformedLine(final int lineNumber) {
        return new IllegalStateException(
                "Malformed constellation data at line " + lineNumber
                        + " in " + RESOURCE_PATH);
    }
}
