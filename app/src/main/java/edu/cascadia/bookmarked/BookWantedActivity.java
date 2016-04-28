package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class BookWantedActivity extends AppCompatActivity {

    private final String TAG = "BookWantedActivity";

    private final static int EDIT_REQUEST_CODE = 2;
    private final static int SEARCH_BOOK_REQUEST = 12;

    private final static String ISBNDB_URI = "http://isbndb.com/api/v2/json/WQ3AZBWL/book/";

    // Progress Dialog Object
    protected ProgressDialog prgDialog;

    private BookWanted bookWanted;

    private EditText isbnEditText;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText editionEditText;
    private EditText descEditText;
    private EditText commentEditText;

    protected String bookAction;
    private Bitmap bitmap;

    private boolean needsUpdating = false;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_wanted);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        bookAction = getIntent().getStringExtra(getString(R.string.book_action_param));
        String jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));
        //userID = getIntent().getStringExtra(getString(R.string.user_id_param));

        initComponents();

        // now initialize the bookForSale object
        // if not adding a new book for sale
        if (!bookAction.equals("AddNew")) {
            Gson gson = new GsonBuilder().create();
            bookWanted = gson.fromJson(jsonString, BookWanted.class);
        }

        if (bookAction.equals("EditExisting")) {
            setTitle(getString(R.string.title_detail_book_wanted));
            populateFields(false);
        } else if (bookAction.equals("ViewExisting") || bookAction.equals("AllowEdit")) {
            setTitle(getString(R.string.title_detail_book_wanted));
            populateFields(true);
            disableBookWantedControls();
            hideBarcodeScanningButton();
            hideSearchBookOnlineButton();
        }


        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        sendScreenImageName();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utility.isNotNull(bookAction)) {
            if (bookAction.equals("AllowEdit")) {
                getMenuInflater().inflate(R.menu.menu_book_edit, menu);
                hideContactBuyerButton(menu);
            } else if (bookAction.equals("AddNew") || bookAction.equals("EditExisting")) {
                getMenuInflater().inflate(R.menu.menu_book, menu);
                // hide camera menu
                MenuItem cameraMenuItem = menu.findItem(R.id.action_take_picture);
                cameraMenuItem.setVisible(false);
            } else if (bookAction.equals("ViewExisting")) {
                getMenuInflater().inflate(R.menu.menu_book_view, menu);
            }

            if (bookAction.equals("AddNew")) {
                hideContactBuyerButton(menu);
            }

        }

        return super.onCreateOptionsMenu(menu);
    }


    protected void hideContactBuyerButton(Menu menu) {
        //findViewById(R.id.contactBuyerButton).setVisibility(View.GONE);
        MenuItem emailMenuItem = menu.findItem(R.id.action_login);
        if (emailMenuItem != null) {
            emailMenuItem.setVisible(false);
        }
    }

    private void hideSearchBookOnlineButton() {
        findViewById(R.id.searchOnlineBookButton).setVisibility(View.GONE);
    }

    private void hideBarcodeScanningButton() {
        // hide the barcode button for now.
        Button barcodeButton = (Button)findViewById(R.id.barcodeButton);
        barcodeButton.setVisibility(View.GONE);
    }

    private void composeAndSendEmail(String eMail) {
        Log.i("Send email to ", eMail);
        String[] TO = new String[]{eMail};

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I have the book with ISBN: " + bookWanted.getIsbn());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please let me know if you would like to make a deal");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email.", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(BookWantedActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void sendEmail() {
        Log.i("Send email", "");
        String buyerID = bookWanted.getUserId();

        if (buyerID.length() > 0 ) {
            prgDialog.show();

            FBUtility.getInstance().getUserProfileByID(buyerID, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User buyerProfile = dataSnapshot.getValue(User.class);
                        System.out.println("*** User:" + buyerProfile.getFirstName() + " " + buyerProfile.getLastName() + " email:" + buyerProfile.getEmail());
                        composeAndSendEmail(buyerProfile.getEmail());
                        prgDialog.hide();
                    } else {
                        prgDialog.hide();
                        Utility.showErrorDialog(getApplicationContext(), "Could not find buyer's email");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    prgDialog.hide();
                    Toast.makeText(getApplicationContext(), "User cancelled operation", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

            case R.id.action_share:
                setShareIntent();
                return true;

            case R.id.action_delete_posted_book:
                deleteABookWanted();
                return true;

            case R.id.action_contact_email:
                sendEmail();
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //share
    //share specific book to social networks
    private void setShareIntent() {
        //we'll share: bookImageView, titleEditText
        String title = titleEditText.getText().toString();
        Intent shareIntent = new Intent();

        //get URI of the bitmap and put it into shareIntent if not null
        if (bitmap != null){
            String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                    bitmap, "Image Description", null);
            Uri uri = Uri.parse(path);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Book Wanted");
        String downloadURL = "https://play.google.com/store/apps/details?id=edu.cascadia.bookmarked";
        //shareIntent.putExtra(Intent.EXTRA_TEXT, "Download BookmarkEd\n");  //override from line below
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Download BookmarkEd\n" + downloadURL);
        shareIntent.setAction(Intent.ACTION_SEND);
        startActivity(Intent.createChooser(shareIntent, "Share Book"));
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


    }

    protected void populateFields(boolean readonly) {
        isbnEditText.setText(bookWanted.getIsbn());
        titleEditText.setText(bookWanted.getTitle());
        authorEditText.setText(bookWanted.getAuthor());
        editionEditText.setText(bookWanted.getEdition());
        descEditText.setText(bookWanted.getDescription());

        commentEditText.setText(bookWanted.getComment());

        if (readonly) disableControls();

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

        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String description = descEditText.getText().toString();
        String comment = commentEditText.getText().toString();

        // perform minimal validation
        if (title.length() == 0) {
            Utility.beep();
            Toast.makeText(getApplicationContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = FBUtility.getInstance().getUserUid();
        bookWanted = new BookWanted(userID, isbn, title, author,edition, description,comment);
        Firebase bookWantedRef = FBUtility.getInstance().getFirebaseRef().child("bookWanted/");
        Firebase newBookWantedRef = bookWantedRef.push();

        bookWanted.setKey(newBookWantedRef.getKey());
        newBookWantedRef.setValue(bookWanted, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    Toast.makeText(getApplicationContext(), "Book wanted was successfully posted!", Toast.LENGTH_SHORT).show();
                    needsUpdating = true;
                    finish();
                }
            }
        });

    }

    private void editABookWanted() {

        Intent editIntent = new Intent(this, BookWantedActivity.class);
        editIntent.putExtra(getString(R.string.book_action_param), "EditExisting");
        Gson gson = new GsonBuilder().create();
        editIntent.putExtra(getString(R.string.book_info_param), gson.toJson(bookWanted));

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
                            deleteBookWanted(reasons[selectedItem[0]]);
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

    private void archiveBookWanted() {
        // set the last updateDate
        bookWanted.setUpdatedDate(new Date());
        Firebase delBookWantedRef = FBUtility.getInstance().getFirebaseRef().child("deleted/bookWanted/" + bookWanted.getKey());
        delBookWantedRef.setValue(bookWanted, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    Toast.makeText(getApplicationContext(), "Book wanted was successfully removed!", Toast.LENGTH_SHORT).show();
                    needsUpdating = true;
                    finish();
                }
            }
        });
    }

    private void deleteBookWanted(String status) {
        bookWanted.setStatus(status);


        // remove the old record first
        Firebase bookWantedRef = FBUtility.getInstance().getFirebaseRef().child("bookWanted/" + bookWanted.getKey());
        bookWantedRef.setValue(null, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    archiveBookWanted();
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

    public void onSearchOnlineBookButtonClicked(View view) {
        Intent searchBookIntent = new Intent(this, SearchBookActivity.class);
        startActivityForResult(searchBookIntent, SEARCH_BOOK_REQUEST);
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

    protected void populateFields() {
        disableControls();

        isbnEditText.setText(bookWanted.getIsbn());
        titleEditText.setText(bookWanted.getTitle());
        authorEditText.setText(bookWanted.getAuthor());
        editionEditText.setText(bookWanted.getEdition());
        descEditText.setText(bookWanted.getDescription());

        commentEditText.setText(bookWanted.getComment());

    }

    private void saveBookWantedChanges() {
        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String description = descEditText.getText().toString();
        String comment = commentEditText.getText().toString();

        // at the minimum book title cannot be empty
        if (Utility.isNotNull(title)) {
            bookWanted.setIsbn(isbn);
            bookWanted.setTitle(title);
            bookWanted.setAuthor(author);
            bookWanted.setEdition(edition);
            bookWanted.setDescription(description);
            bookWanted.setComment(comment);

            // update the last update date
            bookWanted.setUpdatedDate(new Date());
            Firebase bookWantedRef = FBUtility.getInstance().getFirebaseRef().child("bookWanted/" + bookWanted.getKey());
            bookWantedRef.setValue(bookWanted, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                    } else {
                        Toast.makeText(getApplicationContext(), "Book wanted was successfully updated!", Toast.LENGTH_SHORT).show();
                        needsUpdating = true;
                        finish();
                    }
                }
            });
        } else {
            Utility.beep();
            Toast.makeText(this, "Book title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void getFoundBookInfo(Intent intent) {

        // populate the fields with the found book data
        isbnEditText.setText(getExtraInfo(intent, "isbn"));
        titleEditText.setText(getExtraInfo(intent, "title"));
        authorEditText.setText(getExtraInfo(intent, "author"));
        editionEditText.setText(getExtraInfo(intent, "edition"));
        descEditText.setText(getExtraInfo(intent, "summary"));
    }

    private String getExtraInfo(Intent intent, String key) {
        if (intent.hasExtra(key)) {
            return intent.getExtras().getString(key);
        }

        return "";
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_REQUEST_CODE) {
                // update current screen - just close for now, but
                // pass info to update the list
                needsUpdating = true;
                finish();
                return;
            } else if (requestCode == SEARCH_BOOK_REQUEST) {
                // user select a book from the search book activity
                getFoundBookInfo(intent);
            } else {
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

    private void sendScreenImageName() {
        Log.i(TAG, "Setting screen name:" + getTitle());
        mTracker.setScreenName("Screen:" + getTitle());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
