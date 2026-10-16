package smartlibrary.service;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import smartlibrary.algorithms.KMP;
import smartlibrary.algorithms.ZAlgorithm;
import smartlibrary.algorithms.RabinKarp;
import smartlibrary.algorithms.AhoCorasick;

import smartlibrary.data.LibraryData;

import smartlibrary.model.Book;
import smartlibrary.model.ResearchPaper;
import smartlibrary.model.SearchResult;

public class SearchService {

    private LibraryData library;

    public SearchService(LibraryData library) {
        this.library = library;
    }

    // ==========================================
    // MAIN SEARCH METHOD
    // ==========================================

    public List<SearchResult> search(String query) {

        List<SearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        query = query.trim();

        // ==========================================
        // Search every useful book field so members can find an item by
        // title, author, subject, or description.
        // ==========================================

        for (Book book : library.getBooks()) {

            String matchedIn = firstMatchingField(query,
                    new String[][] {
                            {"TITLE", book.getTitle()},
                            {"AUTHOR", book.getAuthor()},
                            {"CATEGORY", book.getCategory()},
                            {"DESCRIPTION", book.getDescription()}
                    });
            if (matchedIn != null) {

                SearchResult result = new SearchResult(
                        "BOOK",
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        matchedIn
                );

                result.setScore(
                        calculateSmartScore(
                                book.getTitle(),
                                query
                        ) + ("TITLE".equals(matchedIn) ? 0 : 20)
                );

                results.add(result);
            }
        }

        // ==========================================
        // Search the paper title, authors, subject, and abstract as well.
        // ==========================================

        for (ResearchPaper paper :
                library.getResearchPapers()) {

            String matchedIn = firstMatchingField(query,
                    new String[][] {
                            {"TITLE", paper.getTitle()},
                            {"AUTHOR", paper.getAuthors()},
                            {"CATEGORY", paper.getCategory()},
                            {"ABSTRACT", paper.getAbstractText()}
                    });
            if (matchedIn != null) {

                SearchResult result = new SearchResult(
                        "RESEARCH PAPER",
                        paper.getId(),
                        paper.getTitle(),
                        paper.getAuthors(),
                        matchedIn
                );

                result.setScore(
                        calculateSmartScore(
                                paper.getTitle(),
                                query
                        ) + ("TITLE".equals(matchedIn) ? 0 : 20)
                );

                results.add(result);
            }
        }

        // ==========================================
        // PRIORITY QUEUE FOR RANKING
        // ==========================================

        PriorityQueue<SearchResult> priorityQueue =
                new PriorityQueue<>(
                        (a, b) -> Integer.compare(
                                b.getScore(),
                                a.getScore()
                        )
                );

        priorityQueue.addAll(results);

        // ==========================================
        // CREATE RANKED RESULTS
        // ==========================================

        List<SearchResult> rankedResults =
                new ArrayList<>();

        while (!priorityQueue.isEmpty()) {
            rankedResults.add(priorityQueue.poll());
        }

        return rankedResults;
    }

    // ==========================================
    // SMART TITLE SCORE
    // ==========================================

    private int calculateSmartScore(
            String text,
            String query) {

        if (text == null || query == null) {
            return 0;
        }

        String textLower =
                text.trim().toLowerCase();

        String queryLower =
                query.trim().toLowerCase();

        if (textLower.equals(queryLower)) {
            return 120;
        }

        if (textLower.contains(queryLower)) {
            return 100;
        }

        return 0;
    }

    private String firstMatchingField(String query, String[][] fields) {
        for (String[] field : fields) {
            if (searchUsingAlgorithms(field[1], query)) {
                return field[0];
            }
        }
        return null;
    }

    // ==========================================
    // SEARCH USING DSA ALGORITHMS
    // ==========================================

    private boolean searchUsingAlgorithms(
            String text,
            String pattern) {

        if (text == null ||
                pattern == null ||
                pattern.trim().isEmpty()) {

            return false;
        }

        String lowerText =
                text.toLowerCase();

        String lowerPattern =
                pattern.toLowerCase();

        // KMP
        boolean kmpResult =
                KMP.search(
                        lowerText,
                        lowerPattern
                );

        // Z Algorithm
        boolean zResult =
                ZAlgorithm.search(
                        lowerText,
                        lowerPattern
                );

        // Rabin-Karp
        boolean rabinResult =
                RabinKarp.search(
                        lowerText,
                        lowerPattern
                );

        // Aho-Corasick
        AhoCorasick aho =
                new AhoCorasick();

        aho.addPattern(lowerPattern);
        aho.buildFailureLinks();

        boolean ahoResult =
                aho.search(lowerText);

        return kmpResult ||
                zResult ||
                rabinResult ||
                ahoResult;
    }
}
