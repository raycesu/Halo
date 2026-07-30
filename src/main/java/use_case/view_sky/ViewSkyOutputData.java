package use_case.view_sky;

import java.util.List;

import entity.Star;

public class ViewSkyOutputData {

    private final String location;
    private final String date;
    private final String time;
    private final List<Star> stars;

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final List<Star> stars) {
        this.location = location;
        this.date = date;
        this.time = time;
        this.stars = List.copyOf(stars);
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

    public List<Star> getStars() {
        return stars;
    }
}
