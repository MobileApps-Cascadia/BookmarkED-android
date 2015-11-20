package edu.cascadia.bookmarked;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by seanchung on 11/6/15.
 */
public class BookItem {
    protected String isbn;
    protected String title;
    protected String jsonString;
    protected String author;
    protected String askingPrice;

    public BookItem(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
    }

    public BookItem(String isbn, String title, String jsonString) {
        this(isbn, title);
        this.jsonString = jsonString;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            author = jsonObject.getString("author");

            NumberFormat numberFormat =
                    NumberFormat.getCurrencyInstance(Locale.US);
            askingPrice = numberFormat.format(jsonObject.getDouble("askingprice"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
