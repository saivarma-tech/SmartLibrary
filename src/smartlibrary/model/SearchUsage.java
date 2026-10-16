package smartlibrary.model;

public class SearchUsage {
    private final String query;
    private final long searchedAt;
    public SearchUsage(String query, long searchedAt) { this.query = query; this.searchedAt = searchedAt; }
    public String getQuery() { return query; }
    public long getSearchedAt() { return searchedAt; }
}
