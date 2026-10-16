package smartlibrary.service;

import java.util.Scanner;

import smartlibrary.algorithms.SuffixAutomation;
import smartlibrary.data.LibraryData;
import smartlibrary.model.Book;
import smartlibrary.model.ResearchPaper;

public class SubstringSearchService {

    private LibraryData libraryData;

    public SubstringSearchService(
            LibraryData libraryData) {

        this.libraryData = libraryData;
    }

    /**
     * Searches a phrase inside a selected document.
     */
    public void searchSubstring(Scanner scanner) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "       FIND A PHRASE IN A DOCUMENT  "
        );

        System.out.println(
                "======================================"
        );

        System.out.print(
                "Enter the book or paper ID: "
        );

        int id;

        try {

            id = Integer.parseInt(
                    scanner.nextLine().trim()
            );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid document ID."
            );

            return;
        }

        String documentText =
                findDocumentTextById(id);

        if (documentText == null) {

            System.out.println(
                    "No document found with ID: " + id
            );

            return;
        }

        System.out.print(
                "Enter the phrase you want to find: "
        );

        String pattern =
                scanner.nextLine();

        if (pattern.trim().isEmpty()) {

            System.out.println(
                    "Search phrase cannot be empty."
            );

            return;
        }

        String normalizedText =
                documentText.toLowerCase();

        String normalizedPattern =
                pattern.toLowerCase();

        System.out.println();
        System.out.println(
                "Searching the document..."
        );

        SuffixAutomation automaton =
                new SuffixAutomation(
                        normalizedText
                );

        boolean found =
                automaton.contains(
                        normalizedPattern
                );

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "          SEARCH RESULT               "
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Document ID       : " + id
        );

        System.out.println(
                "Search Phrase     : " + pattern
        );

        if (found) {

            System.out.println(
                    "Result            : This phrase appears in the document."
            );

            System.out.println(
                    "Phrase length     : "
                            + normalizedPattern.length()
            );

        } else {

            System.out.println(
                    "Result            : This phrase is not in the document."
            );

        }

        System.out.println(
                "======================================"
        );
    }

    /**
     * Combines document fields into searchable text.
     */
    private String findDocumentTextById(int id) {

        for (Book book : libraryData.getBooks()) {

            if (book.getId() == id) {

                return book.getTitle()
                        + " "
                        + book.getAuthor()
                        + " "
                        + book.getCategory()
                        + " "
                        + book.getDescription();
            }
        }

        for (ResearchPaper paper :
                libraryData.getResearchPapers()) {

            if (paper.getId() == id) {

                return paper.getTitle()
                        + " "
                        + paper.getAuthors()
                        + " "
                        + paper.getCategory()
                        + " "
                        + paper.getAbstractText();
            }
        }

        return null;
    }
}
