package use_case.lookup_location;

public class LookupLocationInputData {

    private final String query;
    private final int limit;

    public LookupLocationInputData(final String query, final int limit) {
        this.query = query;
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public int getLimit() {
        return limit;
    }
}
