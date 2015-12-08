package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class SearchBookActivity extends AppCompatActivity {

    private final static String ISBNDB_URI = "http://isbndb.com/api/v2/json/WQ3AZBWL/book/";
    private final static String TITLE_SEARCH_URI = "http://isbndb.com/api/v2/json/WQ3AZBWL/books/?q=";

    private EditText searchEditText;
    private RadioButton titleRadioButton;
    //private RadioButton isbnRadioButton;
    private ListView bookListView;

    private SearchBookAdapter searchBookAdapter;

    private SearchBookListItem searchBookListItem;

    // Progress Dialog Object
    protected ProgressDialog prgDialog;

    SearchBookItem selectedBook = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        searchEditText = (EditText) findViewById(R.id.searchEditText);
        titleRadioButton = (RadioButton) findViewById(R.id.titleRadioButton);
        //isbnRadioButton = (RadioButton) findViewById(R.id.isbnRadioButton);
        bookListView = (ListView) findViewById(R.id.bookListView);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        // load data and call web service if first time get here
        if (searchBookAdapter == null) {
            searchBookListItem = new SearchBookListItem();

            searchBookAdapter = new SearchBookAdapter(this, (ArrayList<SearchBookItem>) searchBookListItem.ITEMS);

            bookListView.setAdapter(searchBookAdapter);
        }
    }

    public void onSearchGoClicked(View view) {

        String searchStr = searchEditText.getText().toString();

        if (searchStr.length() == 0) {
            Utility.beep();
            Toast.makeText(this, "Please enter title or ISBN to search", Toast.LENGTH_SHORT).show();
            return;
        }

        searchBookAdapter.clear();

        if (titleRadioButton.isChecked()) {
            searchByTitle(searchStr.trim());
        } else {
            searchByISBN(searchStr);
        }
    }

    private void searchByTitle(String title) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();


        String hostAddress = TITLE_SEARCH_URI + title;
        client.get(hostAddress, new RequestParams(), new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    JSONObject obj = new JSONObject(response);
                    System.out.println("Response:" + response);

                    // When the JSON response has data, it has the book information
                    if (obj.getString("data").length() > 0) {
                        // Display book successfully added message using Toast
                        Toast.makeText(getApplicationContext(), "Book found", Toast.LENGTH_SHORT).show();
                        // System.out.println("data:" + obj.getString("data"));

                        // get actual book info, which is stored as an array
                        JSONArray jsonBook = obj.getJSONArray("data");
                        try {
                            searchBookAdapter.clear();
                            getArray(jsonBook);
                            searchBookAdapter.notifyDataSetChanged();
                        } catch (ParseException pe) {
                            System.out.println("ParseException: " + pe.getMessage());
                            pe.printStackTrace();
                        }

                    }
                    // Else display error message
                    else {
                        // error is found in the response
                        Toast.makeText(getApplicationContext(), obj.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_404_error), Toast.LENGTH_SHORT).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_500_error), Toast.LENGTH_SHORT).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchByISBN(String isbn) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();


        String hostAddress = ISBNDB_URI + isbn;
        client.get(hostAddress, new RequestParams(), new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    JSONObject obj = new JSONObject(response);
                    System.out.println("Response:" + response);

                    // When the JSON response has data, it has the book information
                    if (obj.getString("data").length() > 0) {
                        // Display book successfully added message using Toast
                        Toast.makeText(getApplicationContext(), "Book found", Toast.LENGTH_SHORT).show();
                        // System.out.println("data:" + obj.getString("data"));

                        // get actual book info, which is stored as an array
                        JSONArray jsonBook = obj.getJSONArray("data");
                        // now populate the fields related to the book info
                        //populateBookFields(jsonBook.getJSONObject(0));
                        searchBookAdapter.clear();
                        try {
                            addBookToAdapter(jsonBook.getJSONObject(0));
                            searchBookAdapter.notifyDataSetChanged();
                        } catch (ParseException pe) {
                            System.out.println("ParseException: " + pe.getMessage());
                            pe.printStackTrace();
                        }
                    }
                    // Else display error message
                    else {
                        // error is found in the response
                        Toast.makeText(getApplicationContext(), obj.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_404_error), Toast.LENGTH_SHORT).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_500_error), Toast.LENGTH_SHORT).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getArray(JSONArray jsonArr) throws ParseException {

        try {
            for (int k = 0; k < jsonArr.length(); k++) {

                try {
                    if (jsonArr.getJSONObject(k) instanceof JSONObject) {
                        //System.out.println("BOOK " + k + ":");
                        addBookToAdapter(jsonArr.getJSONObject(k));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception ex) {
            System.out.println("Exception in getArray:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addBookToAdapter(JSONObject jsonObject) throws ParseException {

        try {
            String isbn = jsonObject.getString("isbn13");
            String title = jsonObject.getString("title");
            //String author = jsonObject.getString("author_data");
            JSONArray jsonAuthors = jsonObject.getJSONArray("author_data");
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < jsonAuthors.length(); i++) {
                stringBuffer.append(((JSONObject) jsonAuthors.get(i)).getString("name") + ", ");
            }
            if (stringBuffer.length() > 2) {
                // delete last 2 char (comma and space);
                // it is possible that the book has no author, like dictionary
                int lastComma = stringBuffer.lastIndexOf(", ");
                stringBuffer.delete(lastComma, lastComma + 1);
            }

            String author = stringBuffer.toString();
            String publisher = jsonObject.getString("publisher_name");
            String edition = jsonObject.getString("edition_info");
            String description = jsonObject.getString("summary");

            SearchBookItem bookItem = new SearchBookItem(isbn, title, author, publisher, edition, description);

            searchBookAdapter.add(bookItem);
        } catch (JSONException e) {
            System.out.println("Exception in addBookToAdapter. e:" + e.getMessage());
            e.printStackTrace();
        }

    }

    public void onBookSelected(SearchBookItem searchBookItem) {
        selectedBook = searchBookItem;
        finish();
    }

    @Override
    public void finish() {
        if (selectedBook != null) {
            Intent data = new Intent();
            data. putExtra("isbn", selectedBook.isbn);
            data. putExtra("title", selectedBook.title);
            data. putExtra("author", selectedBook.author);
            data. putExtra("edition", selectedBook.edition);
            data. putExtra("publisher", selectedBook.publisher);
            data. putExtra("summary", selectedBook.description);

            setResult(RESULT_OK, data);
        }

        super.finish();
    }

}
