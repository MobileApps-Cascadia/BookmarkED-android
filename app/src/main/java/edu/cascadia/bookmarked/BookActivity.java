package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONException;
import org.json.JSONObject;

public class BookActivity extends AppCompatActivity {

    private final static String addABookURI = "bookmarked/book/addbook";
    private final static String addABookForSaleURI = "bookmarked/book/addbookforsale";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        String jsonString = getIntent().getStringExtra("bookInfo");

        readOnlyMode = (jsonString != null && jsonString.length() > 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponents();

        if (readOnlyMode) {
            setTitle("Detail book for sale");
            populateFields(jsonString);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (readOnlyMode)
            return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!readOnlyMode) {

            if (item.getItemId() == R.id.action_save_post_book) {
                addABook();
            } else if (item.getItemId() == R.id.action_cancel) {
                // To do: add confirmation to cancel and loose data
                super.onBackPressed();
            }
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

    private void populateFields(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            isbnEditText.setText(jsonObject.getString("isbn"));
            titleEditText.setText(jsonObject.getString("title"));
            authorEditText.setText(jsonObject.getString("author"));
            editionEditText.setText(jsonObject.getString("edition"));
            descEditText.setText(jsonObject.getString("description"));
            askingPriceEditText.setText(jsonObject.getString("askingprice"));
            bookConditionEditText.setText(jsonObject.getString("bookcondition"));

            // also set edit text to readonly
            isbnEditText.setEnabled(false);
            titleEditText.setEnabled(false);
            authorEditText.setEnabled(false);
            editionEditText.setEnabled(false);
            descEditText.setEnabled(false);
            askingPriceEditText.setEnabled(false);
            bookConditionEditText.setEnabled(false);

            // hide barcode button
            Button barcodeButton = (Button)findViewById(R.id.barcodeButton);
            barcodeButton.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onCancelClicked(View view) {
        super.onBackPressed();
    }

//    public void onAddEditClicked(View view) {
//        addABook();
//    }

    private void addABook() {
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

    private void addBook4Sale() {
        //System.out.println("*** in addBook4Sale ***");
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("isbn", isbnEditText.getText().toString());
        params.put("username", "admin@gmail.com");   // hard-coded for testing
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
                        // Set Default Values for Edit View controls
                        //setDefaultValues();
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), "Book was successfully posted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }

            }
        });
    }

    /**
     * Method that performs RESTful webservice invocations
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
                        // Set Default Values for Edit View controls
                        //setDefaultValues();
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), "Book was successfully added!", Toast.LENGTH_SHORT).show();
                        addBook4Sale();
                        //finish();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_SHORT).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_SHORT).show();
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
            //contentTxt.setText("Content: " + scanContent);
        } else {
            Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT).show();
        }

    }
}