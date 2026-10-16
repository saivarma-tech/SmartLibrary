package smartlibrary.model;

public class ResearchPaper {

    private int id;
    private String title;
    private String authors;
    private String category;
    private String abstractText;

    public ResearchPaper(int id, String title, String authors,
                         String category, String abstractText) {

        this.id = id;
        this.title = title;
        this.authors = authors;
        this.category = category;
        this.abstractText = abstractText;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getCategory() {
        return category;
    }

    public String getAbstractText() {
        return abstractText;
    }

    @Override
    public String toString() {

        return "Paper ID: " + id +
               "\nTitle: " + title +
               "\nAuthors: " + authors +
               "\nCategory: " + category +
               "\nAbstract: " + abstractText;
    }
}