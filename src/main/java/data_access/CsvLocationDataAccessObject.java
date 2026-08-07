package data_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import entity.ObserverLocation;
import use_case.lookup_location.LocationDataAccessInterface;

/**
 * Resolves place names against a bundled extract of the GeoNames {@code cities15000} dataset,
 * trimmed to 1,000 places.
 *
 * <p>A local dataset rather than a web service, because unlike weather and ephemerides this data
 * does not change: a city's coordinates are the same today as last year. Keeping it local means
 * the lookup cannot fail on a dropped connection, needs no API budget, and is fast enough to run
 * on every keystroke, which is what makes an as-you-type list of suggestions possible.
 *
 * <p>Which 1,000, in order: the largest cities of North America, weighted towards Canada since
 * that is where the application's users are; then the largest city of every remaining country, so
 * that nowhere on earth is unreachable; then the most populous cities left over. Canadian coverage
 * therefore reaches down to towns of about 37,000, while somewhere small and far away resolves
 * only if it is the biggest place in its country.
 *
 * <p>The trade is coverage, and the remedy is another implementation of
 * {@link LocationDataAccessInterface} backed by a geocoding service. Nothing above this class
 * would need to change.
 */
public class CsvLocationDataAccessObject implements LocationDataAccessInterface {

    private static final String RESOURCE_PATH = "/data/cities.csv";
    private static final String EXPECTED_HEADER =
            "name,region,country,latitude,longitude,timezone,population";
    private static final int EXPECTED_COLUMNS = 7;

    /** Rank of an entry whose name is exactly what was typed. */
    private static final int MATCH_EXACT = 0;

    /** Rank of an entry whose name starts with what was typed. */
    private static final int MATCH_PREFIX = 1;

    /** Rank of an entry that contains what was typed somewhere else in the name. */
    private static final int MATCH_CONTAINS = 2;

    private static final int NO_MATCH = 3;

    /** The combining marks NFD leaves behind once an accented character is decomposed. */
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private final List<Entry> entries;

    public CsvLocationDataAccessObject() {
        entries = loadEntries();
    }

    @Override
    public List<ObserverLocation> findByName(final String query, final int limit) {
        final List<ObserverLocation> matches = new ArrayList<>();
        if (query != null && !query.isBlank() && limit > 0) {
            final String normalisedQuery = normalise(query);

            // One pass per tier rather than collecting and sorting: the file is already ordered by
            // population, so appending in encounter order keeps the largest city first within each
            // tier, and the loop can stop as soon as enough matches are found.
            for (int tier = MATCH_EXACT; tier <= MATCH_CONTAINS && matches.size() < limit; tier++) {
                collectTier(normalisedQuery, tier, limit, matches);
            }
        }
        return List.copyOf(matches);
    }

    private void collectTier(
            final String normalisedQuery,
            final int tier,
            final int limit,
            final List<ObserverLocation> matches) {

        for (final Entry entry : entries) {
            if (matches.size() >= limit) {
                break;
            }
            if (rank(entry.searchName, normalisedQuery) == tier) {
                matches.add(entry.location);
            }
        }
    }

    private int rank(final String searchName, final String normalisedQuery) {
        final int rank;
        if (searchName.equals(normalisedQuery)) {
            rank = MATCH_EXACT;
        }
        else if (searchName.startsWith(normalisedQuery)) {
            rank = MATCH_PREFIX;
        }
        else if (searchName.contains(normalisedQuery)) {
            rank = MATCH_CONTAINS;
        }
        else {
            rank = NO_MATCH;
        }
        return rank;
    }

    private List<Entry> loadEntries() {
        final InputStream inputStream =
                CsvLocationDataAccessObject.class.getResourceAsStream(RESOURCE_PATH);
        if (inputStream == null) {
            throw new IllegalStateException("City dataset resource was not found: " + RESOURCE_PATH);
        }

        final List<Entry> loaded = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            final String header = reader.readLine();
            if (!EXPECTED_HEADER.equals(header)) {
                throw new IllegalStateException("Unexpected header in city dataset: " + RESOURCE_PATH);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    final Entry entry = parseEntry(line);
                    if (entry != null) {
                        loaded.add(entry);
                    }
                }
            }
        }
        catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read city dataset resource: " + RESOURCE_PATH, exception);
        }
        return List.copyOf(loaded);
    }

    /**
     * Converts one row, or returns null if it is unusable.
     *
     * <p>A bad row is skipped rather than failing the load. The star catalogue is hand-curated and
     * small enough that a malformed line there means a mistake worth surfacing loudly; this file
     * is a 34,000-row third-party extract, and losing the whole city list over one row with an
     * unrecognised time zone would be the wrong trade.
     *
     * @param line one raw CSV row from the city dataset
     * @return the parsed entry, or null if the row is malformed or unusable
     */
    private Entry parseEntry(final String line) {
        Entry entry = null;
        final List<String> fields = parseCsvLine(line);
        if (fields.size() == EXPECTED_COLUMNS) {
            try {
                final ObserverLocation location = new ObserverLocation(
                        displayNameOf(fields.get(0), fields.get(1), fields.get(2)),
                        Double.parseDouble(fields.get(3)),
                        Double.parseDouble(fields.get(4)),
                        ZoneId.of(fields.get(5)));
                entry = new Entry(normalise(fields.get(0)), location);
            }
            catch (IllegalArgumentException | DateTimeException exception) {
                entry = null;
            }
        }
        return entry;
    }

    /**
     * Builds the label the user picks from, widening out from the city to the region and country
     * so that the eight places called Springfield can be told apart.
     *
     * @param name the city name
     * @param region the region or state name, possibly blank
     * @param country the country name, possibly blank
     * @return the formatted display label
     */
    private String displayNameOf(final String name, final String region, final String country) {
        final StringBuilder displayName = new StringBuilder(name);
        if (!region.isBlank()) {
            displayName.append(", ").append(region);
        }
        if (!country.isBlank()) {
            displayName.append(", ").append(country);
        }
        return displayName.toString();
    }

    /**
     * Folds a name to the form comparisons happen in, so that "toronto" finds Toronto and, more
     * to the point, "sao paulo" finds São Paulo.
     *
     * <p>Stripping accents matters because the dataset spells names the local way while a user on
     * an English keyboard types them the reachable way. Without this, the places most in need of
     * a suggestion list are the ones it fails to offer.
     *
     * <p>Decomposing to NFD splits an accented character into a base letter followed by combining
     * marks, which the mark category can then remove.
     *
     * @param value the raw text to normalise
     * @return the lowercase, accent-stripped form used for matching
     */
    private String normalise(final String value) {
        final String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
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

    /** A city paired with the pre-folded name searches compare against. */
    private static final class Entry {

        private final String searchName;
        private final ObserverLocation location;

        Entry(final String searchName, final ObserverLocation location) {
            this.searchName = searchName;
            this.location = location;
        }
    }
}
