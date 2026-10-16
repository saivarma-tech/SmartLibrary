package smartlibrary.data;

import java.nio.file.Files;
import java.nio.file.Path;

import smartlibrary.model.Book;
import smartlibrary.model.BorrowRecord;
import smartlibrary.model.MemberAccount;
import smartlibrary.model.ReservationRecord;
import smartlibrary.model.SearchUsage;

/** Dependency-free persistence regression check; run as a Java application. */
public final class LibraryDataPersistenceTest {
    private LibraryDataPersistenceTest() { }
    public static void main(String[] args) throws Exception {
        Path folder = Files.createTempDirectory("smartlibrary-persistence-test");
        String previous = System.getProperty("smartlibrary.data");
        try {
            System.setProperty("smartlibrary.data", folder.resolve("library.tsv").toString());
            LibraryData first = new LibraryData();
            int bookId = first.nextBookId();
            first.addBook(new Book(bookId, "Persistence Check", "Test Author", "Testing",
                    "Persists across restarts", "9780000000000", 2025, "Test shelf", 2));
            first.addMember(new MemberAccount("Test Member", "persistence@example.test", "salt", "hash", 1234));
            BorrowRecord loan = new BorrowRecord(first.nextLoanId(), bookId, "Test Member",
                    "persistence@example.test", 1000, 2000, false, 1);
            loan.markReturnedAt(3000);
            first.addBorrowRecord(loan);
            first.addReservation(new ReservationRecord(first.nextReservationId(), bookId,
                    "Test Member", "persistence@example.test", 4000, true));
            first.addSearchUsage(new SearchUsage("persistence test", 5000));

            LibraryData restored = new LibraryData();
            check(restored.getBookById(bookId) != null, "book persists");
            check(restored.getBookById(bookId).getIsbn().equals("9780000000000"), "book fields persist");
            check(restored.getMember("persistence@example.test") != null, "member persists");
            check(restored.getBorrowRecords().get(0).getReturnedAt() == 3000, "return date persists");
            check(restored.getBorrowRecords().get(0).getMemberEmail().equals("persistence@example.test"), "loan owner persists");
            check(restored.getReservations().get(0).isFulfilled(), "reservation state persists");
            check(restored.getReservations().get(0).getMemberEmail().equals("persistence@example.test"), "reservation owner persists");
            check(restored.getSearchUsage().get(0).getQuery().equals("persistence test"), "search analytics persist");
            System.out.println("LibraryDataPersistenceTest: PASS");
        } finally {
            if (previous == null) System.clearProperty("smartlibrary.data");
            else System.setProperty("smartlibrary.data", previous);
            try (var files = Files.walk(folder)) {
                files.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        }
    }
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
