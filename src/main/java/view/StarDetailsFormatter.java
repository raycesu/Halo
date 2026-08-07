package view;

import java.util.Locale;

import entity.Star;

/** Formats a {@link Star}'s current details into the multi-line text shown in the sidebar. */
final class StarDetailsFormatter {

    private static final String DEGREES_FORMAT = "%.2f°";

    private StarDetailsFormatter() {
    }

    static String formatObjectDetails(final Star object) {
        if (object == null) {
            return "";
        }

        final StringBuilder details = new StringBuilder();
        appendIdentityFields(details, object);
        appendCoordinateFields(details, object);
        appendDescription(details, object);

        if (details.length() == 0) {
            details.append("Details unavailable");
        }
        return details.toString();
    }

    private static void appendIdentityFields(final StringBuilder details, final Star object) {
        if (object.getType() != null) {
            appendDetail(details, "Type", formatType(object));
        }
        appendDetail(details, "Catalogue ID", object.getCatalogueId());
        if (Double.isFinite(object.getApparentMagnitude())) {
            appendDetail(details, "Magnitude",
                    String.format(Locale.US, "%.2f", object.getApparentMagnitude()));
        }
        appendDetail(details, "Constellation", object.getConstellationRegion());
        appendDetail(details, "Spectral type", object.getSpectralType());
    }

    private static void appendCoordinateFields(final StringBuilder details, final Star object) {
        if (Double.isFinite(object.getRightAscension())) {
            appendDetail(details, "Right ascension",
                    String.format(Locale.US, "%.4f hours", object.getRightAscension()));
        }
        if (Double.isFinite(object.getDeclination())) {
            appendDetail(details, "Declination",
                    String.format(Locale.US, DEGREES_FORMAT, object.getDeclination()));
        }
        if (Double.isFinite(object.getAltitude())) {
            appendDetail(details, "Altitude",
                    String.format(Locale.US, DEGREES_FORMAT, object.getAltitude()));
        }
        if (Double.isFinite(object.getAzimuth())) {
            appendDetail(details, "Azimuth",
                    String.format(Locale.US, DEGREES_FORMAT, object.getAzimuth()));
        }
    }

    private static void appendDescription(final StringBuilder details, final Star object) {
        if (object.getDescription() != null && !object.getDescription().isBlank()) {
            if (details.length() > 0) {
                details.append("\n\n");
            }
            details.append("Description:\n").append(object.getDescription());
        }
    }

    private static String formatType(final Star object) {
        final String typeName = object.getType().name().toLowerCase(Locale.US);
        return Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
    }

    private static void appendDetail(
            final StringBuilder details,
            final String label,
            final String value) {
        if (value != null && !value.isBlank()) {
            if (details.length() > 0) {
                details.append('\n');
            }
            details.append(label).append(": ").append(value);
        }
    }
}
