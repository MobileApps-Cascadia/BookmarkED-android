package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class BookWantedActivity extends AppCompatActivity {

    private final static String addABookURI = "bookmarked/book/addbook";
    private final static String addABookWantedURI = "bookmarked/book/addbookwanted";

    // Progress Dialog Object
    protected ProgressDialog prgDialog;

    private EditText isbnEditText;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText editionEditText;
    private EditText descEditText;
    private EditText commentEditText;

    protected String bookAction;
    private String userID;
    protected String jsonString;
    private String bookWantedID;

    private boolean newPosting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_wanted);
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

        bookAction = getIntent().getStringExtra("BookAction");
        jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));

        userID = getIntent().getStringExtra("UserID");

        initComponents();
        if (bookAction.equals("ViewExisting") || bookAction.equals("AllowEdit")) {
            setTitle("Detail book wanted");
            populateFields(jsonString);
            disableBookWantedControls();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utility.isNotNull(bookAction)) {
            if (bookAction.equals("AllowEdit")) {
                getMenuInflater().inflate(R.menu.menu_book_edit, menu);
            } else if (bookAction.equals("AddNew") || bookAction.equals("EditExisting")) {
                getMenuInflater().inflate(R.menu.menu_book, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_post_book) {
            addABookWanted();
        } else if (item.getItemId() == R.id.action_cancel) {
            // To do: add confirmation to cancel and loose data
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initComponents() {
        this.setTitle("Post book wanted");

        isbnEditText = (EditText) findViewById(R.id.bookIsbn);
        titleEditText = (EditText) findViewById(R.id.bookTitle);
        authorEditText = (EditText) findViewById(R.id.bookAuthor);
        editionEditText = (EditText) findViewById(R.id.bookEdition);
        descEditText = (EditText) findViewById(R.id.bookDescription);

        commentEditText = (EditText) findViewById(R.id.bookWantedComment);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        // hide the barcode button for now. Not sure if need it
        Button barcodeButton = (Button)findViewById(R.id.barcodeButton);
        barcodeButton.setVisibility(View.GONE);
    }

    // fill the fields only for read-only mode
    // data coming from the BookmarkEd web service
    protected void populateFields(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            bookWantedID = jsonObject.getString("id");

            isbnEditText.setText(jsonObject.getString("isbn"));
            titleEditText.setText(jsonObject.getString("title"));
            authorEditText.setText(jsonObject.getString("author"));
            editionEditText.setText(jsonObject.getString("edition"));
            descEditText.setText(jsonObject.getString("description"));

            commentEditText.setText(jsonObject.getString("comment"));

            disableControls();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // set controls for viewing mode
    private void disableControls() {
        // also set edit text to readonly
        isbnEditText.setEnabled(false);
        titleEditText.setEnabled(false);
        authorEditText.setEnabled(false);
        editionEditText.setEnabled(false);
        descEditText.setEnabled(false);

        // don't let the controls to be focusable,
        // so keyboard won't show up
        isbnEditText.setFocusable(false);
        titleEditText.setFocusable(false);
        authorEditText.setFocusable(false);
        editionEditText.setFocusable(false);
        descEditText.setFocusable(false);
    }

    private void disableBookWantedControls() {
        commentEditText.setEnabled(false);

        commentEditText.setFocusable(false);
    }

    private void addABookWanted() {

        // verify that userID exist
        if (!Utility.isNotNull(userID)) {
            Utility.beep();
            Toast.makeText(this, "User name is unknown. Cannot add book for sale", Toast.LENGTH_SHORT).show();
            return;
        }
        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String description = descEditText.getText().toString();

        RequestParams params = new RequestParams();

        // at the minimum book title cannot be empty
        if (Utility.isNotNull(title)) {
            params.put("isbn", isbn);
            params.put("title", title);
            params.put("author", author);
            params.put("edition", edition);
            params.put("description", description);
            // Invoke RESTful Web Service with Http parameters
            requestAddBook(params);
        } else {
            Utility.beep();
            Toast.makeText(this, "Book title cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestAddBookWanted() {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("isbn", isbnEditText.getText().toString());
        params.put("username", userID);
        params.put("comment", commentEditText.getText().toString());

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + addABookWantedURI, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        // Display book for sale successfully posted using Toast
                        Toast.makeText(getApplicationContext(), "Book wanted was successfully posted!", Toast.LENGTH_SHORT).show();
                        newPosting = true;
                        finish();
                    }
                    // Else display error message
                    else {
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }

            }
        });
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * This is a 2-step process. First add the book to the table
     * then add the book for sale table
     *
     * @param params
     */
    private void requestAddBook(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + addABookURI, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        // now add the entry to the book for sale table
                        requestAddBookWanted();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
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

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("NewPosting", newPosting);

        setResult(RESULT_OK, data);

        super.finish();
    }

}
