package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

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
    private final static String updateBook4SaleURI = "bookmarked/book/updatebookforsale";
    private final static String getABook4SaleByIdURI = "bookmarked/book/getabookforsalebyid";

    private final static String ISBNDB_URI = "http://isbndb.com/api/v2/json/WQ3AZBWL/book/";

    private final static int EDIT_REQUEST_CODE = 2;
    static final int REQUEST_IMAGE_CAPTURE = 7;

    private EditText isbnEditText;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText editionEditText;
    private EditText descEditText;
    private EditText askingPriceEditText;
    private EditText commentEditText;

    private Spinner bookConditionSpinner;
    private ImageView bookImageView;

    private String base64Picture;

    // Progress Dialog Object
    protected ProgressDialog prgDialog;

    // flag to indicate screen mode
    private boolean readOnlyMode;
    protected String bookAction;
    private boolean needsUpdating = false;

    private String userID;
    protected String jsonString;
    private String book4SaleID;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        //System.out.println("***in BookDetailActivity.onCreate() ***");

        jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));
        final String jsonStr = getIntent().getStringExtra(getString(R.string.book_info_param));

        // possible value for bookAction:
        //  ViewExisting
        //  EditExisting
        //  AddNew
        //  AllowEdit
        bookAction = getIntent().getStringExtra(getString(R.string.book_action_param));

        readOnlyMode = Utility.isNotNull(jsonString);
        //allowEdit = getIntent().getStringExtra("BookAction").equals("AllowEdit");

        if (!readOnlyMode) {
            userID = getIntent().getStringExtra(getString(R.string.user_id_param));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponents();

        if (readOnlyMode) {
            setTitle(getString(R.string.title_detail_book_for_sale));
            populateFields(jsonString);
            disableBook4SaleControls();
        }
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
//            }
//        });

        //base64Picture = "";

        Button contactSellerBtn = (Button) findViewById(R.id.contactSellerButton);
        contactSellerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendEmail(jsonStr);
            }
        });

        if (!bookAction.equals("ViewExisting")) {
            hideContactSellerButton();
        }
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
            } else if (bookAction.equals("AddNew") || bookAction.equals("EditExisting")) {
                getMenuInflater().inflate(R.menu.menu_book, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    protected void hideContactSellerButton() {
        findViewById(R.id.contactSellerButton).setVisibility(View.GONE);
    }

    protected void sendEmail(String jsonStr) {
        try {
            Log.i("Send email", "");
            JSONObject jsonObj = new JSONObject(jsonStr);
            // String TO = jsonObj.getString("username");
            String[] TO = new String[]{jsonObj.getString("username")};

            Intent emailIntent = new Intent(Intent.ACTION_SEND);

            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I am interested in buying the book with ISBN: " + jsonObj.getString("isbn"));
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                finish();
                Log.i("Finished sending email.", "");
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(BookDetailActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_edit_posted_book) {
            editBook4Sale();
        } else if (item.getItemId() == R.id.action_delete_posted_book) {
            //Toast.makeText(this, "To delete book", Toast.LENGTH_SHORT).show();
            deleteABook4Sale();
        } else if (item.getItemId() == R.id.action_save_post_book) {
            addABookForSale();
        } else if (item.getItemId() == R.id.action_cancel) {
            // To do: add confirmation to cancel and loose data
            super.onBackPressed();
        } else if (item.getItemId() == R.id.action_take_picture) {
            takePicture();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initComponents() {
        setTitle(getString(R.string.title_post_book_for_sale));

        isbnEditText = (EditText) findViewById(R.id.bookIsbn);
        titleEditText = (EditText) findViewById(R.id.bookTitle);
        authorEditText = (EditText) findViewById(R.id.bookAuthor);
        editionEditText = (EditText) findViewById(R.id.bookEdition);
        descEditText = (EditText) findViewById(R.id.bookDescription);

        askingPriceEditText = (EditText) findViewById(R.id.bookAskingPrice);

        commentEditText = (EditText) findViewById(R.id.book4SaleComment);

        bookConditionSpinner = (Spinner) findViewById(R.id.bookConditionSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.book_conditions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        bookConditionSpinner.setAdapter(adapter);

        bookImageView = (ImageView) findViewById(R.id.bookPictureImageView);

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
        //askingPriceEditText.setEnabled(false);
        //bookConditionEditText.setEnabled(false);

        // don't let the controls to be focusable,
        // so keyboard won't show up
        isbnEditText.setFocusable(false);
        titleEditText.setFocusable(false);
        authorEditText.setFocusable(false);
        editionEditText.setFocusable(false);
        descEditText.setFocusable(false);
        //askingPriceEditText.setFocusable(false);
        //bookConditionEditText.setFocusable(false);

        // hide the barcode button
        Button barcodeButton = (Button) findViewById(R.id.barcodeButton);
        barcodeButton.setVisibility(View.GONE);
    }

    protected void disableBook4SaleControls() {
        askingPriceEditText.setEnabled(false);
        bookConditionSpinner.setEnabled(false);
        commentEditText.setEnabled(false);
        //bookConditionEditText.setEnabled(false);

        askingPriceEditText.setFocusable(false);
        bookConditionSpinner.setFocusable(false);
        commentEditText.setFocusable(false);
        //bookConditionEditText.setFocusable(false);

    }

    // fill the fields only for read-only mode
    // data coming from the BookmarkEd web service
    protected void populateFields(String jsonString) {
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
            //bookConditionEditText.setText(jsonObject.getString("bookcondition"));
            String bookCondStr = jsonObject.getString("bookcondition");
            if (Utility.isNotNull(bookCondStr)) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookConditionSpinner.getAdapter();
                int index = adapter.getPosition(bookCondStr);
                bookConditionSpinner.setSelection(index);
            } else {
                // do nothing set to Unknown
            }

            commentEditText.setText(jsonObject.getString("comment"));

            base64Picture = jsonObject.getString("picture");

//            System.out.println("base64Picture from json object");
//            System.out.println("=================================");
//            System.out.println(base64Picture);
//            System.out.println("=================================");
//            System.out.println("base64Picture length:" + base64Picture.length());

            if (base64Picture.trim().length() > 0) {
                try {
                    byte[] decodedString = Base64.decode(base64Picture, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    System.out.println("Bitmap width:" + bitmap.getWidth() + " height:" + bitmap.getHeight());

                    bookImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    System.out.println("Exception in getting image. " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // fill fields with data from isbn db web service
    private void populateBookFields(JSONObject jsonBook) {
        try {
            titleEditText.setText(jsonBook.getString("title"));
            JSONArray jsonAuthors = jsonBook.getJSONArray("author_data");
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
            Toast.makeText(this, "User name is unknown. Cannot add book for sale", Toast.LENGTH_SHORT).show();
            return;
        }
        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String description = descEditText.getText().toString();

        RequestParams params = new RequestParams();

        // When isbn Edit View, title Edit View have values other than Null
        if (Utility.isNotNull(isbn) && Utility.isNotNull(title)) {
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
        //params.put("bookcondition", bookConditionEditText.getText().toString());
        params.put("bookcondition", getBookConditionFromSpinner());
        params.put("comment", commentEditText.getText().toString());
        params.put("picture", base64Picture);
//        System.out.println("***base64Picture being assigned to param***");
//        System.out.println("*****************************");
//        System.out.println(base64Picture);
//        System.out.println("*****************************");

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + addABookForSaleURI + 2, params, new AsyncHttpResponseHandler() {
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

    /**
     * Method that performs RESTful webservice invocations
     * <p/>
     * This is a 2-step process. First add the book to the table
     * then add the book for sale table
     *
     * @param params
     */
    private void invokeWS(RequestParams params) {
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

    public void onBarcodeButtonClicked(View view) {
        // Toast.makeText(this, "To read barcode with camera", Toast.LENGTH_SHORT).show();
        // scan
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                System.out.println("***Received Edit Request Code with OK result");
                // update current screen - just close for now
                needsUpdating = true;
                finish();
            }
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bookImageView.setImageBitmap(imageBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // compress to 50% to reduce the data length to pass to web service
            // the images is not shart however. Have to live with it for now, unless
            // we can upload the file separately.
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byte[] byteArray = stream.toByteArray();
            base64Picture = Base64.encodeToString(byteArray, Base64.DEFAULT);

            System.out.println("*** byteArray to store in table: " + byteArray.length + "  base64Picture:" + base64Picture.length());

            return;
        }

        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
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
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }
        });
    }

    private void editBook4Sale() {
        Intent editIntent = new Intent(this, EditBook4SaleActivity.class);
        editIntent.putExtra(getString(R.string.book_action_param), "EditExisting");
        editIntent.putExtra(getString(R.string.book_info_param), jsonString);
        startActivityForResult(editIntent, EDIT_REQUEST_CODE);

    }


    private void deleteABook4Sale() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.title_delete_book_posting_reason));

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
                            requestDeleteBook4Sale(selectedItem[0] + 2);
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

    private void requestDeleteBook4Sale(int status) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("id", book4SaleID);
        params.put("status", status + "");

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
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_book_deleted), Toast.LENGTH_SHORT).show();
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

    protected void requestUpdateBook4Sale() {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("id", book4SaleID);
        params.put("askingprice", askingPriceEditText.getText().toString());
        params.put("bookcondition", getBookConditionFromSpinner());
        params.put("comment", commentEditText.getText().toString());

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + updateBook4SaleURI, params, new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_book_updated), Toast.LENGTH_SHORT).show();
                        //reloadBook4Sale();
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

    private String getBookConditionFromSpinner() {
        if (bookConditionSpinner.getSelectedItemPosition() == 0) return "";

        return (String) bookConditionSpinner.getSelectedItem();
    }

    // To be later to update the list adapter
//    private void reloadBook4Sale() {
//        // Show Progress Dialog
//        prgDialog.show();
//        // Make RESTful webservice call using AsyncHttpClient object
//        AsyncHttpClient client = new AsyncHttpClient();
//
//        RequestParams params = new RequestParams();
//        params.put("id", book4SaleID);
//
//        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
//        client.get(hostAddress + getABook4SaleByIdURI, params, new AsyncHttpResponseHandler() {
//            // When the response returned by REST has Http response code '200'
//            @Override
//            public void onSuccess(String response) {
//                // Hide Progress Dialog
//                prgDialog.hide();
//
//                try {
//                    // JSON Object
//                    JSONObject obj = new JSONObject(response);
//                    // When the JSON response has status boolean value assigned with true
//                    if (!Utility.isNotNull(obj.getString("error_msg"))) {
//                        // Display book for sale successfully posted using Toast
//                        //Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_book_updated), Toast.LENGTH_SHORT).show();
//                        // apply new json, to be passed back to calling activity
//                        jsonString = response;
//                        needsUpdating = true;
//                        //finish();
//                    }
//                    // Else display error message
//                    else {
//                        //errorMsg.setText(obj.getString("error_msg"));
//                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
//                    }
//                } catch (JSONException e) {
//                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//
//                }
//
//            }
//        });
//    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//                System.out.println("Failed to create file for image. " + ex.getMessage());
//                return;
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private Bitmap getBookImage() {
        byte[] decodedString = Base64.decode(base64Picture, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public void onPictureClicked(View view) {
        //Toast.makeText(this, "To display larger picture", Toast.LENGTH_SHORT).show();
        Intent bookIntent = new Intent(this, BookPictureActivity.class);
        bookIntent.putExtra("base64String", base64Picture);
        startActivity(bookIntent);
    }

    @Override
    public void finish() {
        if (needsUpdating) {
            Intent data = new Intent();
            data.putExtra("NewPosting", needsUpdating);

            System.out.println("===signal parent activity to refesh book list===");
            setResult(RESULT_OK, data);
        }
        super.finish();
    }
}