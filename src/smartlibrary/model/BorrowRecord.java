package smartlibrary.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class BorrowRecord {
    private final int loanId;
    private final int bookId;
    private final String borrowerName;
    private final String memberEmail;
    private final long borrowedAt;
    private long dueAt;
    private long returnedAt;
    private boolean returned;
    private int renewals;

    public BorrowRecord(int bookId, String borrowerName) {
        this(0, bookId, borrowerName, System.currentTimeMillis(),
                Instant.now().plus(14, ChronoUnit.DAYS).toEpochMilli(), false, 0);
    }

    public BorrowRecord(int loanId, int bookId, String borrowerName, long borrowedAt,
                        long dueAt, boolean returned, int renewals) {
        this(loanId, bookId, borrowerName, "", borrowedAt, dueAt, returned, renewals);
    }

    public BorrowRecord(int loanId, int bookId, String borrowerName, String memberEmail,
                        long borrowedAt, long dueAt, boolean returned, int renewals) {
        this(loanId, bookId, borrowerName, memberEmail, borrowedAt, dueAt, returned, renewals, 0);
    }

    public BorrowRecord(int loanId, int bookId, String borrowerName, String memberEmail,
                        long borrowedAt, long dueAt, boolean returned, int renewals, long returnedAt) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.borrowerName = borrowerName == null ? "" : borrowerName.trim();
        this.memberEmail = memberEmail == null ? "" : memberEmail.trim().toLowerCase();
        this.borrowedAt = borrowedAt;
        this.dueAt = dueAt;
        this.returned = returned;
        this.renewals = renewals;
        this.returnedAt = returnedAt;
    }

    public int getLoanId() { return loanId; }
    public int getBookId() { return bookId; }
    public String getBorrowerName() { return borrowerName; }
    public String getMemberEmail() { return memberEmail; }
    public long getBorrowedAt() { return borrowedAt; }
    public long getDueAt() { return dueAt; }
    public long getReturnedAt() { return returnedAt; }
    public int getRenewals() { return renewals; }
    public boolean isReturned() { return returned; }
    public boolean isOverdue() { return !returned && dueAt < System.currentTimeMillis(); }
    public void markReturned() { markReturnedAt(System.currentTimeMillis()); }
    public void markReturnedAt(long returnedAt) { returned = true; this.returnedAt = returnedAt; }
    public boolean renew() {
        if (returned || isOverdue() || renewals >= 2) return false;
        dueAt = Instant.ofEpochMilli(dueAt).plus(14, ChronoUnit.DAYS).toEpochMilli();
        renewals++;
        return true;
    }

    @Override public String toString() {
        return "Book ID: " + bookId + "\nBorrower: " + borrowerName
                + "\nStatus: " + (returned ? "RETURNED" : isOverdue() ? "OVERDUE" : "BORROWED");
    }
}
