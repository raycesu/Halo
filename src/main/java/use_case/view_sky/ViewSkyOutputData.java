package use_case.view_sky;

import java.util.List;

import entity.Constellation;
import entity.ObserverLocation;
import entity.Star;

public class ViewSkyOutputData {

    private final String location;
    private final String date;
    private final String time;
    private final double latitude;
    private final double longitude;
    private final ObserverLocation observerLocation;
    private final List<Star> stars;
    private final List<Constellation> staticConstellations;

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final List<Star> stars) {
        this(location, date, time, Double.NaN, Double.NaN, stars, List.of());
    }

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final double latitude,
            final double longitude,
            final List<Star> stars) {
        this(location, date, time, latitude, longitude, stars, List.of());
    }

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final double latitude,
            final double longitude,
            final List<Star> stars,
            final List<Constellation> staticConstellations) {
        this.location = location;
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observerLocation = null;
        this.stars = List.copyOf(stars);
        this.staticConstellations = List.copyOf(staticConstellations);
    }

    /**
     * Carries the observed place as a whole, so that a caller needing to ask another service
     * about it — the weather, say — gets the time zone along with the coordinates instead of
     * having to guess one from the latitude and longitude.
     *
     * @param location the resolved observer location's display name
     * @param date the observation date as an ISO string
     * @param time the observation time as an ISO string
     * @param observerLocation the resolved observer location
     * @param stars the completed star list for this observation
     */
    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final ObserverLocation observerLocation,
            final List<Star> stars) {
        this(location, date, time, observerLocation, stars, List.of());
    }

    public ViewSkyOutputData(
            final String location,
            final String date,
            final String time,
            final ObserverLocation observerLocation,
            final List<Star> stars,
            final List<Constellation> staticConstellations) {
        this.location = location;
        this.date = date;
        this.time = time;
        this.latitude = observerLocation.getLatitude();
        this.longitude = observerLocation.getLongitude();
        this.observerLocation = observerLocation;
        this.stars = List.copyOf(stars);
        this.staticConstellations = List.copyOf(staticConstellations);
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

    /**
     * The observed place, or null when the result was built from coordinates alone.
     *
     * @return the resolved observer location, or null
     */
    public ObserverLocation getObserverLocation() {
        return observerLocation;
    }

    public List<Star> getStars() {
        return stars;
    }

    public List<Constellation> getStaticConstellations() {
        return staticConstellations;
    }
}
