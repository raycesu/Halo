package use_case.custom_constellation;

import entity.CustomConstellation;

public class ConstellationOutputData {

    private final CustomConstellation constellation;

    public ConstellationOutputData(final CustomConstellation constellation) {
        this.constellation = constellation;
    }
    public CustomConstellation getConstellation() {
        return constellation;
    }

}
