package smartlibrary;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import smartlibrary.algorithms.ComplexityAlgorithms;
import smartlibrary.algorithms.NetworkFlowAlgorithms;
import smartlibrary.algorithms.RandomizedParallelAlgorithms;
import smartlibrary.data.LibraryData;
import smartlibrary.model.Book;
import smartlibrary.model.BorrowRecord;
import smartlibrary.model.ResearchPaper;
import smartlibrary.model.SearchResult;
import smartlibrary.model.SearchUsage;
import smartlibrary.service.LibraryService;
import smartlibrary.service.QueryCorrectionService;
import smartlibrary.service.SearchService;

/** A task-oriented console for members and library staff. */
public class Main {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy")
            .withZone(ZoneId.systemDefault());

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            LibraryData data = new LibraryData();
            SearchService search = new SearchService(data);
            QueryCorrectionService corrections = new QueryCorrectionService(data);
            LibraryService library = new LibraryService(data);

            System.out.println("==============================================");
            System.out.println("             WELCOME TO SMARTLIBRARY");
            System.out.println(" Find books, manage your loans, and get help.");
            System.out.println("==============================================");
            String memberName = readRequired(scanner, "Enter your name to manage your library loans: ");

            boolean running = true;
            while (running) {
                showMenu(memberName);
                int choice = readInt(scanner, "Choose an option: ");
                switch (choice) {
                    case 1 -> findItems(scanner, search, corrections, data);
                    case 2 -> showBooks(data);
                    case 3 -> browseByCategory(scanner, data);
                    case 4 -> findByAuthor(scanner, data);
                    case 5 -> checkAvailability(scanner, data);
                    case 6 -> borrowBook(scanner, data, memberName);
                    case 7 -> returnBook(scanner, data, memberName);
                    case 8 -> renewBook(scanner, data, memberName);
                    case 9 -> showMemberLoans(data, memberName, false);
                    case 10 -> showMemberLoans(data, memberName, true);
                    case 11 -> showPapers(data.getResearchPapers());
                    case 12 -> showLibraryInformation(data);
                    case 13 -> librarianMenu(scanner, library, data);
                    case 14 -> reviewerPlanning(data);
                    case 15 -> documentConflictPlanning(data);
                    case 16 -> usageAnalytics(scanner, data);
                    case 0 -> running = false;
                    default -> System.out.println("Please choose one of the options shown in the menu.");
                }
                if (running) pause(scanner);
            }
            System.out.println("Thank you for visiting SmartLibrary, " + memberName + ". Have a lovely day!");
        }
    }

    private static void showMenu(String memberName) {
        System.out.println();
        System.out.println("--------------- SMARTLIBRARY ---------------");
        System.out.println("Hello, " + memberName + "!");
        System.out.println(" 1. Find a book or publication");
        System.out.println(" 2. Browse all books");
        System.out.println(" 3. Browse books by category");
        System.out.println(" 4. Find books by author");
        System.out.println(" 5. Check book availability");
        System.out.println(" 6. Borrow a book");
        System.out.println(" 7. Return a borrowed book");
        System.out.println(" 8. Renew a borrowed book");
        System.out.println(" 9. View my borrowed books");
        System.out.println("10. View my borrowing history");
        System.out.println("11. Browse research publications");
        System.out.println("12. Library information");
        System.out.println("13. Librarian tools");
        System.out.println("14. Module 4 — Assign research papers to reviewers");
        System.out.println("15. Module 5 — Plan around related-document conflicts");
        System.out.println("16. Module 6 — Review library usage and processing");
        System.out.println(" 0. Exit");
        System.out.println("---------------------------------------------");
    }

    private static void findItems(Scanner scanner, SearchService search, QueryCorrectionService corrections, LibraryData data) {
        String query = readRequired(scanner, "What would you like to find? ");
        data.addSearchUsage(new SearchUsage(query, System.currentTimeMillis()));
        List<SearchResult> results = search.search(query);
        if (results.isEmpty()) {
            String suggestion = corrections.findSuggestion(query);
            if (!suggestion.isBlank() && !suggestion.equalsIgnoreCase(query)) {
                System.out.println("No exact matches. Did you mean \"" + suggestion + "\"?");
                if (readLine(scanner, "Search for that instead? (y/n): ").equalsIgnoreCase("y")) {
                    query = suggestion;
                    results = search.search(query);
                }
            }
        }
        if (results.isEmpty()) {
            System.out.println("No matching books or publications found. Try another title, author, or subject.");
            return;
        }
        System.out.println("\nMatches for \"" + query + "\":");
        for (SearchResult result : results) {
            System.out.println("- " + result.getTitle() + " — " + result.getAuthor()
                    + " (" + displayType(result.getType()) + ", item " + result.getId() + ")");
            if ("BOOK".equalsIgnoreCase(result.getType())) {
                Book book = data.getBookById(result.getId());
                if (book != null) System.out.println("  " + availableCopies(book, data) + " of "
                        + book.getTotalCopies() + " copies available · " + book.getLocation());
            }
        }
    }

    private static String displayType(String type) {
        return type == null ? "Library item" : type.toLowerCase().contains("paper") ? "research publication" : "book";
    }

    private static void showBooks(LibraryData data) {
        List<Book> books = data.getBooks();
        if (books.isEmpty()) { System.out.println("There are no books in the catalogue yet."); return; }
        System.out.println("\nBOOKS IN THE LIBRARY");
        for (Book book : books) printBook(book, data);
    }

    private static void browseByCategory(Scanner scanner, LibraryData data) {
        String category = readRequired(scanner, "Enter a category or subject: ");
        List<Book> matches = data.getBooks().stream().filter(b -> contains(b.getCategory(), category)).toList();
        if (matches.isEmpty()) System.out.println("No books were found in that category.");
        else { System.out.println("\nBooks in " + category + ":"); matches.forEach(book -> printBook(book, data)); }
    }

    private static void findByAuthor(Scanner scanner, LibraryData data) {
        String author = readRequired(scanner, "Enter an author's name: ");
        List<Book> matches = data.getBooks().stream().filter(b -> contains(b.getAuthor(), author)).toList();
        if (matches.isEmpty()) System.out.println("No books were found for that author.");
        else { System.out.println("\nBooks by " + author + ":"); matches.forEach(book -> printBook(book, data)); }
    }

    private static void checkAvailability(Scanner scanner, LibraryData data) {
        int id = readInt(scanner, "Enter the book number shown in the catalogue: ");
        Book book = data.getBookById(id);
        if (book == null) { System.out.println("We couldn't find a book with that number."); return; }
        int available = availableCopies(book, data);
        System.out.println("\n" + book.getTitle() + " — " + book.getAuthor());
        System.out.println(available > 0 ? "Available now: " + available + " of " + book.getTotalCopies() + " copies"
                : "Currently checked out. Please ask a librarian about reserving it.");
        System.out.println("Library location: " + book.getLocation());
    }

    private static void borrowBook(Scanner scanner, LibraryData data, String memberName) {
        int id = readInt(scanner, "Enter the book number you would like to borrow: ");
        Book book = data.getBookById(id);
        if (book == null) { System.out.println("We couldn't find a book with that number."); return; }
        if (availableCopies(book, data) <= 0) {
            System.out.println("All copies are currently checked out. Please ask a librarian about a reservation.");
            return;
        }
        long now = System.currentTimeMillis();
        BorrowRecord loan = new BorrowRecord(data.nextLoanId(), id, memberName, now,
                now + 14L * 24 * 60 * 60 * 1000, false, 0);
        data.addBorrowRecord(loan);
        System.out.println("You borrowed \"" + book.getTitle() + "\". Please return it by " + DATE.format(Instant.ofEpochMilli(loan.getDueAt())) + ".");
    }

    private static void returnBook(Scanner scanner, LibraryData data, String memberName) {
        List<BorrowRecord> loans = memberLoans(data, memberName, false);
        if (loans.isEmpty()) { System.out.println("You have no books to return."); return; }
        printLoans(data, loans);
        int loanId = readInt(scanner, "Enter the loan number to return: ");
        BorrowRecord loan = loans.stream().filter(r -> r.getLoanId() == loanId).findFirst().orElse(null);
        if (loan == null) { System.out.println("That loan number isn't in your active loans."); return; }
        loan.markReturned();
        data.save();
        Book book = data.getBookById(loan.getBookId());
        System.out.println("Thank you. " + (book == null ? "The book" : "\"" + book.getTitle() + "\"") + " has been returned.");
    }

    private static void renewBook(Scanner scanner, LibraryData data, String memberName) {
        List<BorrowRecord> loans = memberLoans(data, memberName, false);
        if (loans.isEmpty()) { System.out.println("You have no active loans to renew."); return; }
        printLoans(data, loans);
        int loanId = readInt(scanner, "Enter the loan number to renew: ");
        BorrowRecord loan = loans.stream().filter(r -> r.getLoanId() == loanId).findFirst().orElse(null);
        if (loan == null) { System.out.println("That loan number isn't in your active loans."); return; }
        if (loan.renew()) {
            data.save();
            System.out.println("Renewal complete. Your new due date is " + DATE.format(Instant.ofEpochMilli(loan.getDueAt())) + ".");
        } else {
            System.out.println("This loan can't be renewed. It may be overdue or already renewed twice. Please ask a librarian for help.");
        }
    }

    private static void showMemberLoans(LibraryData data, String memberName, boolean history) {
        List<BorrowRecord> loans = memberLoans(data, memberName, history);
        if (loans.isEmpty()) {
            System.out.println(history ? "You don't have any borrowing history yet." : "You don't have any books checked out right now.");
            return;
        }
        System.out.println(history ? "\nYOUR BORROWING HISTORY" : "\nYOUR CURRENT LOANS");
        printLoans(data, loans);
    }

    private static List<BorrowRecord> memberLoans(LibraryData data, String memberName, boolean includeReturned) {
        return data.getBorrowRecords().stream()
                .filter(r -> r.getBorrowerName().equalsIgnoreCase(memberName))
                .filter(r -> includeReturned || !r.isReturned()).toList();
    }

    private static void printLoans(LibraryData data, List<BorrowRecord> loans) {
        for (BorrowRecord loan : loans) {
            Book book = data.getBookById(loan.getBookId());
            String title = book == null ? "Catalogue item " + loan.getBookId() : book.getTitle();
            String state = loan.isReturned() ? "Returned" : loan.isOverdue() ? "Overdue" : "Checked out";
            System.out.println("- Loan " + loan.getLoanId() + ": " + title + " · " + state
                    + " · due " + DATE.format(Instant.ofEpochMilli(loan.getDueAt())));
        }
    }

    private static void showPapers(List<ResearchPaper> papers) {
        if (papers.isEmpty()) { System.out.println("There are no research publications in the catalogue yet."); return; }
        System.out.println("\nRESEARCH PUBLICATIONS");
        for (ResearchPaper paper : papers) {
            System.out.println("- " + paper.getTitle() + " — " + paper.getAuthors()
                    + " · " + paper.getCategory() + " (item " + paper.getId() + ")");
        }
    }

    private static void showLibraryInformation(LibraryData data) {
        int copies = data.getBooks().stream().mapToInt(Book::getTotalCopies).sum();
        long available = data.getBooks().stream().mapToInt(b -> availableCopies(b, data)).sum();
        long activeLoans = data.getBorrowRecords().stream().filter(r -> !r.isReturned()).count();
        System.out.println("\nLIBRARY AT A GLANCE");
        System.out.println("Book titles: " + data.getBooks().size());
        System.out.println("Books on the shelves: " + available + " of " + copies + " copies");
        System.out.println("Research publications: " + data.getResearchPapers().size());
        System.out.println("Active loans: " + activeLoans);
    }

    /** Module 4: connect reviewer-paper assignment and capacity flow to staff work. */
    private static void reviewerPlanning(LibraryData data) {
        List<ResearchPaper> papers = data.getResearchPapers();
        if (papers.isEmpty()) {
            System.out.println("Add research publications before planning reviewer assignments.");
            return;
        }
        int reviewerCount = Math.min(3, papers.size());
        int[][] eligible = new int[reviewerCount * 2][];
        int edge = 0;
        for (int reviewer = 0; reviewer < reviewerCount; reviewer++) {
            eligible[edge++] = new int[] {reviewer, reviewer};
            eligible[edge++] = new int[] {reviewer, (reviewer + 1) % papers.size()};
        }
        eligible = Arrays.copyOf(eligible, edge);
        NetworkFlowAlgorithms.Matching matching = NetworkFlowAlgorithms.match(
                reviewerCount, papers.size(), eligible);
        System.out.println("\nREVIEWER ASSIGNMENT PLAN (sample eligibility scenario)");
        if (matching.reviewerToPaper.isEmpty()) {
            System.out.println("No eligible reviewer-paper assignments were found.");
        } else {
            matching.reviewerToPaper.forEach((reviewer, paper) ->
                    System.out.println("Reviewer " + (reviewer + 1) + " → ["
                            + papers.get(paper).getId() + "] " + papers.get(paper).getTitle()));
        }
        System.out.println("Assignments made: " + matching.size() + " of " + papers.size() + " papers.");
        System.out.println("Minimum conflict cover: reviewers " + matching.reviewerCover
                + ", papers " + matching.paperCover + " (zero-based sample indexes).");

        List<NetworkFlowAlgorithms.Edge> resourceNetwork = List.of(
                new NetworkFlowAlgorithms.Edge(0, 1, 4),
                new NetworkFlowAlgorithms.Edge(0, 2, 3),
                new NetworkFlowAlgorithms.Edge(1, 3, 2),
                new NetworkFlowAlgorithms.Edge(1, 4, 2),
                new NetworkFlowAlgorithms.Edge(2, 3, 2),
                new NetworkFlowAlgorithms.Edge(2, 4, 2),
                new NetworkFlowAlgorithms.Edge(3, 5, 3),
                new NetworkFlowAlgorithms.Edge(4, 5, 3));
        int maxResources = NetworkFlowAlgorithms.edmondsKarp(6, resourceNetwork, 0, 5);
        int largerNetworkPlan = NetworkFlowAlgorithms.dinic(6, resourceNetwork, 0, 5);
        System.out.println("Resource planning example: up to " + maxResources
                + " resource units can be routed from the library to its service points.");
        System.out.println("The faster large-network planning method confirms a capacity of "
                + largerNetworkPlan + " units.");
        System.out.println("The reviewer and resource examples use illustrative capacities; the current project has no saved reviewer roster or branch-capacity records.");
    }

    /** Module 5: find related records to review together and show the constraint reduction. */
    private static void documentConflictPlanning(LibraryData data) {
        List<ResearchPaper> papers = data.getResearchPapers();
        if (papers.size() < 2) {
            System.out.println("Add at least two research publications to plan document conflicts.");
            return;
        }
        List<int[]> conflictPairs = new ArrayList<>();
        for (int first = 0; first < papers.size(); first++) {
            for (int second = first + 1; second < papers.size(); second++) {
                if (contains(papers.get(first).getCategory(), papers.get(second).getCategory())
                        || contains(papers.get(second).getCategory(), papers.get(first).getCategory())) {
                    conflictPairs.add(new int[] {first, second});
                }
            }
        }
        System.out.println("\nRELATED-DOCUMENT REVIEW PLAN");
        if (conflictPairs.isEmpty()) {
            System.out.println("No publications share a category, so there are no category conflicts to cover.");
        } else {
            for (int[] pair : conflictPairs) {
                System.out.println("Related: [" + papers.get(pair[0]).getId() + "] "
                        + papers.get(pair[0]).getTitle() + " ↔ [" + papers.get(pair[1]).getId()
                        + "] " + papers.get(pair[1]).getTitle());
            }
            var cover = ComplexityAlgorithms.vertexCoverTwoApprox(papers.size(),
                    conflictPairs.toArray(int[][]::new));
            System.out.println("Suggested set to include in the review: " + cover.stream()
                    .map(index -> "[" + papers.get(index).getId() + "] " + papers.get(index).getTitle())
                    .toList());
            System.out.println("This approximate plan selects document records so every detected category-conflict pair is represented.");
        }

        int[][] sampleConstraints = {{1, 2, 3}, {-1, 2, 3}, {1, -2, 3}};
        boolean feasible = ComplexityAlgorithms.isSatisfiable(3, sampleConstraints);
        ComplexityAlgorithms.CliqueInstance reduction =
                ComplexityAlgorithms.threeSatToClique(3, sampleConstraints);
        int[][] independentSet = ComplexityAlgorithms.cliqueToIndependentSet(
                reduction.vertices, reduction.edges());
        ComplexityAlgorithms.VertexCoverInstance coverReduction =
                ComplexityAlgorithms.cliqueToVertexCover(reduction.vertices,
                        reduction.edges(), reduction.required);
        System.out.println("\nRESOURCE-CONSTRAINT EXAMPLE");
        System.out.println("A sample set of three yes/no library constraints is "
                + (feasible ? "feasible." : "not feasible."));
        System.out.println("Its planning conversion contains " + reduction.vertices
                + " choices; the equivalent independent-set graph has " + independentSet.length
                + " links, and the corresponding cover target is "
                + coverReduction.requiredCoverSize + " records.");
        System.out.println("These are a small demonstration instance; actual project records use the category-conflict plan above.");
    }

    /** Module 6: apply randomized ranking and parallel summaries to saved activity. */
    private static void usageAnalytics(Scanner scanner, LibraryData data) {
        boolean back = false;
        while (!back) {
            System.out.println("\nLIBRARY ACTIVITY AND PROCESSING");
            System.out.println("1. Rank books by title");
            System.out.println("2. Summarize and sample saved searches");
            System.out.println("3. Check a large numeric processing value");
            System.out.println("4. Estimate parallel processing time");
            System.out.println("0. Return to SmartLibrary menu");
            int choice = readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 1 -> {
                    Book[] ranked = data.getBooks().toArray(Book[]::new);
                    RandomizedParallelAlgorithms.randomizedQuickSort(ranked,
                            Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER), new Random());
                    System.out.println("\nBOOKS BY TITLE");
                    if (ranked.length == 0) System.out.println("No books are in the catalogue yet.");
                    for (Book book : ranked) System.out.println("- [" + book.getId() + "] " + book.getTitle());
                }
                case 2 -> showSearchUsageSummary(data);
                case 3 -> {
                    long value = readLong(scanner, "Enter the numeric batch value to check: ");
                    System.out.println(value + (RandomizedParallelAlgorithms.isPrime(value)
                            ? " is a prime value." : " is not a prime value."));
                }
                case 4 -> {
                    long work = readPositiveLong(scanner, "Estimated work units: ");
                    long depth = readPositiveLong(scanner, "Longest dependent-work units: ");
                    int processors = readPositiveInt(scanner, "Available processors: ");
                    double estimate = RandomizedParallelAlgorithms.brentBound(work, depth, processors);
                    System.out.printf("Estimated processing bound: %.2f time units.%n", estimate);
                }
                case 0 -> back = true;
                default -> System.out.println("Please choose one of the options shown.");
            }
        }
    }

    private static void showSearchUsageSummary(LibraryData data) {
        List<String> queries = data.getSearchUsage().stream().map(usage -> usage.getQuery()).toList();
        if (queries.isEmpty()) {
            System.out.println("There are no saved search events yet. Search for books first, then return here.");
            return;
        }
        List<String> sample = RandomizedParallelAlgorithms.reservoirSample(
                queries, Math.min(5, queries.size()), new Random());
        Map<LocalDate, Long> dailyCounts = new TreeMap<>();
        for (var usage : data.getSearchUsage()) {
            LocalDate day = Instant.ofEpochMilli(usage.getSearchedAt())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            dailyCounts.merge(day, 1L, Long::sum);
        }
        long[] counts = dailyCounts.values().stream().mapToLong(Long::longValue).toArray();
        long total = RandomizedParallelAlgorithms.parallelReduce(counts);
        long[] beforeDay = RandomizedParallelAlgorithms.blellochExclusiveScan(counts);
        System.out.println("\nSAVED SEARCH ACTIVITY");
        System.out.println("Total saved searches: " + total);
        System.out.println("Random sample of searches: " + sample);
        int index = 0;
        for (Map.Entry<LocalDate, Long> entry : dailyCounts.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue()
                    + " searches (" + beforeDay[index++] + " occurred on earlier days)");
        }
    }

    private static void librarianMenu(Scanner scanner, LibraryService library, LibraryData data) {
        boolean back = false;
        while (!back) {
            System.out.println("\nLIBRARIAN TOOLS");
            System.out.println("1. Add a book to the catalogue");
            System.out.println("2. Add a research publication");
            System.out.println("3. Remove a catalogue item");
            System.out.println("4. View library summary");
            System.out.println("0. Return to member menu");
            int choice = readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 1 -> library.addBook(scanner);
                case 2 -> library.addResearchPaper(scanner);
                case 3 -> library.removeItemById(scanner);
                case 4 -> showLibraryInformation(data);
                case 0 -> back = true;
                default -> System.out.println("Please choose one of the librarian options shown.");
            }
            if (!back) pause(scanner);
        }
    }

    private static void printBook(Book book, LibraryData data) {
        System.out.println("- [" + book.getId() + "] " + book.getTitle() + " — " + book.getAuthor());
        System.out.println("  " + book.getCategory() + " · " + availableCopies(book, data) + "/" + book.getTotalCopies()
                + " copies available · " + book.getLocation());
    }

    private static int availableCopies(Book book, LibraryData data) {
        long onLoan = data.getBorrowRecords().stream()
                .filter(r -> r.getBookId() == book.getId() && !r.isReturned()).count();
        return Math.max(0, book.getTotalCopies() - (int) onLoan);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }

    private static String readRequired(Scanner scanner, String prompt) {
        while (true) {
            String value = readLine(scanner, prompt).trim();
            if (!value.isEmpty()) return value;
            System.out.println("Please enter a value to continue.");
        }
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            String input = readLine(scanner, prompt).trim();
            try { return Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("Please enter a number from the menu."); }
        }
    }

    private static long readLong(Scanner scanner, String prompt) {
        while (true) {
            String input = readLine(scanner, prompt).trim();
            try { return Long.parseLong(input); }
            catch (NumberFormatException e) { System.out.println("Please enter a whole number."); }
        }
    }

    private static long readPositiveLong(Scanner scanner, String prompt) {
        while (true) {
            long value = readLong(scanner, prompt);
            if (value > 0) return value;
            System.out.println("Please enter a value greater than zero.");
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value > 0) return value;
            System.out.println("Please enter a value greater than zero.");
        }
    }

    private static void pause(Scanner scanner) {
        System.out.print("\nPress Enter to return to the menu...");
        if (scanner.hasNextLine()) scanner.nextLine();
    }
}
