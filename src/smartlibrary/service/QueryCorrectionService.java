package smartlibrary.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import smartlibrary.algorithms.LevenshteinDistance;
import smartlibrary.data.LibraryData;
import smartlibrary.model.Book;
import smartlibrary.model.ResearchPaper;

public class QueryCorrectionService {

    private LibraryData libraryData;

    public QueryCorrectionService(
            LibraryData libraryData) {

        this.libraryData = libraryData;
    }

    /** Returns a short spelling suggestion for use directly in library search. */
    public String findSuggestion(String query) {
        if (query == null || query.trim().length() < 3) {
            return "";
        }
        String normalizedQuery = query.trim().toLowerCase();
        String bestMatch = "";
        int bestDistance = Integer.MAX_VALUE;
        for (String word : collectLibraryWords()) {
            int distance = LevenshteinDistance.calculate(
                    normalizedQuery, word.toLowerCase());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMatch = word;
            }
        }
        return bestDistance <= 2 ? bestMatch : "";
    }

    /**
     * Finds the closest matching book title,
     * research paper title, author, or keyword.
     */
    public void suggestCorrection(Scanner scanner) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "       SEARCH QUERY CORRECTION        "
        );

        System.out.println(
                "======================================"
        );

        System.out.print(
                "Enter incorrect search query: "
        );

        String query =
                scanner.nextLine().trim();

        if (query.isEmpty()) {

            System.out.println(
                    "Search query cannot be empty."
            );

            return;
        }

        String normalizedQuery =
                query.toLowerCase();

        List<String> dictionary =
                collectLibraryWords();

        String bestMatch = "";
        int bestDistance =
                Integer.MAX_VALUE;

        for (String word : dictionary) {

            String normalizedWord =
                    word.toLowerCase();

            int distance =
                    LevenshteinDistance.calculate(
                            normalizedQuery,
                            normalizedWord
                    );

            if (distance < bestDistance) {

                bestDistance = distance;
                bestMatch = word;
            }
        }

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "          CORRECTION RESULT            "
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Entered Query : " + query
        );

        if (bestMatch.isEmpty()) {

            System.out.println(
                    "No correction suggestion available."
            );

        } else {

            System.out.println(
                    "Suggested Word: " + bestMatch
            );

            System.out.println(
                    "Edit Distance : " + bestDistance
            );

            if (bestDistance == 0) {

                System.out.println(
                        "Status        : Correct spelling"
                );

            } else if (bestDistance <= 2) {

                System.out.println(
                        "Status        : Close spelling match"
                );

            } else {

                System.out.println(
                        "Status        : Weak spelling match"
                );
            }
        }

        System.out.println(
                "======================================"
        );
    }

    /**
     * Displays the DP matrix for two words.
     */
    public void displayDistanceMatrix(
            Scanner scanner) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "       LEVENSHTEIN DISTANCE MATRIX     "
        );

        System.out.println(
                "======================================"
        );

        System.out.print(
                "Enter first word: "
        );

        String first =
                scanner.nextLine();

        System.out.print(
                "Enter second word: "
        );

        String second =
                scanner.nextLine();

        int distance =
                LevenshteinDistance.calculate(
                        first,
                        second
                );

        System.out.println();

        System.out.println(
                "First Word  : " + first
        );

        System.out.println(
                "Second Word : " + second
        );

        System.out.println(
                "Edit Distance: " + distance
        );

        LevenshteinDistance.displayMatrix(
                first,
                second
        );
    }

    /**
     * Collects searchable words from library documents.
     */
    private List<String> collectLibraryWords() {

        List<String> words =
                new ArrayList<>();

        for (Book book : libraryData.getBooks()) {

            addWords(
                    words,
                    book.getTitle()
            );

            addWords(
                    words,
                    book.getAuthor()
            );

            addWords(
                    words,
                    book.getCategory()
            );

            addWords(
                    words,
                    book.getDescription()
            );
        }

        for (
                ResearchPaper paper :
                libraryData.getResearchPapers()
        ) {

            addWords(
                    words,
                    paper.getTitle()
            );

            addWords(
                    words,
                    paper.getAuthors()
            );

            addWords(
                    words,
                    paper.getCategory()
            );

            addWords(
                    words,
                    paper.getAbstractText()
            );
        }

        return words;
    }

    /**
     * Adds individual words into the dictionary.
     */
    private void addWords(
            List<String> words,
            String text) {

        if (text == null) {
            return;
        }

        String[] splitWords =
                text.split("[^a-zA-Z0-9]+");

        for (String word : splitWords) {

            if (!word.trim().isEmpty()) {

                boolean alreadyExists = false;

                for (String existing : words) {

                    if (
                            existing.equalsIgnoreCase(word)
                    ) {

                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {
                    words.add(word);
                }
            }
        }
    }
}
