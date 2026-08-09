package entity;

import java.util.Objects;

/** A catalogue-ID pair describing one line in a built-in constellation. */
public final class StaticConstellationSegment {

    private final String startCatalogueId;
    private final String endCatalogueId;

    public StaticConstellationSegment(
            final String startCatalogueId,
            final String endCatalogueId) {
        this.startCatalogueId = Objects.requireNonNull(startCatalogueId);
        this.endCatalogueId = Objects.requireNonNull(endCatalogueId);
    }

    public String getStartCatalogueId() {
        return startCatalogueId;
    }

    public String getEndCatalogueId() {
        return endCatalogueId;
    }
}
