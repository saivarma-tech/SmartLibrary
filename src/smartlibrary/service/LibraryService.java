package smartlibrary.service;

import java.util.Scanner;

import smartlibrary.data.LibraryData;
import smartlibrary.model.Book;
import smartlibrary.model.ResearchPaper;

public class LibraryService {

    private LibraryData library;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public LibraryService(LibraryData library) {

        this.library = library;
    }


    // ==========================================
    // DISPLAY ALL BOOKS
    // ==========================================

    public void displayBooks() {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "           ALL BOOKS"
        );

        System.out.println(
                "================================="
        );


        if (library.getBooks().isEmpty()) {

            System.out.println();

            System.out.println(
                    "No books available."
            );

            return;
        }


        int number = 1;


        for (Book book : library.getBooks()) {

            System.out.println();

            System.out.println(
                    "BOOK " + number
            );

            System.out.println(
                    "---------------------------------"
            );

            System.out.println(
                    "Book ID: " +
                    book.getId()
            );

            System.out.println(
                    "Title: " +
                    book.getTitle()
            );

            System.out.println(
                    "Author: " +
                    book.getAuthor()
            );

            System.out.println(
                    "Category: " +
                    book.getCategory()
            );

            System.out.println(
                    "Description: " +
                    book.getDescription()
            );


            number++;
        }


        System.out.println();

        System.out.println(
                "================================="
        );
    }


    // ==========================================
    // DISPLAY ALL RESEARCH PAPERS
    // ==========================================

    public void displayResearchPapers() {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "       RESEARCH PAPERS"
        );

        System.out.println(
                "================================="
        );


        if (library.getResearchPapers().isEmpty()) {

            System.out.println();

            System.out.println(
                    "No research papers available."
            );

            return;
        }


        int number = 1;


        for (ResearchPaper paper :
                library.getResearchPapers()) {

            System.out.println();

            System.out.println(
                    "PAPER " + number
            );

            System.out.println(
                    "---------------------------------"
            );

            System.out.println(
                    "Paper ID: " +
                    paper.getId()
            );

            System.out.println(
                    "Title: " +
                    paper.getTitle()
            );

            System.out.println(
                    "Authors: " +
                    paper.getAuthors()
            );

            System.out.println(
                    "Category: " +
                    paper.getCategory()
            );

            System.out.println(
                    "Abstract: " +
                    paper.getAbstractText()
            );


            number++;
        }


        System.out.println();

        System.out.println(
                "================================="
        );
    }


    // ==========================================
    // SEARCH BY CATEGORY
    // ==========================================

    public void searchByCategory(String category) {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "       CATEGORY RESULTS"
        );

        System.out.println(
                "================================="
        );


        if (category == null ||
            category.trim().isEmpty()) {

            System.out.println();

            System.out.println(
                    "Category cannot be empty."
            );

            System.out.println();

            System.out.println(
                    "================================="
            );

            return;
        }


        category = category.trim();


        System.out.println();

        System.out.println(
                "Category: " + category
        );


        int count = 0;


        // ======================================
        // SEARCH BOOKS
        // ======================================

        for (Book book : library.getBooks()) {

            if (book.getCategory() != null &&
                book.getCategory()
                    .equalsIgnoreCase(category)) {

                count++;

                System.out.println();

                System.out.println(
                        "BOOK " + count
                );

                System.out.println(
                        "---------------------------------"
                );

                System.out.println(
                        "Book ID: " +
                        book.getId()
                );

                System.out.println(
                        "Title: " +
                        book.getTitle()
                );

                System.out.println(
                        "Author: " +
                        book.getAuthor()
                );

                System.out.println(
                        "Category: " +
                        book.getCategory()
                );
            }
        }


        // ======================================
        // SEARCH RESEARCH PAPERS
        // ======================================

        for (ResearchPaper paper :
                library.getResearchPapers()) {

            if (paper.getCategory() != null &&
                paper.getCategory()
                    .equalsIgnoreCase(category)) {

                count++;

                System.out.println();

                System.out.println(
                        "RESEARCH PAPER " + count
                );

                System.out.println(
                        "---------------------------------"
                );

                System.out.println(
                        "Paper ID: " +
                        paper.getId()
                );

                System.out.println(
                        "Title: " +
                        paper.getTitle()
                );

                System.out.println(
                        "Authors: " +
                        paper.getAuthors()
                );

                System.out.println(
                        "Category: " +
                        paper.getCategory()
                );
            }
        }


        // ======================================
        // DISPLAY RESULT COUNT
        // ======================================

        System.out.println();

        if (count == 0) {

            System.out.println(
                    "No items found in this category."
            );

        } else {

            System.out.println(
                    "Found " +
                    count +
                    " item(s)."
            );
        }


        System.out.println();

        System.out.println(
                "================================="
        );
    }


    // ==========================================
    // VIEW ITEM BY ID
    // ==========================================

    public void displayItemById(int id) {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "          ITEM DETAILS"
        );

        System.out.println(
                "================================="
        );


        // ======================================
        // SEARCH BOOKS
        // ======================================

        for (Book book : library.getBooks()) {

            if (book.getId() == id) {

                System.out.println();

                System.out.println(
                        "Type: BOOK"
                );

                System.out.println(
                        "---------------------------------"
                );

                System.out.println(
                        "Book ID: " +
                        book.getId()
                );

                System.out.println(
                        "Title: " +
                        book.getTitle()
                );

                System.out.println(
                        "Author: " +
                        book.getAuthor()
                );

                System.out.println(
                        "Category: " +
                        book.getCategory()
                );

                System.out.println(
                        "Description: " +
                        book.getDescription()
                );

                System.out.println();

                System.out.println(
                        "================================="
                );

                return;
            }
        }


        // ======================================
        // SEARCH RESEARCH PAPERS
        // ======================================

        for (ResearchPaper paper :
                library.getResearchPapers()) {

            if (paper.getId() == id) {

                System.out.println();

                System.out.println(
                        "Type: RESEARCH PAPER"
                );

                System.out.println(
                        "---------------------------------"
                );

                System.out.println(
                        "Paper ID: " +
                        paper.getId()
                );

                System.out.println(
                        "Title: " +
                        paper.getTitle()
                );

                System.out.println(
                        "Authors: " +
                        paper.getAuthors()
                );

                System.out.println(
                        "Category: " +
                        paper.getCategory()
                );

                System.out.println(
                        "Abstract: " +
                        paper.getAbstractText()
                );

                System.out.println();

                System.out.println(
                        "================================="
                );

                return;
            }
        }


        // ======================================
        // ID NOT FOUND
        // ======================================

        System.out.println();

        System.out.println(
                "No book or research paper found"
        );

        System.out.println(
                "with ID: " + id
        );

        System.out.println();

        System.out.println(
                "================================="
        );
    }


    // ==========================================
    // ADD BOOK
    // ==========================================

    public void addBook(Scanner sc) {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "           ADD BOOK"
        );

        System.out.println(
                "================================="
        );


        try {

            System.out.println();

            System.out.print(
                    "Enter Book ID: "
            );

            int id = Integer.parseInt(
                    sc.nextLine().trim()
            );


            System.out.print(
                    "Enter Title: "
            );

            String title = sc.nextLine();


            System.out.print(
                    "Enter Author: "
            );

            String author = sc.nextLine();


            System.out.print(
                    "Enter Category: "
            );

            String category = sc.nextLine();


            System.out.print(
                    "Enter Description: "
            );

            String description = sc.nextLine();


            Book book = new Book(
                    id,
                    title,
                    author,
                    category,
                    description
            );


            library.addBook(book);


            System.out.println();

            System.out.println(
                    "Book added successfully!"
            );

            System.out.println(
                    "Book ID: " + id
            );

        } catch (NumberFormatException e) {

            System.out.println();

            System.out.println(
                    "Invalid Book ID."
            );
        }


        System.out.println();

        System.out.println(
                "================================="
        );
    }


    // ==========================================
    // ADD RESEARCH PAPER
    // ==========================================

    public void addResearchPaper(Scanner sc) {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "       ADD RESEARCH PAPER"
        );

        System.out.println(
                "================================="
        );


        try {

            System.out.println();

            System.out.print(
                    "Enter Paper ID: "
            );

            int id = Integer.parseInt(
                    sc.nextLine().trim()
            );


            System.out.print(
                    "Enter Title: "
            );

            String title = sc.nextLine();


            System.out.print(
                    "Enter Authors: "
            );

            String authors = sc.nextLine();


            System.out.print(
                    "Enter Category: "
            );

            String category = sc.nextLine();


            System.out.print(
                    "Enter Abstract: "
            );

            String abstractText = sc.nextLine();


            ResearchPaper paper =
                    new ResearchPaper(
                            id,
                            title,
                            authors,
                            category,
                            abstractText
                    );


            library.addResearchPaper(paper);


            System.out.println();

            System.out.println(
                    "Research paper added successfully!"
            );

            System.out.println(
                    "Paper ID: " + id
            );

        } catch (NumberFormatException e) {

            System.out.println();

            System.out.println(
                    "Invalid Paper ID."
            );
        }


        System.out.println();

        System.out.println(
                "================================="
        );
    }


    // ==========================================
    // REMOVE ITEM BY ID
    // ==========================================

    public void removeItemById(Scanner sc) {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "          REMOVE ITEM"
        );

        System.out.println(
                "================================="
        );


        try {

            System.out.println();

            System.out.print(
                    "Enter Item ID: "
            );

            int id = Integer.parseInt(
                    sc.nextLine().trim()
            );


            boolean removed =
                    library.removeItemById(id);


            System.out.println();

            if (removed) {

                System.out.println(
                        "Item removed successfully."
                );

                System.out.println(
                        "Removed Item ID: " + id
                );

            } else {

                System.out.println(
                        "No item found with ID: " + id
                );
            }

        } catch (NumberFormatException e) {

            System.out.println();

            System.out.println(
                    "Invalid ID."
            );
        }


        
        System.out.println();

        System.out.println(
                "================================="
        );
    }
}