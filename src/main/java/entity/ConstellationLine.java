package entity;

import java.util.Objects;

public class ConstellationLine {
    private final Star startStar;
    private final Star endStar;

    public ConstellationLine(final Star startStar, final Star endStar) {
        this.startStar = Objects.requireNonNull(startStar);
        this.endStar = Objects.requireNonNull(endStar);
    }
    public Star getStartStar() {
        return startStar;
    }
    public Star getEndStar() {
        return endStar;
    }
}
