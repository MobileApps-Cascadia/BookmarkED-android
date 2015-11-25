package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class BookWantedActivity extends AppCompatActivity {

    private final static int EDIT_REQUEST_CODE = 2;

    private final static String addABookURI = "bookmarked/book/addbook";
    private final static String addABookWantedURI = "bookmarked/book/addbookwanted";
    private final static String updateBookWantedURI = "bookmarked/book/updatebookwanted";
    private final static String deleteBookWantedURI = "bookmarked/book/deletebookwanted";

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
    private String bookID;
    private String bookWantedID;

    private boolean needsUpdating = false;

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

        bookAction = getIntent().getStringExtra(getString(R.string.book_action_param));
        jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));

        userID = getIntent().getStringExtra(getString(R.string.user_id_param));

        initComponents();

        if (bookAction.equals("EditExisting")) {
            setTitle(getString(R.string.title_detail_book_wanted));
            populateFields(jsonString, false);
        } else if (bookAction.equals("ViewExisting") || bookAction.equals("AllowEdit")) {
            setTitle(getString(R.string.title_detail_book_wanted));
            populateFields(jsonString, true);
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
        switch (item.getItemId()) {
            case R.id.action_save_post_book:
                if (bookAction.equals("EditExisting")) {
                    saveBookWantedChanges();
                } else {
                    addABookWanted();
                }
                return true;

            case R.id.action_cancel:
                super.onBackPressed();
                return true;

            case R.id.action_edit_posted_book:
                editABookWanted();
                return true;

            case R.id.action_delete_posted_book:
                deleteABookWanted();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void initComponents() {
        setTitle(getString(R.string.title_post_book_wanted));

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

    protected void populateFields(String jsonString, boolean readonly) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            bookWantedID = jsonObject.getString("id");
            bookID = jsonObject.getString("book_id");

            isbnEditText.setText(jsonObject.getString("isbn"));
            titleEditText.setText(jsonObject.getString("title"));
            authorEditText.setText(jsonObject.getString("author"));
            editionEditText.setText(jsonObject.getString("edition"));
            descEditText.setText(jsonObject.getString("description"));

            commentEditText.setText(jsonObject.getString("comment"));

            if (readonly) disableControls();

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
            Toast.makeText(this, "User name is unknown. Cannot add book wanted", Toast.LENGTH_SHORT).show();
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

    private void editABookWanted() {

        Intent editIntent = new Intent(this, BookWantedActivity.class);
        editIntent.putExtra(getString(R.string.book_action_param), "EditExisting");
        editIntent.putExtra(getString(R.string.book_info_param), jsonString);
        editIntent.putExtra(getString(R.string.user_id_param), userID);

        startActivityForResult(editIntent, EDIT_REQUEST_CODE);

    }

    private void deleteABookWanted() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.title_delete_book_posting_reason));

        // set dialog message
        final String[] reasons = getResources().getStringArray(R.array.delete_book_wanted_reasons);

        final int[] selectedItem = new int[1];
        selectedItem[0] = -1;
        alertDialogBuilder.setSingleChoiceItems(reasons, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedItem[0] = i;
            }
        });

        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton("Yes", null);

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // if this button is clicked, just close
                // the dialog box and do nothing
                dialog.cancel();
            }
        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (selectedItem[0] >= 0) {
                            //Toast.makeText(getApplicationContext(), "Item selected", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            // status in the table starts 1 as being active
                            doDeleteBookWanted(selectedItem[0] + 2);
                        } else {
                            Utility.beep();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_delete_reason), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        // show it
        alertDialog.show();
    }

    private void requestDeleteBookWanted(int status) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("id", bookWantedID);
        params.put("status", status + "");

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + deleteBookWantedURI, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    System.out.println("***Deletion successful");
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        // Display book for sale successfully posted using Toast
                        //Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_book_deleted), Toast.LENGTH_SHORT).show();
                        System.out.println("***setting up needsUpdating ****");
                        needsUpdating = true;
                        finish();
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
        });
    }

    private void doDeleteBookWanted(int status) {
        requestDeleteBookWanted(status);
        finish();
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
                        // Display book wanted successfully posted using Toast
                        Toast.makeText(getApplicationContext(), "Book wanted was successfully posted!", Toast.LENGTH_SHORT).show();
                        needsUpdating = true;
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
     * then add the book wanted to the table
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
                        // now add the entry to the book wanted table
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

    private void saveBookWantedChanges() {
        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String description = descEditText.getText().toString();
        String comment = commentEditText.getText().toString();

        RequestParams params = new RequestParams();

        // at the minimum book title cannot be empty
        if (Utility.isNotNull(title)) {
            params.put("bookid", bookID);
            params.put("isbn", isbn);
            params.put("title", title);
            params.put("author", author);
            params.put("edition", edition);
            params.put("description", description);
            params.put("id", bookWantedID);
            params.put("comment", comment);
            // Invoke RESTful Web Service with Http parameters
            requestUpdateBookWanted(params);
        } else {
            Utility.beep();
            Toast.makeText(this, "Book title cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    private void requestUpdateBookWanted(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + updateBookWantedURI, params, new AsyncHttpResponseHandler() {
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
                        needsUpdating = true;
                        finish();
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //System.out.println("***Received Edit Request Code with OK result");
                // update current screen - just close for now, but
                // pass info to update the list
                needsUpdating = true;
                finish();
            }
            return;
        }
    }

    @Override
    public void finish() {

        if (needsUpdating) {
            Intent data = new Intent();
            data.putExtra("NewPosting", needsUpdating);
            setResult(RESULT_OK, data);
        }

        super.finish();
    }

}
