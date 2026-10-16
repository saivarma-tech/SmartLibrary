package smartlibrary.service;

import smartlibrary.data.LibraryData;

public class DashboardService {

    private LibraryData libraryData;
    private BorrowService borrowService;

    public DashboardService(
            LibraryData libraryData,
            BorrowService borrowService) {

        this.libraryData = libraryData;
        this.borrowService = borrowService;
    }

    public void displayDashboard() {

        int totalBooks = libraryData.getBooks().size();

        int totalResearchPapers =
                libraryData.getResearchPapers().size();

        int totalItems =
                totalBooks + totalResearchPapers;

        int borrowedBooks =
                borrowService.getActiveBorrowCount();

        int availableBooks =
                totalBooks - borrowedBooks;

        int totalBorrowRecords =
                borrowService.getTotalBorrowRecords();

        System.out.println();
        System.out.println("======================================");
        System.out.println("         SMART LIBRARY DASHBOARD      ");
        System.out.println("======================================");

        System.out.println("Total Books           : " + totalBooks);

        System.out.println(
                "Total Research Papers: " + totalResearchPapers);

        System.out.println("Total Library Items   : " + totalItems);

        System.out.println("Borrowed Books        : " + borrowedBooks);

        System.out.println("Available Books       : " + availableBooks);

        System.out.println(
                "Borrowing Records     : " + totalBorrowRecords);

        System.out.println("======================================");
        System.out.println();
    }
}