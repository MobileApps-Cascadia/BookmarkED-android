package edu.cascadia.bookmarked;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rattanak on 11/17/2015.
 */
public class BooksAdapter extends ArrayAdapter<BookItem> {
    public BooksAdapter(Context context, ArrayList<BookItem> booksList) {
        super(context, 0, booksList);
        //super(context, 0, booksList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BookItem bookItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.book_list_item_row3, parent, false);
        }
        // Lookup view for data population
        TextView bookTitle = (TextView) convertView.findViewById(R.id.bookTitleTextView);
        TextView bookPrice = (TextView) convertView.findViewById(R.id.bookPriceTextView);
        ImageView bookImageView = (ImageView) convertView.findViewById(R.id.bookCoverimageView);
        // Populate the data into the template view using the data object
        bookTitle.setText(bookItem.title);
        TextView bookAuthor = (TextView) convertView.findViewById(R.id.bookAuthorTextView);
        bookAuthor.setText(bookItem.author);

        if (Utility.isNotNull(bookItem.askingPrice)) {
            bookPrice.setText(bookItem.askingPrice);
        }
        if (bookItem.base64Picture != null && bookItem.base64Picture.trim().length() > 0) {
            try {
                byte[] decodedString = Base64.decode(bookItem.base64Picture, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                System.out.println("Bitmap width:" + bitmap.getWidth() + " height:" + bitmap.getHeight());

                bookImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                System.out.println("Exception in getting image. " + e.getMessage());
            }
        }

        //tvHome.setText(user.hometown);
        // Return the completed view to render on screen
        return convertView;
    }
}
