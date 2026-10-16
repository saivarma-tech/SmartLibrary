package smartlibrary.model;

public class ReservationRecord {
    private final int id;
    private final int bookId;
    private final String memberName;
    private final String memberEmail;
    private final long createdAt;
    private boolean fulfilled;

    public ReservationRecord(int id, int bookId, String memberName, long createdAt, boolean fulfilled) {
        this(id, bookId, memberName, "", createdAt, fulfilled);
    }
    public ReservationRecord(int id, int bookId, String memberName, String memberEmail, long createdAt, boolean fulfilled) {
        this.id = id;
        this.bookId = bookId;
        this.memberName = memberName == null ? "" : memberName.trim();
        this.memberEmail = memberEmail == null ? "" : memberEmail.trim().toLowerCase();
        this.createdAt = createdAt;
        this.fulfilled = fulfilled;
    }
    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public String getMemberName() { return memberName; }
    public String getMemberEmail() { return memberEmail; }
    public long getCreatedAt() { return createdAt; }
    public boolean isFulfilled() { return fulfilled; }
    public void setFulfilled(boolean value) { fulfilled = value; }
}
