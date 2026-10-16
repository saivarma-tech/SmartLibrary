package smartlibrary.model;

public class SearchResult {

    private String type;
    private int id;
    private String title;
    private String author;
    private String matchedIn;
    private int score;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public SearchResult(
            String type,
            int id,
            String title,
            String author,
            String matchedIn) {

        this.type = type;
        this.id = id;
        this.title = title;
        this.author = author;
        this.matchedIn = matchedIn;

        this.score = calculateScore(matchedIn);
    }

    // ==========================================
    // CALCULATE BASE SCORE
    // ==========================================

    private int calculateScore(String matchedIn) {

        if (matchedIn == null) {
            return 0;
        }

        switch (matchedIn.toUpperCase()) {

            case "TITLE":
                return 100;

            case "AUTHOR":
            case "AUTHORS":
                return 80;

            case "CATEGORY":
                return 60;

            case "ABSTRACT":
            case "DESCRIPTION":
                return 40;

            default:
                return 0;
        }
    }

    // ==========================================
    // SET SCORE
    // ==========================================

    public void setScore(int score) {
        this.score = score;
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getMatchedIn() {
        return matchedIn;
    }

    public int getScore() {
        return score;
    }

    // ==========================================
    // TO STRING
    // ==========================================

    @Override
    public String toString() {

        return "Type: " + type
                + "\nID: " + id
                + "\nTitle: " + title
                + "\nAuthor(s): " + author
                + "\nMatched In: " + matchedIn
                + "\nScore: " + score;
    }
}