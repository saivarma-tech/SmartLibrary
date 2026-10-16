package smartlibrary.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Base64;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import smartlibrary.data.LibraryData;
import smartlibrary.model.SearchResult;
import smartlibrary.model.Book;
import smartlibrary.model.BorrowRecord;
import smartlibrary.model.ReservationRecord;
import smartlibrary.model.ResearchPaper;
import smartlibrary.model.MemberAccount;
import smartlibrary.model.SearchUsage;
import smartlibrary.algorithms.RandomizedParallelAlgorithms;
import smartlibrary.service.SearchService;
import smartlibrary.service.QueryCorrectionService;
import smartlibrary.service.DocumentIndexService;
import smartlibrary.algorithms.ComplexityAlgorithms;

public class LibraryServer {

    private static final int DEFAULT_PORT = 8080;

    private static final LibraryData libraryData =
            new LibraryData();

    private static final SearchService searchService =
            new SearchService(libraryData);
    private static final QueryCorrectionService correctionService = new QueryCorrectionService(libraryData);
    private static final DocumentIndexService documentIndexService = new DocumentIndexService(libraryData);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        final int port;
        try {
            port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            System.err.println("Invalid port. Run LibraryServer with a port number from 1 to 65535.");
            return;
        }
        if (port < 1 || port > 65535) {
            System.err.println("Invalid port. Choose a number from 1 to 65535.");
            return;
        }

        HttpServer server = null;
        int activePort = port;
        int lastPortToTry = Math.min(65535, port + 10);
        for (int candidatePort = port; candidatePort <= lastPortToTry; candidatePort++) {
            try {
                server = HttpServer.create(
                        new InetSocketAddress(InetAddress.getLoopbackAddress(), candidatePort), 0);
                activePort = candidatePort;
                break;
            } catch (BindException exception) {
                if (candidatePort == port) {
                    System.err.println("Port " + port + " is already in use. Trying another local port...");
                }
            }
        }
        if (server == null) {
            System.err.println("SmartLibrary could not start. Ports " + port + " through "
                    + lastPortToTry + " are already in use.");
            System.err.println("Stop an older SmartLibrary server or choose a free port.");
            return;
        }

        server.createContext(
                "/",
                LibraryServer::homeHandler
        );

        server.createContext(
                "/api/",
                LibraryServer::apiHandler
        );

        server.setExecutor(null);
        server.start();

        System.out.println("======================================");
        System.out.println("SmartLibrary backend started");
        System.out.println("Backend URL: http://localhost:" + activePort);
        System.out.println(
                "Status URL: http://localhost:"
                        + activePort
                        + "/api/status"
        );
        System.out.println(
                "Search URL: http://localhost:"
                        + activePort
                        + "/api/search?keyword=algorithm"
        );
        System.out.println("======================================");
    }

    // ==========================================
    // HOME PAGE
    // ==========================================

    private static void apiHandler(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();
        if (method.equals("OPTIONS")) { sendResponse(exchange, 204, "", "application/json; charset=UTF-8"); return; }
        try {
            if (path.equals("/api/status") && method.equals("GET")) {
                respond(exchange, 200, Map.of("success", true, "message", "SmartLibrary is ready"));
            } else if (path.equals("/api/books") || path.startsWith("/api/books/")) {
                booksApi(exchange, path, method);
            } else if (path.equals("/api/catalogue") && method.equals("GET")) {
                catalogueApi(exchange);
            } else if (path.startsWith("/api/related/") && method.equals("GET")) {
                relatedApi(exchange, path);
            } else if (path.equals("/api/records-review") && method.equals("GET")) {
                recordsReviewApi(exchange);
            } else if (path.equals("/api/usage") && method.equals("GET")) {
                usageApi(exchange);
            } else if (path.equals("/api/loans") || path.startsWith("/api/loans/")) {
                loansApi(exchange, path, method);
            } else if (path.equals("/api/reservations") || path.startsWith("/api/reservations/")) {
                reservationsApi(exchange, path, method);
            } else if (path.equals("/api/logout") && method.equals("POST")) {
                String header = exchange.getRequestHeaders().getFirst("Authorization");
                if (header != null && header.startsWith("Bearer ")) sessions.remove(header.substring(7).trim());
                respond(exchange, 200, Map.of("success", true));
            } else if (path.equals("/api/members") || path.startsWith("/api/members/") || path.equals("/api/me")) {
                membersApi(exchange, path, method);
            } else if (path.equals("/api/stats") && method.equals("GET")) {
                statsApi(exchange);
            } else if (path.equals("/api/discover") && method.equals("GET")) {
                discoverApi(exchange);
            } else if (path.equals("/api/search") && method.equals("GET")) {
                searchHandler(exchange);
            } else {
                respond(exchange, 404, Map.of("error", "That library service was not found."));
            }
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, Map.of("error", safeMessage(exception)));
        } catch (SecurityException exception) {
            respond(exchange, 403, Map.of("error", safeMessage(exception)));
        } catch (NotFoundException exception) {
            respond(exchange, 404, Map.of("error", safeMessage(exception)));
        } catch (IllegalStateException exception) {
            respond(exchange, 409, Map.of("error", safeMessage(exception)));
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            respond(exchange, 500, Map.of("error", "The request could not be completed."));
        }
    }

    private static void booksApi(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/books") && method.equals("GET")) {
            Map<String, String> params = parseQueryParameters(exchange.getRequestURI().getRawQuery());
            String query = params.getOrDefault("q", "").trim();
            validateSearchText(query);
            String category = params.getOrDefault("category", "").trim();
            String author = params.getOrDefault("author", "").trim();
            String year = params.getOrDefault("year", "").trim();
            String available = params.getOrDefault("available", "").trim();
            String sort = params.getOrDefault("sort", "title").trim();
            Set<Integer> matches = new HashSet<>();
            if (!query.isEmpty()) for (SearchResult result : searchService.search(query)) {
                if (result.getType().equals("BOOK")) matches.add(result.getId());
            }
            List<Object> result = new ArrayList<>();
            libraryData.getBooks().stream().sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                    .filter(book -> query.isEmpty() || matches.contains(book.getId())
                            || contains(book.getIsbn(), query) || contains(book.getDescription(), query))
                    .filter(book -> category.isEmpty() || contains(book.getCategory(), category))
                    .filter(book -> author.isEmpty() || contains(book.getAuthor(), author))
                    .filter(book -> year.isEmpty() || String.valueOf(book.getPublicationYear()).equals(year))
                    .filter(book -> available.isEmpty() || (available.equalsIgnoreCase("true") == (availableCopies(book) > 0)))
                    .forEach(book -> result.add(bookJson(book)));
            if (sort.equals("author")) result.sort(Comparator.comparing(row -> String.valueOf(((Map<?, ?>) row).get("author")), String.CASE_INSENSITIVE_ORDER));
            else if (sort.equals("year")) result.sort(Comparator.comparingInt(row -> -((Number)((Map<?, ?>)row).get("publicationYear")).intValue()));
            respond(exchange, 200, Map.of("books", result));
            return;
        }
        if (path.equals("/api/books") && method.equals("POST")) {
            requireAdmin(exchange);
            Map<String, Object> body = body(exchange);
            String title = required(body, "title");
            String author = required(body, "author");
            String category = required(body, "category");
            int copies = integer(body, "totalCopies", 1);
            if (copies < 1 || copies > 10000) throw new IllegalArgumentException("Copies must be between 1 and 10,000.");
            int year = integer(body, "publicationYear", 0);
            validateYear(year);
            validateUniqueIsbn(optional(body, "isbn"), -1);
            int id = libraryData.nextBookId();
            Book book = new Book(id, title, author, category, optional(body, "description"),
                    optional(body, "isbn"), year,
                    optionalDefault(body, "location", "Main Library"), copies);
            libraryData.addBook(book);
            respond(exchange, 201, bookJson(book));
            return;
        }
        String[] parts = path.split("/");
        if (parts.length != 4) throw new NotFoundException("Book record not found.");
        requireAdmin(exchange);
        int id = parseId(parts[3]);
        Book book = libraryData.getBookById(id);
        if (book == null) throw new NotFoundException("Book record not found.");
        if (method.equals("PUT")) {
            Map<String, Object> body = body(exchange);
            int copies = integer(body, "totalCopies", book.getTotalCopies());
            int reserved = (int) libraryData.getReservations().stream().filter(r -> r.getBookId() == id && r.isFulfilled()).count();
            if (copies < activeLoans(id) + reserved || copies < 1) throw new IllegalArgumentException("Copies cannot be fewer than copies currently borrowed or held for pickup.");
            int year = integer(body, "publicationYear", 0); validateYear(year);
            String isbn = optional(body, "isbn"); validateUniqueIsbn(isbn, id);
            book.update(required(body, "title"), required(body, "author"), required(body, "category"),
                    optional(body, "description"), isbn, year,
                    optionalDefault(body, "location", "Main Library"), copies);
            libraryData.save();
            promoteReservations(id);
            respond(exchange, 200, bookJson(book));
        } else if (method.equals("DELETE")) {
            if (activeLoans(id) > 0) throw new IllegalStateException("Return all copies before archiving this title.");
            if (libraryData.getReservations().stream().anyMatch(r -> r.getBookId() == id))
                throw new IllegalStateException("Resolve the waiting list before archiving this title.");
            libraryData.removeBookById(id);
            respond(exchange, 200, Map.of("success", true, "message", "Book archived."));
        } else {
            respond(exchange, 405, Map.of("error", "Use GET, POST, PUT, or DELETE for book records."));
        }
    }

    private static void catalogueApi(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryParameters(exchange.getRequestURI().getRawQuery());
        String query = params.getOrDefault("q", "").trim();
        validateSearchText(query);
        String category = params.getOrDefault("category", "").trim();
        String author = params.getOrDefault("author", "").trim();
        String type = params.getOrDefault("type", "").trim();
        String year = params.getOrDefault("year", "").trim();
        String available = params.getOrDefault("available", "").trim();
        Set<String> matches = new HashSet<>();
        if (!query.isEmpty()) libraryData.addSearchUsage(new SearchUsage(query, System.currentTimeMillis()));
        if (!query.isEmpty()) for (SearchResult result : searchService.search(query)) {
            matches.add(result.getType() + ":" + result.getId());
        }
        List<Object> rows = new ArrayList<>();
        for (Book book : libraryData.getBooks()) {
            if ((!query.isEmpty() && !matches.contains("BOOK:" + book.getId())
                    && !contains(book.getIsbn(), query) && !contains(book.getDescription(), query))
                    || (!category.isEmpty() && !contains(book.getCategory(), category))
                    || (!author.isEmpty() && !contains(book.getAuthor(), author))
                    || (!year.isEmpty() && !String.valueOf(book.getPublicationYear()).equals(year))
                    || (!available.isEmpty() && (available.equalsIgnoreCase("true") != (availableCopies(book) > 0)))
                    || (!type.isEmpty() && !type.equalsIgnoreCase("book"))) continue;
            rows.add(bookJson(book));
        }
        for (ResearchPaper paper : libraryData.getResearchPapers()) {
            if ((!query.isEmpty() && !matches.contains("RESEARCH PAPER:" + paper.getId()))
                    || (!category.isEmpty() && !contains(paper.getCategory(), category))
                    || (!author.isEmpty() && !contains(paper.getAuthors(), author))
                    || (!year.isEmpty() && !year.equals("0"))
                    || !available.isEmpty()
                    || (!type.isEmpty() && !type.equalsIgnoreCase("paper"))) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", paper.getId()); row.put("type", "PAPER"); row.put("title", paper.getTitle());
            row.put("author", paper.getAuthors()); row.put("category", paper.getCategory());
            row.put("description", paper.getAbstractText()); row.put("isbn", "");
            row.put("publicationYear", 0); row.put("location", "Digital collection");
            row.put("totalCopies", 0); row.put("availableCopies", 0); rows.add(row);
        }
        rows.sort(Comparator.comparing(row -> String.valueOf(((Map<?, ?>) row).get("title")), String.CASE_INSENSITIVE_ORDER));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", rows);
        if (query.isEmpty() || !rows.isEmpty()) response.put("suggestion", "");
        else response.put("suggestion", correctionService.findSuggestion(query));
        respond(exchange, 200, response);
    }

    private static void relatedApi(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length != 4) throw new NotFoundException("Related titles not found.");
        int id = parseId(parts[3]); List<Object> results = new ArrayList<>();
        for (DocumentIndexService.RelatedDocumentResult match : documentIndexService.findRelatedTo(id, 5)) {
            results.add(Map.of("id", match.id, "type", match.type, "title", match.title, "sharedPhrase", match.sharedPhrase));
        }
        respond(exchange, 200, Map.of("related", results));
    }

    private static void recordsReviewApi(HttpExchange exchange) throws IOException {
        requireAdmin(exchange);
        List<String> titles = new ArrayList<>(); List<String> categories = new ArrayList<>();
        List<Map<String, Object>> records = new ArrayList<>();
        for (Book book : libraryData.getBooks()) {
            titles.add(book.getTitle()); categories.add(book.getCategory().trim().toLowerCase());
            records.add(Map.of("id", book.getId(), "type", "BOOK", "title", book.getTitle(), "category", book.getCategory()));
        }
        for (ResearchPaper paper : libraryData.getResearchPapers()) {
            titles.add(paper.getTitle()); categories.add(paper.getCategory().trim().toLowerCase());
            records.add(Map.of("id", paper.getId(), "type", "PAPER", "title", paper.getTitle(), "category", paper.getCategory()));
        }
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) for (int j = i + 1; j < categories.size(); j++)
            if (!categories.get(i).isBlank() && categories.get(i).equals(categories.get(j))) pairs.add(new int[] {i, j});
        int[][] edges = pairs.toArray(int[][]::new); Set<Integer> reviewIndexes = ComplexityAlgorithms.vertexCoverTwoApprox(titles.size(), edges);
        List<Object> review = new ArrayList<>(); for (int index : reviewIndexes) review.add(records.get(index));
        List<Object> pairOutput = new ArrayList<>(); for (int[] pair : pairs)
            pairOutput.add(Map.of("first", records.get(pair[0]), "second", records.get(pair[1])));
        respond(exchange, 200, Map.of("pairs", pairOutput, "recordsToReview", review));
    }

    private static void usageApi(HttpExchange exchange) throws IOException {
        requireAdmin(exchange);
        Map<String, Integer> counts = new HashMap<>();
        for (SearchUsage item : libraryData.getSearchUsage()) counts.merge(item.getQuery().toLowerCase(), 1, Integer::sum);
        UsageCount[] ranked = counts.entrySet().stream().map(e -> new UsageCount(e.getKey(), e.getValue())).toArray(UsageCount[]::new);
        RandomizedParallelAlgorithms.randomizedQuickSort(ranked,
                Comparator.comparingInt((UsageCount u) -> u.count).reversed().thenComparing(u -> u.query), new java.util.Random());
        long[] frequencies = java.util.Arrays.stream(ranked).mapToLong(u -> u.count).toArray();
        long total = RandomizedParallelAlgorithms.parallelReduce(frequencies);
        long[] prefix = RandomizedParallelAlgorithms.blellochExclusiveScan(frequencies);
        List<Object> popular = new ArrayList<>();
        for (int i = 0; i < Math.min(8, ranked.length); i++) popular.add(Map.of("query", ranked[i].query, "count", ranked[i].count));
        List<String> sample = RandomizedParallelAlgorithms.reservoirSample(
                libraryData.getSearchUsage().stream().map(SearchUsage::getQuery).toList(), 5, new java.util.Random());
        java.time.LocalDate firstDay = java.time.LocalDate.now().minusDays(6);
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        long start = firstDay.atStartOfDay(zone).toInstant().toEpochMilli();
        long[] daily = new long[7];
        for (SearchUsage item : libraryData.getSearchUsage()) if (item.getSearchedAt() >= start) {
            int index = (int)java.time.temporal.ChronoUnit.DAYS.between(firstDay,
                    java.time.Instant.ofEpochMilli(item.getSearchedAt()).atZone(zone).toLocalDate());
            if (index >= 0 && index < daily.length) daily[index]++;
        }
        long[] dailyPrefix = RandomizedParallelAlgorithms.blellochExclusiveScan(daily);
        List<Object> days = new ArrayList<>();
        for (int i = 0; i < daily.length; i++) days.add(Map.of("date", java.time.LocalDate.now().minusDays(6 - i).toString(),
                "searches", daily[i], "cumulative", dailyPrefix[i] + daily[i]));
        List<Object> cumulative = new ArrayList<>();
        for (int i = 0; i < ranked.length; i++) cumulative.add(Map.of("query", ranked[i].query, "totalThrough", prefix[i] + frequencies[i]));
        respond(exchange, 200, Map.of("totalSearches", total, "popular", popular, "sample", sample,
                "daily", days, "cumulativeByQuery", cumulative));
    }
    private static final class UsageCount {
        final String query; final int count;
        UsageCount(String query, int count) { this.query = query; this.count = count; }
    }

    private static void loansApi(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/loans") && method.equals("GET")) {
            Map<String, String> params = parseQueryParameters(exchange.getRequestURI().getRawQuery());
            if (params.getOrDefault("all", "").equalsIgnoreCase("true")) {
                requireAdmin(exchange);
                List<Object> all = new ArrayList<>(); libraryData.getBorrowRecords().forEach(l -> all.add(loanJson(l)));
                respond(exchange, 200, Map.of("loans", all)); return;
            }
            MemberAccount member = requireMember(exchange);
            List<Object> rows = new ArrayList<>();
            for (BorrowRecord loan : libraryData.getBorrowRecords()) {
                if (memberOwns(member, loan)) rows.add(loanJson(loan));
            }
            respond(exchange, 200, Map.of("loans", rows));
            return;
        }
        if (path.equals("/api/loans") && method.equals("POST")) {
            Map<String, Object> body = body(exchange);
            int bookId = integer(body, "bookId", -1);
            MemberAccount member = requireMember(exchange);
            Book book = libraryData.getBookById(bookId);
            if (book == null) throw new NotFoundException("That book is no longer in the catalogue.");
            if (activeLoansForMember(member.getEmail()) >= 3) throw new IllegalStateException("You already have the maximum of three books on loan.");
            ReservationRecord ownReady = libraryData.getReservations().stream()
                    .filter(r -> r.getBookId() == bookId && r.getMemberEmail().equalsIgnoreCase(member.getEmail()) && r.isFulfilled())
                    .findFirst().orElse(null);
            if (ownReady == null && availableCopies(book) < 1) throw new IllegalStateException("No copy is available. Join the waiting list to be notified.");
            long now = System.currentTimeMillis();
            BorrowRecord loan = new BorrowRecord(libraryData.nextLoanId(), bookId, member.getName(), member.getEmail(), now,
                    now + 14L * 24 * 60 * 60 * 1000, false, 0);
            libraryData.addBorrowRecord(loan);
            if (ownReady != null) libraryData.getReservations().remove(ownReady);
            libraryData.save();
            promoteReservations(bookId);
            respond(exchange, 201, Map.of("loan", loanJson(loan), "book", book.getTitle()));
            return;
        }
        String[] parts = path.split("/");
        if (parts.length != 5 || !method.equals("POST")) throw new NotFoundException("Loan action not found.");
        int loanId = parseId(parts[3]);
        String action = parts[4];
        MemberAccount member = requireMember(exchange);
        BorrowRecord loan = libraryData.getBorrowRecords().stream().filter(item -> item.getLoanId() == loanId)
                .findFirst().orElse(null);
        if (loan == null) throw new NotFoundException("Loan record not found.");
        if (!memberOwns(member, loan)) throw new SecurityException("This loan belongs to another member.");
        if (action.equals("return")) {
            if (loan.isReturned()) throw new IllegalStateException("This book has already been returned.");
            loan.markReturned(); libraryData.save(); promoteReservations(loan.getBookId());
            respond(exchange, 200, Map.of("success", true, "message", "Book returned. Thank you!"));
        } else if (action.equals("renew")) {
            boolean waiting = libraryData.getReservations().stream().anyMatch(r -> r.getBookId() == loan.getBookId() && !r.isFulfilled());
            if (waiting) throw new IllegalStateException("Another member is waiting for this book, so it cannot be renewed.");
            if (!loan.renew()) throw new IllegalStateException("This loan cannot be renewed. Renewals are limited to two and overdue books cannot be renewed.");
            libraryData.save(); respond(exchange, 200, Map.of("success", true, "loan", loanJson(loan)));
        } else throw new NotFoundException("Loan action not found.");
    }

    private static void reservationsApi(HttpExchange exchange, String path, String method) throws IOException {
        if (method.equals("DELETE")) {
            String[] parts = path.split("/");
            if (parts.length != 4) throw new NotFoundException("Waiting-list entry not found.");
            int id = parseId(parts[3]);
            ReservationRecord reservation = libraryData.getReservations().stream().filter(r -> r.getId() == id)
                    .findFirst().orElse(null);
            if (reservation == null) throw new NotFoundException("Waiting-list entry not found.");
            boolean librarian = hasAdminAccess(exchange);
            if (!librarian) {
                MemberAccount member = requireMember(exchange);
                if (!reservation.getMemberEmail().equalsIgnoreCase(member.getEmail()))
                    throw new SecurityException("This waiting-list entry belongs to another member.");
            }
            int bookId = reservation.getBookId(); libraryData.getReservations().remove(reservation); libraryData.save();
            promoteReservations(bookId); respond(exchange, 200, Map.of("success", true, "message", "Reservation cancelled.")); return;
        }
        if (method.equals("GET")) {
            Map<String, String> params = parseQueryParameters(exchange.getRequestURI().getRawQuery());
            if (params.getOrDefault("all", "").equalsIgnoreCase("true")) {
                requireAdmin(exchange); List<Object> all = new ArrayList<>();
                for (ReservationRecord r : libraryData.getReservations()) all.add(reservationJson(r, queuePosition(r)));
                respond(exchange, 200, Map.of("reservations", all)); return;
            }
            MemberAccount member = requireMember(exchange);
            List<Object> rows = new ArrayList<>();
            List<ReservationRecord> active = libraryData.getReservations().stream()
                    .filter(r -> !r.isFulfilled() || r.getMemberEmail().equalsIgnoreCase(member.getEmail())).toList();
            for (ReservationRecord reservation : active) if (reservation.getMemberEmail().equalsIgnoreCase(member.getEmail())) {
                rows.add(reservationJson(reservation, queuePosition(reservation)));
            }
            respond(exchange, 200, Map.of("reservations", rows)); return;
        }
        if (!method.equals("POST")) { respond(exchange, 405, Map.of("error", "Use GET or POST for reservations.")); return; }
        Map<String, Object> body = body(exchange);
        int bookId = integer(body, "bookId", -1); MemberAccount member = requireMember(exchange);
        Book book = libraryData.getBookById(bookId);
        if (book == null) throw new NotFoundException("Book record not found.");
        if (availableCopies(book) > 0) throw new IllegalStateException("A copy is available now. Borrow it from the catalogue.");
        if (libraryData.getReservations().stream().anyMatch(r -> r.getBookId() == bookId && r.getMemberEmail().equalsIgnoreCase(member.getEmail())))
            throw new IllegalStateException("You already have a place on this waiting list.");
        ReservationRecord record = new ReservationRecord(libraryData.nextReservationId(), bookId, member.getName(), member.getEmail(), System.currentTimeMillis(), false);
        libraryData.addReservation(record);
        int position = (int) libraryData.getReservations().stream().filter(r -> r.getBookId() == bookId && !r.isFulfilled()).count();
        respond(exchange, 201, Map.of("success", true, "position", position, "message", "You joined the waiting list."));
    }

    private static void statsApi(HttpExchange exchange) throws IOException {
        int copies = libraryData.getBooks().stream().mapToInt(Book::getTotalCopies).sum();
        int active = (int) libraryData.getBorrowRecords().stream().filter(l -> !l.isReturned()).count();
        int ready = (int) libraryData.getReservations().stream().filter(ReservationRecord::isFulfilled).count();
        int overdue = (int) libraryData.getBorrowRecords().stream().filter(BorrowRecord::isOverdue).count();
        respond(exchange, 200, Map.of("titles", libraryData.getBooks().size(), "copies", copies,
                "availableCopies", Math.max(0, copies - active - ready), "activeLoans", active,
                "overdueLoans", overdue, "membersSeen", libraryData.getMembers().size()));
    }

    private static void discoverApi(HttpExchange exchange) throws IOException {
        Map<Integer, Integer> popularity = new HashMap<>();
        for (BorrowRecord loan : libraryData.getBorrowRecords()) popularity.merge(loan.getBookId(), 1, Integer::sum);
        Book[] books = libraryData.getBooks().stream().filter(b -> popularity.getOrDefault(b.getId(), 0) > 0).toArray(Book[]::new);
        RandomizedParallelAlgorithms.randomizedQuickSort(books,
                Comparator.comparingInt((Book b) -> popularity.getOrDefault(b.getId(), 0)).reversed()
                        .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER), new java.util.Random());
        List<Object> popular = new ArrayList<>(); for (int i = 0; i < Math.min(6, books.length); i++) popular.add(bookJson(books[i]));
        List<Object> recent = new ArrayList<>();
        libraryData.getBooks().stream().sorted(Comparator.comparingInt(Book::getId).reversed())
                .limit(6).map(LibraryServer::bookJson).forEach(recent::add);
        List<Object> recommendations = new ArrayList<>();
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null) {
            try {
                MemberAccount member = requireMember(exchange);
                Map<String, Integer> interests = new HashMap<>(); Set<Integer> alreadyBorrowed = new HashSet<>();
                for (BorrowRecord loan : libraryData.getBorrowRecords()) if (memberOwns(member, loan)) {
                    Book past = libraryData.getBookById(loan.getBookId());
                    if (past != null) interests.merge(past.getCategory().toLowerCase(), 1, Integer::sum);
                    alreadyBorrowed.add(loan.getBookId());
                }
            Book[] suggestions = libraryData.getBooks().stream().filter(b -> !alreadyBorrowed.contains(b.getId()))
                        .toArray(Book[]::new);
                RandomizedParallelAlgorithms.randomizedQuickSort(suggestions,
                        Comparator.comparingInt((Book b) -> interests.getOrDefault(b.getCategory().toLowerCase(), 0)).reversed()
                                .thenComparing(Comparator.comparingInt((Book b) -> popularity.getOrDefault(b.getId(), 0)).reversed())
                                .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER), new java.util.Random());
                for (int i = 0; i < Math.min(4, suggestions.length); i++) {
                    Book suggestion = suggestions[i]; Map<String, Object> row = bookJson(suggestion);
                    String category = suggestion.getCategory();
                    row.put("recommendationReason", interests.getOrDefault(category.toLowerCase(), 0) > 0
                            ? "In a subject you have borrowed before"
                            : popularity.values().stream().anyMatch(count -> count > 0) ? "Popular with readers" : "Selected from the catalogue");
                    recommendations.add(row);
                }
            } catch (SecurityException ignored) { /* Public discovery still works for visitors. */ }
        }
        List<String> categories = libraryData.getBooks().stream().map(Book::getCategory).filter(c -> !c.isBlank())
                .distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        respond(exchange, 200, Map.of("popular", popular, "recent", recent,
                "categories", categories, "recommendations", recommendations));
    }

    private static Map<String, Object> body(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(64 * 1024 + 1);
        if (bytes.length > 64 * 1024) throw new IllegalArgumentException("Request is too large.");
        String raw = new String(bytes, StandardCharsets.UTF_8);
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/json"))
            throw new IllegalArgumentException("Send form data as JSON.");
        return JsonCodec.parseObject(raw);
    }

    private static void membersApi(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/members") && method.equals("GET")) {
            requireAdmin(exchange);
            List<Object> result = new ArrayList<>();
            libraryData.getMembers().forEach(member -> result.add(Map.of("name", member.getName(),
                    "email", member.getEmail(), "joinedAt", member.getCreatedAt())));
            respond(exchange, 200, Map.of("members", result)); return;
        }
        if (path.equals("/api/me") && method.equals("GET")) {
            MemberAccount member = requireMember(exchange);
            respond(exchange, 200, Map.of("name", member.getName(), "email", member.getEmail())); return;
        }
        if (!path.equals("/api/members/register") && !path.equals("/api/members/login"))
            throw new NotFoundException("Member service not found.");
        if (!method.equals("POST")) { respond(exchange, 405, Map.of("error", "Use POST for member sign-in.")); return; }
        Map<String, Object> request = body(exchange);
        String email = required(request, "email").toLowerCase();
        String password = required(request, "password");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Enter a valid email address.");
        if (path.endsWith("register")) {
            String name = required(request, "name");
            if (password.length() < 8 || password.length() > 128) throw new IllegalArgumentException("Use a password between 8 and 128 characters.");
            if (libraryData.getMember(email) != null) throw new IllegalStateException("An account already exists for that email.");
            byte[] salt = new byte[16]; secureRandom.nextBytes(salt);
            MemberAccount account = new MemberAccount(name, email, Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(passwordHash(password, salt)), System.currentTimeMillis());
            libraryData.addMember(account);
            respond(exchange, 201, sessionJson(account)); return;
        }
        MemberAccount account = libraryData.getMember(email);
        if (account == null) throw new SecurityException("Email or password is incorrect.");
        byte[] salt = Base64.getDecoder().decode(account.getSalt());
        byte[] expected = Base64.getDecoder().decode(account.getPasswordHash());
        byte[] actual = passwordHash(password, salt);
        if (!java.security.MessageDigest.isEqual(expected, actual)) throw new SecurityException("Email or password is incorrect.");
        respond(exchange, 200, sessionJson(account));
    }

    private static Map<String, Object> sessionJson(MemberAccount member) {
        byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(member.getEmail(), System.currentTimeMillis() + 12L * 60 * 60 * 1000));
        return Map.of("token", token, "member", Map.of("name", member.getName(), "email", member.getEmail()));
    }

    private static byte[] passwordHash(String password, byte[] salt) {
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, 150000, 256);
        try { return javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException exception) { throw new IllegalStateException("Secure password storage is unavailable.", exception); }
        finally { spec.clearPassword(); }
    }

    private static MemberAccount requireMember(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new SecurityException("Sign in to your member account first.");
        String token = header.substring(7).trim(); Session session = sessions.get(token);
        if (session == null || session.expiresAt < System.currentTimeMillis()) {
            if (session != null) sessions.remove(token);
            throw new SecurityException("Your sign-in has expired. Please sign in again.");
        }
        MemberAccount member = libraryData.getMember(session.email);
        if (member == null) throw new SecurityException("Member account not found.");
        return member;
    }

    private record Session(String email, long expiresAt) { }

    private static void requireAdmin(HttpExchange exchange) {
        if (!hasAdminAccess(exchange)) throw new SecurityException("Librarian sign-in is required for this action.");
    }
    private static boolean hasAdminAccess(HttpExchange exchange) {
        String expected = System.getenv().getOrDefault("SMARTLIBRARY_ADMIN_KEY", "library-admin");
        String supplied = exchange.getRequestHeaders().getFirst("X-Admin-Key");
        return supplied != null && java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, Object> bookJson(Book book) {
        int borrowed = activeLoans(book.getId());
        int reserved = (int) libraryData.getReservations().stream().filter(r -> r.getBookId() == book.getId() && r.isFulfilled()).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", book.getId()); result.put("type", "BOOK"); result.put("title", book.getTitle());
        result.put("author", book.getAuthor()); result.put("category", book.getCategory());
        result.put("description", book.getDescription()); result.put("isbn", book.getIsbn());
        result.put("publicationYear", book.getPublicationYear()); result.put("location", book.getLocation());
        result.put("totalCopies", book.getTotalCopies());
        result.put("availableCopies", Math.max(0, book.getTotalCopies() - borrowed - reserved));
        return result;
    }

    private static Map<String, Object> loanJson(BorrowRecord loan) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", loan.getLoanId()); result.put("bookId", loan.getBookId());
        result.put("title", title(loan.getBookId())); result.put("member", loan.getBorrowerName());
        result.put("borrowedAt", loan.getBorrowedAt()); result.put("dueAt", loan.getDueAt());
        result.put("returnedAt", loan.getReturnedAt());
        result.put("returned", loan.isReturned()); result.put("overdue", loan.isOverdue());
        result.put("renewals", loan.getRenewals()); result.put("status", loan.isReturned() ? "Returned" : loan.isOverdue() ? "Overdue" : "Borrowed");
        return result;
    }

    private static Map<String, Object> reservationJson(ReservationRecord reservation, int position) {
        return Map.of("id", reservation.getId(), "bookId", reservation.getBookId(),
                "title", title(reservation.getBookId()), "member", reservation.getMemberName(),
                "status", reservation.isFulfilled() ? "Ready for pickup" : "Waiting",
                "position", position, "createdAt", reservation.getCreatedAt());
    }
    private static int queuePosition(ReservationRecord reservation) {
        if (reservation.isFulfilled()) return 0;
        return (int) libraryData.getReservations().stream().filter(r -> r.getBookId() == reservation.getBookId()
                && !r.isFulfilled() && (r.getCreatedAt() < reservation.getCreatedAt()
                        || (r.getCreatedAt() == reservation.getCreatedAt() && r.getId() <= reservation.getId()))).count();
    }

    private static int activeLoans(int bookId) {
        return (int) libraryData.getBorrowRecords().stream().filter(l -> l.getBookId() == bookId && !l.isReturned()).count();
    }
    private static int activeLoansForMember(String member) {
        return (int) libraryData.getBorrowRecords().stream().filter(l -> !l.isReturned()
                && (!l.getMemberEmail().isEmpty() ? l.getMemberEmail().equalsIgnoreCase(member)
                        : l.getBorrowerName().equalsIgnoreCase(member))).count();
    }
    private static boolean memberOwns(MemberAccount member, BorrowRecord loan) {
        return !loan.getMemberEmail().isEmpty() ? loan.getMemberEmail().equalsIgnoreCase(member.getEmail())
                : loan.getBorrowerName().equalsIgnoreCase(member.getName());
    }
    private static int availableCopies(Book book) {
        int ready = (int) libraryData.getReservations().stream().filter(r -> r.getBookId() == book.getId() && r.isFulfilled()).count();
        return Math.max(0, book.getTotalCopies() - activeLoans(book.getId()) - ready);
    }
    private static void promoteReservations(int bookId) {
        Book book = libraryData.getBookById(bookId);
        if (book == null) return;
        int free = book.getTotalCopies() - activeLoans(bookId)
                - (int) libraryData.getReservations().stream().filter(r -> r.getBookId() == bookId && r.isFulfilled()).count();
        if (free <= 0) return;
        PriorityQueue<ReservationRecord> waiting = new PriorityQueue<>(Comparator
                .comparingLong(ReservationRecord::getCreatedAt).thenComparingInt(ReservationRecord::getId));
        libraryData.getReservations().stream().filter(r -> r.getBookId() == bookId && !r.isFulfilled()).forEach(waiting::add);
        while (free-- > 0 && !waiting.isEmpty()) waiting.remove().setFulfilled(true);
        libraryData.save();
    }
    private static String title(int bookId) {
        Book book = libraryData.getBookById(bookId); return book == null ? "Archived title" : book.getTitle();
    }
    private static boolean contains(String text, String part) {
        return text != null && text.toLowerCase().contains(part.toLowerCase());
    }
    private static String required(Map<String, Object> body, String key) {
        String value = optional(body, key);
        if (value.isBlank()) throw new IllegalArgumentException("Please enter " + key + ".");
        if (value.length() > 300) throw new IllegalArgumentException("The " + key + " is too long.");
        return value;
    }
    private static void validateYear(int year) {
        int maximum = java.time.Year.now().getValue() + 1;
        if (year != 0 && (year < 1400 || year > maximum)) throw new IllegalArgumentException("Enter a publication year between 1400 and " + maximum + ".");
    }
    private static void validateUniqueIsbn(String isbn, int exceptId) {
        if (isbn.isBlank()) return;
        if (isbn.length() > 30 || !isbn.matches("[0-9xX -]+")) throw new IllegalArgumentException("ISBN can use only digits, spaces, hyphens, and X.");
        for (Book book : libraryData.getBooks()) if (book.getId() != exceptId && book.getIsbn().equalsIgnoreCase(isbn))
            throw new IllegalStateException("That ISBN is already in the catalogue.");
    }
    private static void validateSearchText(String query) {
        if (query.length() > 200) throw new IllegalArgumentException("Search text must be 200 characters or fewer.");
    }
    private static String optional(Map<String, Object> body, String key) {
        Object value = body.get(key); return value == null ? "" : String.valueOf(value).trim();
    }
    private static String optionalDefault(Map<String, Object> body, String key, String fallback) {
        String value = optional(body, key); return value.isEmpty() ? fallback : value;
    }
    private static int integer(Map<String, Object> body, String key, int fallback) {
        Object value = body.get(key); if (value == null || String.valueOf(value).isBlank()) return fallback;
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Enter a valid number for " + key + "."); }
    }
    private static int parseId(String value) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Record ID is invalid."); }
    }
    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? "The request could not be completed." : exception.getMessage();
    }
    private static void respond(HttpExchange exchange, int status, Object payload) throws IOException {
        sendResponse(exchange, status, JsonCodec.stringify(payload), "application/json; charset=UTF-8");
    }
    private static final class NotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        NotFoundException(String message) { super(message); }
    }

    private static void homeHandler(
            HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "Only GET method is allowed",
                    "text/plain; charset=UTF-8"
            );

            return;
        }

        String html = Files.readString(
                Path.of("web", "index.html"),
                StandardCharsets.UTF_8
        );

        sendResponse(
                exchange,
                200,
                html,
                "text/html; charset=UTF-8"
        );
    }

    // ==========================================
    // STATUS API
    // ==========================================

    private static void statusHandler(
            HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Only GET method is allowed\"}",
                    "application/json; charset=UTF-8"
            );

            return;
        }

        String response = """
                {
                    "success": true,
                    "message": "SmartLibrary backend is running",
                    "project": "SmartLibrary",
                    "searchService": "Ready"
                }
                """;

        sendResponse(
                exchange,
                200,
                response,
                "application/json; charset=UTF-8"
        );
    }

    // ==========================================
    // SEARCH API
    // ==========================================

    private static void searchHandler(
            HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Only GET method is allowed\"}",
                    "application/json; charset=UTF-8"
            );

            return;
        }

        URI requestUri = exchange.getRequestURI();

        String query = requestUri.getRawQuery();

        Map<String, String> parameters =
                parseQueryParameters(query);

        String keyword = parameters
                .getOrDefault("keyword", "")
                .trim();

        if (keyword.isEmpty()) {

            String response = """
                    {
                        "success": false,
                        "error": "Please provide a keyword",
                        "example": "/api/search?keyword=algorithm"
                    }
                    """;

            sendResponse(
                    exchange,
                    400,
                    response,
                    "application/json; charset=UTF-8"
            );

            return;
        }

        // ==========================================
        // CALL YOUR REAL SEARCH SERVICE
        // ==========================================

        List<SearchResult> results =
                searchService.search(keyword);

        String jsonResponse =
                createSearchJson(keyword, results);

        sendResponse(
                exchange,
                200,
                jsonResponse,
                "application/json; charset=UTF-8"
        );
    }

    // ==========================================
    // CREATE SEARCH JSON
    // ==========================================

    private static String createSearchJson(
            String keyword,
            List<SearchResult> results) {

        StringBuilder json =
                new StringBuilder();

        json.append("{");

        json.append("\"success\":true,");
        json.append("\"keyword\":\"")
                .append(escapeJson(keyword))
                .append("\",");

        json.append("\"message\":\"Real SmartLibrary search completed\",");

        json.append("\"matchingDocuments\":[");

        for (int i = 0; i < results.size(); i++) {

            SearchResult result = results.get(i);

            json.append("{");

            json.append("\"type\":\"")
                    .append(escapeJson(result.getType()))
                    .append("\",");

            json.append("\"id\":")
                    .append(result.getId())
                    .append(",");

            json.append("\"title\":\"")
                    .append(escapeJson(result.getTitle()))
                    .append("\",");

            json.append("\"author\":\"")
                    .append(escapeJson(result.getAuthor()))
                    .append("\",");

            json.append("\"matchedIn\":\"")
                    .append(escapeJson(result.getMatchedIn()))
                    .append("\",");

            json.append("\"score\":")
                    .append(result.getScore());

            json.append("}");

            if (i < results.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        json.append("}");

        return json.toString();
    }

    // ==========================================
    // PARSE URL PARAMETERS
    // ==========================================

    private static Map<String, String> parseQueryParameters(
            String query) {

        Map<String, String> parameters =
                new HashMap<>();

        if (query == null || query.isEmpty()) {
            return parameters;
        }

        String[] pairs = query.split("&");

        for (String pair : pairs) {

            String[] keyValue =
                    pair.split("=", 2);

            String key =
                    URLDecoder.decode(
                            keyValue[0],
                            StandardCharsets.UTF_8
                    );

            String value = "";

            if (keyValue.length == 2) {

                value =
                        URLDecoder.decode(
                                keyValue[1],
                                StandardCharsets.UTF_8
                        );
            }

            parameters.put(key, value);
        }

        return parameters;
    }

    // ==========================================
    // SEND HTTP RESPONSE
    // ==========================================

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response,
            String contentType) throws IOException {

        Headers headers =
                exchange.getResponseHeaders();

        headers.set(
                "Content-Type",
                contentType
        );

        byte[] responseBytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(responseBytes);
        }
    }

    // ==========================================
    // ESCAPE JSON CHARACTERS
    // ==========================================

    private static String escapeJson(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
