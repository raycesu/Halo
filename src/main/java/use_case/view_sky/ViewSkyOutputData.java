package use_case.view_sky;

import java.util.List;

public class ViewSkyOutputData {

    private final String location;
    private final String date;
    private final String time;
    private final List<String> demoStarNames;

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final List<String> demoStarNames) {
        this.location = location;
        this.date = date;
        this.time = time;
        this.demoStarNames = List.copyOf(demoStarNames);
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public List<String> getDemoStarNames() {
        return demoStarNames;
    }
}
