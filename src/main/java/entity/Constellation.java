package entity;

import java.util.List;

public class Constellation {
    private  final String name;
    private final List<ConstellationLine> lines;

    public Constellation(final String name, final List<ConstellationLine> lines) {
        this.name = name;
        this.lines = List.copyOf(lines);
    }

    public String getName() {
        return name;
    }
    public List<ConstellationLine> getLines() {
        return lines;
    }
}
