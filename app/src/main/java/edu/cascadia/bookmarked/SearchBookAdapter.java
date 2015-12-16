package edu.cascadia.bookmarked;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seanchung on 12/4/15.
 */
public class SearchBookAdapter extends ArrayAdapter<SearchBookItem> {

    private SearchBookActivity searchBookActivity;

    public SearchBookAdapter(Context context, ArrayList<SearchBookItem> booksList) {
        super(context, 0, booksList);
        searchBookActivity = (SearchBookActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final SearchBookItem searchBookItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_book_item, parent, false);
        }
        // Lookup view for data population
        TextView bookTitle = (TextView) convertView.findViewById(R.id.bookTitleTextView);
        // Populate the data into the template view using the data object
        bookTitle.setText(searchBookItem.title);

        TextView isbnTextView = (TextView) convertView.findViewById(R.id.ISBNTextView);
        isbnTextView.setText(searchBookItem.isbn);

        TextView editionTextView = (TextView) convertView.findViewById(R.id.editionTextView);
        editionTextView.setText(searchBookItem.edition);

        TextView bookAuthorTextView = (TextView) convertView.findViewById(R.id.bookAuthorTextView);
        bookAuthorTextView.setText(searchBookItem.author);

        TextView publisherTextView = (TextView) convertView.findViewById(R.id.publisherTextView);
        publisherTextView.setText(searchBookItem.publisher);

        Button selectButton = (Button) convertView.findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("book with isbn:" + searchBookItem.isbn + " was selected");
                searchBookActivity.onBookSelected(searchBookItem);
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}
