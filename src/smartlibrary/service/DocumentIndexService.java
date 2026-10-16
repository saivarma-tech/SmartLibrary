package smartlibrary.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import smartlibrary.algorithms.LCPArray;
import smartlibrary.algorithms.SuffixArray;
import smartlibrary.data.LibraryData;
import smartlibrary.model.Book;
import smartlibrary.model.ResearchPaper;

public class DocumentIndexService {

    private static final int MIN_SHARED_PHRASE_LENGTH = 6;

    private LibraryData libraryData;

    public DocumentIndexService(LibraryData libraryData) {
        this.libraryData = libraryData;
    }

    /** Returns related catalog entries for the item's member-facing detail view. */
    public List<RelatedDocumentResult> findRelatedTo(int id, int limit) {
        List<Document> documents = new ArrayList<>();
        for (Book book : libraryData.getBooks()) documents.add(new Document("BOOK", book.getId(), book.getTitle(), findDocumentTextById(book.getId())));
        for (ResearchPaper paper : libraryData.getResearchPapers()) documents.add(new Document("PAPER", paper.getId(), paper.getTitle(), findDocumentTextById(paper.getId())));
        Document target = documents.stream().filter(document -> document.id == id).findFirst().orElse(null);
        if (target == null) return List.of();
        List<RelatedDocumentResult> results = new ArrayList<>();
        for (Document other : documents) {
            if (other == target) continue;
            String combined = target.text + '\u0000' + other.text;
            int[] suffixes = SuffixArray.buildSuffixArray(combined);
            int[] lcp = LCPArray.buildLCPArray(combined, suffixes);
            String best = "";
            for (int rank = 1; rank < suffixes.length; rank++) {
                int first = suffixes[rank - 1], second = suffixes[rank];
                boolean cross = (first < target.text.length() && second > target.text.length())
                        || (second < target.text.length() && first > target.text.length());
                if (cross) {
                    String phrase = wholeWordPhrase(combined, Math.min(first, second), lcp[rank]);
                    if (phrase.length() > best.length()) best = phrase;
                }
            }
            if (best.length() >= MIN_SHARED_PHRASE_LENGTH) results.add(new RelatedDocumentResult(other, best));
        }
        results.sort((a, b) -> Integer.compare(b.sharedPhrase.length(), a.sharedPhrase.length()));
        return results.subList(0, Math.min(Math.max(limit, 0), results.size()));
    }

    public static final class RelatedDocumentResult {
        public final String type;
        public final int id;
        public final String title;
        public final String sharedPhrase;
        private RelatedDocumentResult(Document document, String phrase) {
            type = document.type; id = document.id; title = document.title; sharedPhrase = phrase;
        }
    }

    /**
     * Uses a suffix index internally to find books and papers that share a
     * meaningful phrase. The member sees related titles and the phrase, not
     * the internal index structure.
     */
    public void findRelatedDocuments(Scanner scanner) {
        List<Document> documents = new ArrayList<>();
        for (Book book : libraryData.getBooks()) {
            documents.add(new Document("Book", book.getId(), book.getTitle(),
                    findDocumentTextById(book.getId())));
        }
        for (ResearchPaper paper : libraryData.getResearchPapers()) {
            documents.add(new Document("Paper", paper.getId(), paper.getTitle(),
                    findDocumentTextById(paper.getId())));
        }

        System.out.println();
        System.out.println("Related books and papers");
        if (documents.size() < 2) {
            System.out.println("Add at least two documents to find related items.");
            return;
        }

        List<SharedPhrase> matches = new ArrayList<>();
        for (int first = 0; first < documents.size(); first++) {
            for (int second = first + 1; second < documents.size(); second++) {
                Document left = documents.get(first);
                Document right = documents.get(second);
                String combined = left.text + '\u0000' + right.text;
                int boundary = left.text.length();
                int[] suffixes = SuffixArray.buildSuffixArray(combined);
                int[] commonLengths = LCPArray.buildLCPArray(combined, suffixes);
                String bestPhrase = "";
                for (int rank = 1; rank < suffixes.length; rank++) {
                    int a = suffixes[rank - 1];
                    int b = suffixes[rank];
                    boolean crossesDocuments = (a < boundary && b > boundary)
                            || (b < boundary && a > boundary);
                    if (crossesDocuments) {
                        int start = a < boundary ? a : b;
                        String phrase = wholeWordPhrase(combined, start, commonLengths[rank]);
                        if (phrase.length() > bestPhrase.length()) {
                            bestPhrase = phrase;
                        }
                    }
                }
                if (bestPhrase.length() >= MIN_SHARED_PHRASE_LENGTH) {
                    matches.add(new SharedPhrase(left, right, bestPhrase));
                }
            }
        }

        matches.sort((a, b) -> Integer.compare(b.phrase.length(), a.phrase.length()));
        if (matches.isEmpty()) {
            System.out.println("No strong shared phrases were found yet.");
            return;
        }
        System.out.println("These items contain the same phrase in their details:");
        for (int i = 0; i < Math.min(5, matches.size()); i++) {
            SharedPhrase match = matches.get(i);
            System.out.println("- " + match.left.type + " " + match.left.id + ": "
                    + match.left.title + "  <->  " + match.right.type + " "
                    + match.right.id + ": " + match.right.title);
            System.out.println("  Shared phrase: \"" + match.phrase + "\"");
        }
    }

    private static final class Document {
        final String type;
        final int id;
        final String title;
        final String text;

        Document(String type, int id, String title, String text) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.text = text == null ? "" : text.toLowerCase();
        }
    }

    private static final class SharedPhrase {
        final Document left;
        final Document right;
        final String phrase;

        SharedPhrase(Document left, Document right, String phrase) {
            this.left = left;
            this.right = right;
            this.phrase = phrase;
        }
    }

    private String wholeWordPhrase(String text, int start, int length) {
        int end = Math.min(text.length(), start + length);
        if (start > 0 && start < end
                && Character.isLetterOrDigit(text.charAt(start - 1))
                && Character.isLetterOrDigit(text.charAt(start))) {
            while (start < end && Character.isLetterOrDigit(text.charAt(start))) {
                start++;
            }
        }
        while (start < end && !Character.isLetterOrDigit(text.charAt(start))) {
            start++;
        }
        if (end < text.length() && start < end
                && Character.isLetterOrDigit(text.charAt(end - 1))
                && Character.isLetterOrDigit(text.charAt(end))) {
            while (end > start && Character.isLetterOrDigit(text.charAt(end - 1))) {
                end--;
            }
        }
        while (end > start && !Character.isLetterOrDigit(text.charAt(end - 1))) {
            end--;
        }
        return end > start ? text.substring(start, end).trim() : "";
    }

    /** Compatibility entry point: show a useful library feature, not index internals. */
    public void displayDocumentIndex(Scanner scanner) {
        findRelatedDocuments(scanner);
    }

    /**
     * Finds document text using its ID.
     */
    private String findDocumentTextById(int id) {

        for (Book book : libraryData.getBooks()) {

            if (book.getId() == id) {

                StringBuilder document =
                        new StringBuilder();

                document.append(
                        book.getTitle()
                ).append(" ");

                document.append(
                        book.getAuthor()
                ).append(" ");

                document.append(
                        book.getCategory()
                ).append(" ");

                document.append(
                        book.getDescription()
                );

                return document.toString();
            }
        }

        for (ResearchPaper paper :
                libraryData.getResearchPapers()) {

            if (paper.getId() == id) {

                StringBuilder document =
                        new StringBuilder();

                document.append(
                        paper.getTitle()
                ).append(" ");

                document.append(
                        paper.getAuthors()
                ).append(" ");

                document.append(
                        paper.getCategory()
                ).append(" ");

                document.append(
                        paper.getAbstractText()
                );

                return document.toString();
            }
        }

        return null;
    }
}
