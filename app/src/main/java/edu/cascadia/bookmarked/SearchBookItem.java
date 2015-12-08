package edu.cascadia.bookmarked;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by seanchung on 12/4/15.
 */
public class SearchBookItem {
    protected String isbn;
    protected String title;
    protected String author;
    protected String publisher;
    protected String edition;
    protected String description;

    public SearchBookItem() {
        super();
    }

    public SearchBookItem(String isbn, String title, String author, String publisher, String edition, String description) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.edition = edition;
        this.description = description;
    }

    @Override
    public String toString() {
        return title;
    }


}
