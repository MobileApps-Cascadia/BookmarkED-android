package edu.cascadia.bookmarked;

/**
 * Created by seanchung on 11/6/15.
 */
public class BookItem {
    protected String isbn;
    protected String title;
    protected String jsonString;

    public BookItem(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
    }

    public BookItem(String isbn, String title, String jsonString) {
        this(isbn, title);
        this.jsonString = jsonString;
    }

//    public BookItem(String isbn, String title, String author, String edition, String description) {
//        this(isbn, title);
//        this.author = author;
//        this.edition = edition;
//        this.description = description;
//    }

    @Override
    public String toString() {
        return title;
    }
}
