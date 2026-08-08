package entity;

import java.util.List;
import java.util.Objects;

/** A named, immutable set of catalogue-ID segments loaded from project data. */
public final class StaticConstellationDefinition {

    private final String name;
    private final List<StaticConstellationSegment> segments;

    public StaticConstellationDefinition(
            final String name,
            final List<StaticConstellationSegment> segments) {
        this.name = Objects.requireNonNull(name);
        this.segments = List.copyOf(segments);
    }

    public String getName() {
        return name;
    }

    public List<StaticConstellationSegment> getSegments() {
        return segments;
    }
}
