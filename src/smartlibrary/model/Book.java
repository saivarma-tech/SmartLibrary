package smartlibrary.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private String description;
    private String isbn;
    private int publicationYear;
    private String location;
    private int totalCopies;

    public Book(int id, String title, String author, String category, String description) {
        this(id, title, author, category, description, "", 0, "Main Library", 1);
    }

    public Book(int id, String title, String author, String category, String description,
                String isbn, int publicationYear, String location, int totalCopies) {
        this.id = id;
        this.title = clean(title);
        this.author = clean(author);
        this.category = clean(category);
        this.description = clean(description);
        this.isbn = clean(isbn);
        this.publicationYear = publicationYear;
        this.location = clean(location);
        this.totalCopies = Math.max(1, totalCopies);
    }

    private static String clean(String value) { return value == null ? "" : value.trim(); }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getIsbn() { return isbn; }
    public int getPublicationYear() { return publicationYear; }
    public String getLocation() { return location; }
    public int getTotalCopies() { return totalCopies; }

    public void update(String title, String author, String category, String description,
                       String isbn, int publicationYear, String location, int totalCopies) {
        this.title = clean(title);
        this.author = clean(author);
        this.category = clean(category);
        this.description = clean(description);
        this.isbn = clean(isbn);
        this.publicationYear = publicationYear;
        this.location = clean(location);
        this.totalCopies = Math.max(1, totalCopies);
    }

    @Override public String toString() {
        return "Book ID: " + id + "\nTitle: " + title + "\nAuthor: " + author
                + "\nCategory: " + category + "\nDescription: " + description;
    }
}
