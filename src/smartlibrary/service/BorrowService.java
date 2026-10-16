package smartlibrary.service;

import java.util.List;
import java.util.Scanner;

import smartlibrary.data.LibraryData;
import smartlibrary.model.Book;
import smartlibrary.model.BorrowRecord;

public class BorrowService {

    private LibraryData libraryData;

    private List<BorrowRecord> borrowRecords;

    public BorrowService(LibraryData libraryData) {
        this.libraryData = libraryData;
        this.borrowRecords = libraryData.getBorrowRecords();
    }

    // Borrow a book
    public void borrowBook(Scanner scanner) {

        System.out.println();
        System.out.println("========== BORROW BOOK ==========");

        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();

        Book selectedBook = null;

        for (Book book : libraryData.getBooks()) {

            if (book.getId() == bookId) {
                selectedBook = book;
                break;
            }
        }

        if (selectedBook == null) {
            System.out.println("Book not found.");
            return;
        }

        for (BorrowRecord record : borrowRecords) {

            if (record.getBookId() == bookId
                    && !record.isReturned()) {

                System.out.println(
                        "This book is already borrowed.");

                return;
            }
        }

        System.out.print("Enter borrower name: ");
        String borrowerName = scanner.nextLine();

        long now = System.currentTimeMillis();
        BorrowRecord record = new BorrowRecord(libraryData.nextLoanId(), bookId, borrowerName,
                now, now + 14L * 24 * 60 * 60 * 1000, false, 0);

        libraryData.addBorrowRecord(record);

        System.out.println();
        System.out.println("Book borrowed successfully.");
        System.out.println("Book Title: " + selectedBook.getTitle());
        System.out.println("Borrower: " + borrowerName);
    }

    // Return a book
    public void returnBook(Scanner scanner) {

        System.out.println();
        System.out.println("========== RETURN BOOK ==========");

        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();

        for (BorrowRecord record : borrowRecords) {

            if (record.getBookId() == bookId
                    && !record.isReturned()) {

                record.markReturned();
                libraryData.save();

                System.out.println();
                System.out.println("Book returned successfully.");
                System.out.println("Book ID: " + bookId);

                return;
            }
        }

        System.out.println(
                "No active borrowing record found for this book.");
    }

    // Display currently borrowed books
    public void displayBorrowedBooks() {

        System.out.println();
        System.out.println("========== BORROWED BOOKS ==========");

        boolean found = false;

        for (BorrowRecord record : borrowRecords) {

            if (!record.isReturned()) {

                found = true;

                System.out.println(
                        "Book ID: " + record.getBookId());

                System.out.println(
                        "Borrower: "
                                + record.getBorrowerName());

                System.out.println("Status: BORROWED");

                System.out.println("-----------------------------------");
            }
        }

        if (!found) {
            System.out.println("No books are currently borrowed.");
        }
    }

    // Display complete borrowing history
    public void displayBorrowHistory() {

        System.out.println();
        System.out.println("========== BORROWING HISTORY ==========");

        if (borrowRecords.isEmpty()) {
            System.out.println("No borrowing records found.");
            return;
        }

        for (BorrowRecord record : borrowRecords) {

            System.out.println(record);

            System.out.println("-----------------------------------");
        }
    }

    // Return the number of currently borrowed books
    public int getActiveBorrowCount() {

        int count = 0;

        for (BorrowRecord record : borrowRecords) {

            if (!record.isReturned()) {
                count++;
            }
        }

        return count;
    }

    // Return the total number of borrowing records
    public int getTotalBorrowRecords() {

        return borrowRecords.size();
    }
}
