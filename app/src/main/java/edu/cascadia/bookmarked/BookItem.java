package edu.cascadia.bookmarked;

/**
 * Created by seanchung on 11/6/15.
 */
public class BookItem {
    public String isbn;
    public String title;

    public BookItem(String id, String title) {
        this.isbn = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
