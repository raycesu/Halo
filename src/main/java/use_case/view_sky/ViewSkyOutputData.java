package use_case.view_sky;

import java.util.List;

import entity.Star;

public class ViewSkyOutputData {

    private final String location;
    private final String date;
    private final String time;
    private final double latitude;
    private final double longitude;
    private final List<Star> stars;

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final List<Star> stars) {
        this(location, date, time, Double.NaN, Double.NaN, stars);
    }

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final double latitude,
            final double longitude,
            final List<Star> stars) {
        this.location = location;
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Star> getStars() {
        return stars;
    }
}
