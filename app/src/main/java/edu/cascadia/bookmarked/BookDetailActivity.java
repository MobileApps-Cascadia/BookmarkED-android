package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
    This activity can be called from the following:
    1. main screen in view only mode
    2. main screen via post a book for sale action
    3. My posting where user can initially view and then edit or delete the posting
 */
public class BookDetailActivity extends AppCompatActivity {

    private final static String addABookURI = "bookmarked/book/addbook";
    private final static String addABookForSaleURI = "bookmarked/book/addbookforsale";
    private final static String deleteBook4SaleURI = "bookmarked/book/deletebookforsale";

    private final static String ISBNDB_URI = "http://isbndb.com/api/v2/json/WQ3AZBWL/book/";

    private EditText isbnEditText;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText editionEditText;
    private EditText descEditText;
    private EditText askingPriceEditText;
    private EditText bookConditionEditText;
    private EditText noteEditText;

    // Progress Dialog Object
    private ProgressDialog prgDialog;

    // flag to indicate screen mode
    private boolean readOnlyMode;
    private String bookAction;
    private boolean newPosting = false;

    private String userID;

    private String book4SaleID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        String jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));
        final String jsonStr = getIntent().getStringExtra(getString(R.string.book_info_param));

        // possible value for bookAction:
        //  ViewExisting
        //  AddNew
        //  AllowEdit
        bookAction = getIntent().getStringExtra("BookAction");

        readOnlyMode = Utility.isNotNull(jsonString);
        //allowEdit = getIntent().getStringExtra("BookAction").equals("AllowEdit");

        if (!readOnlyMode) {
            userID = getIntent().getStringExtra("UserID");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponents();

        if (readOnlyMode) {
            setTitle("Detail book for sale");
            populateFields(jsonString);
        }
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
//            }
//        });

        Button startBtn = (Button) findViewById(R.id.button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendEmail(jsonStr);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 3 possibilities in the toolbar menu,
        // edit & delete for existing book
        // save & cancel for new entry
        // nothing when viewing the book
        if (Utility.isNotNull(bookAction)) {
            if (bookAction.equals("AllowEdit")) {
                getMenuInflater().inflate(R.menu.menu_book_edit, menu);
            } else if (bookAction.equals("AddNew")) {
                getMenuInflater().inflate(R.menu.menu_book, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }
    protected void sendEmail(String jsonStr) {

 //String[] TO = {""};
        try{
            Log.i("Send email", "");
            JSONObject jsonObj = new JSONObject(jsonStr);
           // String TO = jsonObj.getString("username");
            String[] TO = new String[] { jsonObj.getString("username") };

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,  "I am interested in buying the book with ISBN: " + jsonObj.getString("isbn"));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email.", "");
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(BookDetailActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_posted_book) {
            Toast.makeText(this, "To edit book", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.action_delete_posted_book) {
            Toast.makeText(this, "To delete book", Toast.LENGTH_SHORT).show();
            confirmDeleteBook4Sale();
        } else if (item.getItemId() == R.id.action_save_post_book) {
            addABookForSale();
        } else if (item.getItemId() == R.id.action_cancel) {
            // To do: add confirmation to cancel and loose data
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initComponents() {
        this.setTitle("Post book for sale");

        isbnEditText = (EditText) findViewById(R.id.bookIsbn);
        titleEditText = (EditText) findViewById(R.id.bookTitle);
        authorEditText = (EditText) findViewById(R.id.bookAuthor);
        editionEditText = (EditText) findViewById(R.id.bookEdition);
        descEditText = (EditText) findViewById(R.id.bookDescription);

        askingPriceEditText = (EditText) findViewById(R.id.bookAskingPrice);
        bookConditionEditText = (EditText) findViewById(R.id.bookCondition);
        //noteEditText = (EditText) findViewById(R.id.note)

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

    }

    // set controls for viewing mode
    private void disableControls() {
        // also set edit text to readonly
        isbnEditText.setEnabled(false);
        titleEditText.setEnabled(false);
        authorEditText.setEnabled(false);
        editionEditText.setEnabled(false);
        descEditText.setEnabled(false);
        askingPriceEditText.setEnabled(false);
        bookConditionEditText.setEnabled(false);

        // don't let the controls to be focusable,
        // so keyboard won't show up
        isbnEditText.setFocusable(false);
        titleEditText.setFocusable(false);
        authorEditText.setFocusable(false);
        editionEditText.setFocusable(false);
        descEditText.setFocusable(false);
        askingPriceEditText.setFocusable(false);
        bookConditionEditText.setFocusable(false);

        // hide the barcode button
        Button barcodeButton = (Button)findViewById(R.id.barcodeButton);
        barcodeButton.setVisibility(View.GONE);
    }

    // fill the fields only for read only mode
    private void populateFields(String jsonString) {
        try {
            disableControls();

            JSONObject jsonObject = new JSONObject(jsonString);
            book4SaleID = jsonObject.getString("id");

            isbnEditText.setText(jsonObject.getString("isbn"));
            titleEditText.setText(jsonObject.getString("title"));
            authorEditText.setText(jsonObject.getString("author"));
            editionEditText.setText(jsonObject.getString("edition"));
            descEditText.setText(jsonObject.getString("description"));
            askingPriceEditText.setText(jsonObject.getString("askingprice"));
            bookConditionEditText.setText(jsonObject.getString("bookcondition"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // fill fields with data from isbn db
    private void populateBookFields(JSONObject jsonBook) {
        try {
            titleEditText.setText(jsonBook.getString("title"));
            JSONArray jsonAuthors = jsonBook.getJSONArray("author_data");
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < jsonAuthors.length(); i++) {
                stringBuffer.append(((JSONObject) jsonAuthors.get(i)).getString("name") + ", ");
            }
            // delete last 2 char (comma and space);
            int lastComma = stringBuffer.lastIndexOf(", ");
            stringBuffer.delete(lastComma, lastComma + 1);
            authorEditText.setText(stringBuffer);
            editionEditText.setText(jsonBook.getString("edition_info"));
            descEditText.setText(jsonBook.getString("summary"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onCancelClicked(View view) {
        super.onBackPressed();
    }

    private void addABookForSale() {

        // verify that userID exist
        if (!Utility.isNotNull(userID)) {
            Utility.beep();
            Toast.makeText(this,"User name is unknown. Cannot add book for sale", Toast.LENGTH_SHORT).show();
            return;
        }
        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String description = descEditText.getText().toString();

        RequestParams params = new RequestParams();

        // When isbn Edit View, title Edit View and author Edit View have values other than Null
        if (Utility.isNotNull(isbn) && Utility.isNotNull(title) && Utility.isNotNull(author)) {
            params.put("isbn", isbn);
            params.put("title", title);
            params.put("author", author);
            params.put("edition", edition);
            params.put("description", description);
            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }
    }

    private void requestAddBook4Sale() {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("isbn", isbnEditText.getText().toString());
        params.put("username", userID);
        params.put("askingprice", askingPriceEditText.getText().toString());
        params.put("bookcondition", bookConditionEditText.getText().toString());
        params.put("note", "sample note");

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + addABookForSaleURI, params, new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), "Book was successfully posted!", Toast.LENGTH_SHORT).show();
                        newPosting = true;
                        finish();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
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
    private void invokeWS(RequestParams params){
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
                        // Display book successfully added message using Toast
                        // Toast.makeText(getApplicationContext(), "Book was successfully added!", Toast.LENGTH_SHORT).show();
                        // now add the entry to the book for sale table
                        requestAddBook4Sale();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
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

    public void onBarcodeButtonClicked(View view) {
        // Toast.makeText(this, "To read barcode with camera", Toast.LENGTH_SHORT).show();
        // scan
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null ) {
            String scanContent = scanningResult.getContents();
            //String scanFormat = scanningResult.getFormatName();
            // display the info
            isbnEditText.setText(scanContent);
            // look up ISBN db for book info
            lookUpIsbnDB(scanContent);
        } else {
            Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT).show();
        }

    }

    private void lookUpIsbnDB(String isbn) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();


        String hostAddress = ISBNDB_URI + isbn;
        System.out.println("***Querying isbn: " + hostAddress);
        client.get(hostAddress, new RequestParams(), new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    JSONObject obj = new JSONObject(response);

                    // When the JSON response has data, it has the book information
                    if (obj.getString("data").length() > 0) {
                        // Display book successfully added message using Toast
                        Toast.makeText(getApplicationContext(), "Book found", Toast.LENGTH_SHORT).show();
                        // System.out.println("data:" + obj.getString("data"));

                        // get actual book info, which is stored as an array
                        JSONArray jsonBook = obj.getJSONArray("data");
                        // now populate the fields related to the book info
                        populateBookFields(jsonBook.getJSONObject(0));
                    }
                    // Else display error message
                    else {
                        // error is found in the response
                        Toast.makeText(getApplicationContext(), obj.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }
        });
    }

    private void confirmDeleteBook4Sale() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Are you sure to delete?\nSelect reason to delete posting.");

        // set dialog message
        final String[] reasons = getResources().getStringArray(R.array.delete_book4sale_reasons);

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

//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // if this button is clicked, perform actual delete
//                        doDeleteBook4Sale();
//                    }
//                })
//
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
                            doDeleteBook4Sale(selectedItem[0] + 2);
                        } else {
                            Utility.beep();
                            Toast.makeText(getApplicationContext(), "Please select a reason", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        // show it
        alertDialog.show();
    }

    private void requestDeleteBook4Sale(int status) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("id", book4SaleID);
        params.put("status", status+"");

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + deleteBook4SaleURI, params, new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), "Posted book was successfully removed!", Toast.LENGTH_SHORT).show();
                        newPosting = true;
                        finish();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }

            }
        });
    }

    private void doDeleteBook4Sale(int status) {
        Toast.makeText(this, "To delete book - ID:" + book4SaleID, Toast.LENGTH_SHORT).show();
        requestDeleteBook4Sale(status);
        finish();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("NewPosting", newPosting);
        setResult(RESULT_OK, data);

        super.finish();
    }
}