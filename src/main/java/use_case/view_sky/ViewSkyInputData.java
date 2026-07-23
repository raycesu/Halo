package use_case.view_sky;

public class ViewSkyInputData {

    private final String location;
    private final String date;
    private final String time;

    public ViewSkyInputData(final String location, final String date, final String time) {
        this.location = location;
        this.date = date;
        this.time = time;
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
}
