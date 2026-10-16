package smartlibrary.service;

import java.util.List;

import smartlibrary.model.SearchResult;

public class SearchStatistics {

    private int totalResults;

    private int bookResults;

    private int researchPaperResults;

    private int titleMatches;

    private int authorMatches;

    private int categoryMatches;

    private int abstractMatches;

    private int descriptionMatches;

    private int highestScore;

    private int lowestScore;

    // ==========================================
    // DEFAULT CONSTRUCTOR
    // ==========================================

    public SearchStatistics() {

        resetStatistics();
    }

    // ==========================================
    // CONSTRUCTOR WITH RESULTS
    // ==========================================

    public SearchStatistics(List<SearchResult> results) {

        resetStatistics();

        calculateStatistics(results);
    }

    // ==========================================
    // RESET STATISTICS
    // ==========================================

    private void resetStatistics() {

        totalResults = 0;

        bookResults = 0;

        researchPaperResults = 0;

        titleMatches = 0;

        authorMatches = 0;

        categoryMatches = 0;

        abstractMatches = 0;

        descriptionMatches = 0;

        highestScore = 0;

        lowestScore = 0;
    }

    // ==========================================
    // CALCULATE STATISTICS
    // ==========================================

    private void calculateStatistics(
            List<SearchResult> results) {

        resetStatistics();

        if (results == null || results.isEmpty()) {

            return;
        }

        totalResults = results.size();

        highestScore = results.get(0).getScore();

        lowestScore = results.get(0).getScore();

        for (SearchResult result : results) {

            if (result == null) {
                continue;
            }

            // ==================================
            // SCORE STATISTICS
            // ==================================

            int score = result.getScore();

            if (score > highestScore) {

                highestScore = score;
            }

            if (score < lowestScore) {

                lowestScore = score;
            }

            // ==================================
            // RESULT TYPE
            // ==================================

            String type = result.getType();

            if (type != null) {

                if (type.equalsIgnoreCase("BOOK")) {

                    bookResults++;

                } else if (
                        type.equalsIgnoreCase("RESEARCH PAPER")) {

                    researchPaperResults++;
                }
            }

            // ==================================
            // MATCHED FIELD
            // ==================================

            String matchedIn = result.getMatchedIn();

            if (matchedIn == null) {
                continue;
            }

            if (matchedIn.equalsIgnoreCase("TITLE")) {

                titleMatches++;

            } else if (
                    matchedIn.equalsIgnoreCase("AUTHOR") ||
                    matchedIn.equalsIgnoreCase("AUTHORS")) {

                authorMatches++;

            } else if (
                    matchedIn.equalsIgnoreCase("CATEGORY")) {

                categoryMatches++;

            } else if (
                    matchedIn.equalsIgnoreCase("ABSTRACT")) {

                abstractMatches++;

            } else if (
                    matchedIn.equalsIgnoreCase("DESCRIPTION")) {

                descriptionMatches++;
            }
        }
    }

    // ==========================================
    // UPDATE STATISTICS
    // ==========================================

    public void updateStatistics(List<SearchResult> results) {

        calculateStatistics(results);
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public int getTotalResults() {

        return totalResults;
    }

    public int getBookResults() {

        return bookResults;
    }

    public int getResearchPaperResults() {

        return researchPaperResults;
    }

    public int getTitleMatches() {

        return titleMatches;
    }

    public int getAuthorMatches() {

        return authorMatches;
    }

    public int getCategoryMatches() {

        return categoryMatches;
    }

    public int getAbstractMatches() {

        return abstractMatches;
    }

    public int getDescriptionMatches() {

        return descriptionMatches;
    }

    public int getHighestScore() {

        return highestScore;
    }

    public int getLowestScore() {

        return lowestScore;
    }

    // ==========================================
    // DISPLAY STATISTICS
    // ==========================================

    public void display() {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "       SEARCH STATISTICS"
        );

        System.out.println(
                "================================="
        );

        System.out.println();

        System.out.println(
                "Total Results: " +
                totalResults
        );

        System.out.println(
                "Books Found: " +
                bookResults
        );

        System.out.println(
                "Research Papers Found: " +
                researchPaperResults
        );

        System.out.println(
                "Highest Score: " +
                highestScore
        );

        System.out.println(
                "Lowest Score: " +
                lowestScore
        );

        System.out.println();

        System.out.println(
                "Title Matches: " +
                titleMatches
        );

        System.out.println(
                "Author Matches: " +
                authorMatches
        );

        System.out.println(
                "Category Matches: " +
                categoryMatches
        );

        System.out.println(
                "Abstract Matches: " +
                abstractMatches
        );

        System.out.println(
                "Description Matches: " +
                descriptionMatches
        );

        System.out.println();

        System.out.println(
                "================================="
        );
    }

    // ==========================================
    // ALTERNATIVE DISPLAY METHOD
    // ==========================================

    public void displayStatistics() {

        display();
    }
}