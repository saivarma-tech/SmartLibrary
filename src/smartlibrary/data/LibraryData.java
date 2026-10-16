package smartlibrary.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import smartlibrary.model.Book;
import smartlibrary.model.BorrowRecord;
import smartlibrary.model.ReservationRecord;
import smartlibrary.model.ResearchPaper;
import smartlibrary.model.MemberAccount;
import smartlibrary.model.SearchUsage;

/** Shared library data, persisted as a small human-independent TSV store. */
public class LibraryData {
    private final List<Book> books = new ArrayList<>();
    private final List<ResearchPaper> researchPapers = new ArrayList<>();
    private final List<BorrowRecord> borrowRecords = new ArrayList<>();
    private final List<ReservationRecord> reservations = new ArrayList<>();
    private final List<MemberAccount> members = new ArrayList<>();
    private final List<SearchUsage> searchUsage = new ArrayList<>();
    private final Path storePath;
    private int nextLoanId = 1;
    private int nextReservationId = 1;

    public LibraryData() {
        storePath = Path.of(System.getProperty("smartlibrary.data", "data/smartlibrary.tsv"));
        if (!load()) {
            loadSampleBooks();
            loadSampleResearchPapers();
            save();
        }
    }

    private boolean load() {
        if (!Files.isRegularFile(storePath)) return false;
        try {
            for (String line : Files.readAllLines(storePath, StandardCharsets.UTF_8)) {
                String[] f = line.split("\\t", -1);
                switch (f[0]) {
                    case "B" -> books.add(new Book(Integer.parseInt(f[1]), dec(f[2]), dec(f[3]),
                            dec(f[4]), dec(f[5]), dec(f[6]), Integer.parseInt(f[7]),
                            dec(f[8]), Integer.parseInt(f[9])));
                    case "P" -> researchPapers.add(new ResearchPaper(Integer.parseInt(f[1]),
                            dec(f[2]), dec(f[3]), dec(f[4]), dec(f[5])));
                    case "L" -> {
                        String email = f.length > 8 ? dec(f[8]) : "";
                        long returnedAt = f.length > 9 ? Long.parseLong(f[9]) : 0;
                        BorrowRecord record = new BorrowRecord(Integer.parseInt(f[1]),
                                Integer.parseInt(f[2]), dec(f[3]), email, Long.parseLong(f[4]),
                                Long.parseLong(f[5]), Boolean.parseBoolean(f[6]), Integer.parseInt(f[7]), returnedAt);
                        borrowRecords.add(record);
                        nextLoanId = Math.max(nextLoanId, record.getLoanId() + 1);
                    }
                    case "R" -> {
                        ReservationRecord record = new ReservationRecord(Integer.parseInt(f[1]),
                                Integer.parseInt(f[2]), dec(f[3]), Long.parseLong(f[4]),
                                Boolean.parseBoolean(f[5]));
                        if (f.length > 6 && !dec(f[6]).isEmpty()) {
                            record = new ReservationRecord(record.getId(), record.getBookId(), record.getMemberName(),
                                    dec(f[6]), record.getCreatedAt(), record.isFulfilled());
                        }
                        reservations.add(record);
                        nextReservationId = Math.max(nextReservationId, record.getId() + 1);
                    }
                    case "M" -> members.add(new MemberAccount(dec(f[1]), dec(f[2]), dec(f[3]), dec(f[4]), Long.parseLong(f[5])));
                    case "U" -> searchUsage.add(new SearchUsage(dec(f[1]), Long.parseLong(f[2])));
                    default -> { /* Ignore unknown future record types. */ }
                }
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not read library data at " + storePath, exception);
        }
    }

    public synchronized void save() {
        List<String> lines = new ArrayList<>();
        for (Book b : books) lines.add(String.join("\t", "B", s(b.getId()), enc(b.getTitle()),
                enc(b.getAuthor()), enc(b.getCategory()), enc(b.getDescription()), enc(b.getIsbn()),
                s(b.getPublicationYear()), enc(b.getLocation()), s(b.getTotalCopies())));
        for (ResearchPaper p : researchPapers) lines.add(String.join("\t", "P", s(p.getId()),
                enc(p.getTitle()), enc(p.getAuthors()), enc(p.getCategory()), enc(p.getAbstractText())));
        for (BorrowRecord l : borrowRecords) lines.add(String.join("\t", "L", s(l.getLoanId()),
                s(l.getBookId()), enc(l.getBorrowerName()), s(l.getBorrowedAt()), s(l.getDueAt()),
                s(l.isReturned()), s(l.getRenewals()), enc(l.getMemberEmail()), s(l.getReturnedAt())));
        for (ReservationRecord r : reservations) lines.add(String.join("\t", "R", s(r.getId()),
                s(r.getBookId()), enc(r.getMemberName()), s(r.getCreatedAt()), s(r.isFulfilled()), enc(r.getMemberEmail())));
        for (MemberAccount m : members) lines.add(String.join("\t", "M", enc(m.getName()), enc(m.getEmail()),
                enc(m.getSalt()), enc(m.getPasswordHash()), s(m.getCreatedAt())));
        for (SearchUsage usage : searchUsage) lines.add(String.join("\t", "U", enc(usage.getQuery()), s(usage.getSearchedAt())));
        try {
            Path parent = storePath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(storePath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save library data at " + storePath, exception);
        }
    }

    private static String enc(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String dec(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
    private static String s(Object value) { return String.valueOf(value); }

    private void loadSampleBooks() {
        books.add(new Book(1, "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science",
                "A comprehensive guide to algorithms and data structures.", "9780262046305", 2022, "CS · Aisle 2", 3));
        books.add(new Book(2, "Database System Concepts", "Abraham Silberschatz", "Database",
                "Fundamentals of database management systems.", "9780078022159", 2019, "CS · Aisle 4", 2));
        books.add(new Book(3, "Artificial Intelligence", "Stuart Russell", "Artificial Intelligence",
                "Introduction to artificial intelligence and intelligent agents.", "9780134610993", 2021, "CS · Aisle 5", 2));
    }
    private void loadSampleResearchPapers() {
        researchPapers.add(new ResearchPaper(101, "Machine Learning for Intelligent Systems",
                "John Smith, Priya Kumar", "Machine Learning",
                "This paper explores machine learning for intelligent systems and data analysis."));
        researchPapers.add(new ResearchPaper(102, "Efficient Graph Algorithms", "Rahul Sharma, Ananya Rao",
                "Algorithms", "This paper studies graph algorithms and network optimization."));
        researchPapers.add(new ResearchPaper(103, "Natural Language Processing with AI",
                "David Wilson, Sneha Patel", "Artificial Intelligence",
                "Research on natural language processing, text search, and artificial intelligence."));
    }

    public List<Book> getBooks() { return books; }
    public List<ResearchPaper> getResearchPapers() { return researchPapers; }
    public List<BorrowRecord> getBorrowRecords() { return borrowRecords; }
    public List<ReservationRecord> getReservations() { return reservations; }
    public List<MemberAccount> getMembers() { return members; }
    public List<SearchUsage> getSearchUsage() { return searchUsage; }
    public Path getStorePath() { return storePath; }
    public Book getBookById(int id) { return books.stream().filter(b -> b.getId() == id).findFirst().orElse(null); }
    public int nextBookId() { return books.stream().mapToInt(Book::getId).max().orElse(0) + 1; }
    public int nextLoanId() { return nextLoanId++; }
    public int nextReservationId() { return nextReservationId++; }
    public MemberAccount getMember(String email) { return members.stream().filter(m -> m.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null); }
    public void addMember(MemberAccount member) { members.add(member); save(); }
    public void addSearchUsage(SearchUsage usage) { searchUsage.add(usage); save(); }
    public void addBook(Book book) { if (book != null) { books.add(book); save(); } }
    public void addResearchPaper(ResearchPaper paper) { if (paper != null) { researchPapers.add(paper); save(); } }
    public void addBorrowRecord(BorrowRecord record) { borrowRecords.add(record); save(); }
    public void addReservation(ReservationRecord record) { reservations.add(record); save(); }
    public boolean removeBookById(int id) { boolean changed = books.removeIf(b -> b.getId() == id); if (changed) save(); return changed; }
    public boolean removeResearchPaperById(int id) { boolean changed = researchPapers.removeIf(p -> p.getId() == id); if (changed) save(); return changed; }
    public boolean removeItemById(int id) { return removeBookById(id) || removeResearchPaperById(id); }
}
