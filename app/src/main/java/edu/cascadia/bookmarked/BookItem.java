package edu.cascadia.bookmarked;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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
    protected String base64Picture;

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

            if (jsonObject.has("askingprice")) {
                NumberFormat numberFormat =
                        NumberFormat.getCurrencyInstance(Locale.US);
                askingPrice = numberFormat.format(jsonObject.getDouble("askingprice"));
            } else {
                askingPrice = "";
            }
            if (jsonObject.has("picture")) {
                base64Picture = jsonObject.getString("picture");
            } else {
                base64Picture = "";
            }
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
