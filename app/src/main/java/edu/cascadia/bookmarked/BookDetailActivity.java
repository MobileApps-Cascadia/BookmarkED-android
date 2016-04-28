package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/*
    This activity can be called from the following:
    1. main screen in view only mode
    2. main screen via post a book for sale action
    3. My posting where user can initially view and then edit or delete the posting
 */
public class BookDetailActivity extends AppCompatActivity {

    private final static String TAG = "BookDetailActivity";

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
    private Bitmap bitmap;

    private String base64Picture = "";

    // Progress Dialog Object
    protected ProgressDialog prgDialog;

    // flag to indicate screen mode
    protected String bookAction;
    private boolean needsUpdating = false;

    private BookForSale bookForSale = null;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        //System.out.println("***in BookDetailActivity.onCreate() ***");

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();


        String jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));
        // String jsonStr = getIntent().getStringExtra(getString(R.string.book_info_param));

        // possible value for bookAction:
        //  ViewExisting
        //  EditExisting
        //  AddNew
        //  AllowEdit
        bookAction = getIntent().getStringExtra(getString(R.string.book_action_param));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponents();

        // now initialize the bookForSale object
        // if not adding a new book for sale
        if (!bookAction.equals("AddNew")) {
            Gson gson = new GsonBuilder().create();
            bookForSale = gson.fromJson(jsonString, BookForSale.class);
            setTitle(getString(R.string.title_detail_book_for_sale));
            populateFields();

            if (!bookAction.equals("EditExisting")) {
                disableControls();
            }
        }

        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);

        sendScreenImageName();
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
//            }
//            else if (bookAction.equals("AddNew") || bookAction.equals("EditExisting")) {
//                getMenuInflater().inflate(R.menu.menu_book, menu);
            } else if (bookAction.equals("ViewExisting")) {
                getMenuInflater().inflate(R.menu.menu_book_view, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

//    protected void hideContactSellerButton() {
//        findViewById(R.id.contactSellerButton).setVisibility(View.GONE);
//    }

    private void sendEmail() {
        String sellerID = bookForSale.getUserId();

        if (sellerID.length() > 0 ) {
            prgDialog.show();

            FBUtility.getInstance().getUserProfileByID(sellerID, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User sellerProfile = dataSnapshot.getValue(User.class);
                        System.out.println("*** User:" + sellerProfile.getFirstName() + " " + sellerProfile.getLastName() + " email:" + sellerProfile.getEmail());
                        composeAndSendEmail(sellerProfile.getEmail());
                        prgDialog.hide();
                    } else {
                        prgDialog.hide();
                        Utility.showErrorDialog(getApplicationContext(), "Could not find seller's email");
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

    protected void composeAndSendEmail(String eMail) {
        Log.i("Send email to ", eMail);

        String[] TO = new String[]{eMail};

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I am interested in buying the book with ISBN: " + bookForSale.getIsbn());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email.", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(BookDetailActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
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
        } else if (item.getItemId() == R.id.action_contact_email) {
            sendEmail();
            return true;
        } else if  (item.getItemId() == R.id.action_share) {
            setShareIntent();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        String downloadURL = "https://play.google.com/store/apps/details?id=edu.cascadia.bookmarked";
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Download BookmarkEd\n" + downloadURL);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Book For Sale");
        shareIntent.setAction(Intent.ACTION_SEND);
        startActivity(Intent.createChooser(shareIntent, "Share Book"));
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
        askingPriceEditText.setEnabled(false);
        bookConditionSpinner.setEnabled(false);
        commentEditText.setEnabled(false);

        // don't let the controls to be focusable,
        // so keyboard won't show up
        isbnEditText.setFocusable(false);
        titleEditText.setFocusable(false);
        authorEditText.setFocusable(false);
        editionEditText.setFocusable(false);
        descEditText.setFocusable(false);
        //askingPriceEditText.setFocusable(false);
        //bookConditionEditText.setFocusable(false);
        askingPriceEditText.setFocusable(false);
        bookConditionSpinner.setFocusable(false);
        commentEditText.setFocusable(false);

        // hide the barcode button
        Button barcodeButton = (Button) findViewById(R.id.barcodeButton);
        barcodeButton.setVisibility(View.GONE);
    }

    // fill the fields only for read-only mode
    // data coming from the BookmarkEd web service
    protected void populateFields() {
        isbnEditText.setText(bookForSale.getIsbn());
        titleEditText.setText(bookForSale.getTitle());
        authorEditText.setText(bookForSale.getAuthor());
        editionEditText.setText(bookForSale.getEdition());
        descEditText.setText(bookForSale.getDescription());
        askingPriceEditText.setText(bookForSale.getAskingPrice());

        String bookCondStr = bookForSale.getBookCondition();
        if (Utility.isNotNull(bookCondStr)) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookConditionSpinner.getAdapter();
            int index = adapter.getPosition(bookCondStr);
            bookConditionSpinner.setSelection(index);
        } else {
            // do nothing set to Unknown
        }

        commentEditText.setText(bookForSale.getComment());

        base64Picture = bookForSale.getPicture();

        if (base64Picture != null && base64Picture.trim().length() > 0) {
            try {
                byte[] decodedString = Base64.decode(base64Picture, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                System.out.println("Bitmap width:" + bitmap.getWidth() + " height:" + bitmap.getHeight());
                bookImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                System.out.println("Exception in getting image. " + e.getMessage());
            }
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

    private void addABookForSale() {

        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String askingPrice = askingPriceEditText.getText().toString();
        String description = descEditText.getText().toString();
        String comment = commentEditText.getText().toString();
        String bookCondition = getBookConditionFromSpinner();

        // perform minimal validation
        if (title.length() == 0) {
            Utility.beep();
            Toast.makeText(getApplicationContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = FBUtility.getInstance().getUserUid();
        // verify that userID exist
        if (!Utility.isNotNull(userID)) {
            Utility.beep();
            Toast.makeText(this, "User ID is not valid. Cannot add book for sale", Toast.LENGTH_SHORT).show();
            return;
        }

        bookForSale = new BookForSale(userID, isbn, title, author, edition, description, askingPrice, bookCondition, comment, base64Picture);
        Firebase book4SaleRef = FBUtility.getInstance().getFirebaseRef().child("bookForSale/");
        Firebase newBook4SaleRef = book4SaleRef.push();

        bookForSale.setKey(newBook4SaleRef.getKey());
        newBook4SaleRef.setValue(bookForSale, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    Toast.makeText(getApplicationContext(), "Book for sale was successfully posted!", Toast.LENGTH_SHORT).show();
                    needsUpdating = true;
                    finish();
                }
            }
        });
    }

    protected void saveBoo4SaleChanges() {
        System.out.println("*** in saveBook4SaleChanges ***");

        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String askingPrice = askingPriceEditText.getText().toString();
        String description = descEditText.getText().toString();
        String comment = commentEditText.getText().toString();
        String bookCondition = getBookConditionFromSpinner();

        // at the minimum book title cannot be empty
        if (Utility.isNotNull(title)) {
//            Gson gson = new GsonBuilder().create();
//            bookForSale = gson.fromJson(jsonString, BookForSale.class);

            bookForSale.setIsbn(isbn);
            bookForSale.setTitle(title);
            bookForSale.setAuthor(author);
            bookForSale.setEdition(edition);
            bookForSale.setAskingPrice(askingPrice);
            bookForSale.setDescription(description);
            bookForSale.setComment(comment);
            bookForSale.setBookCondition(bookCondition);

            // update the last update date
            bookForSale.setUpdatedDate(new Date());
            Firebase bookForSaleRef = FBUtility.getInstance().getFirebaseRef().child("bookForSale/" + bookForSale.getKey());
            bookForSaleRef.setValue(bookForSale, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                    } else {
                        Toast.makeText(getApplicationContext(), "Book for sale was successfully updated!", Toast.LENGTH_SHORT).show();
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

    public void onBarcodeButtonClicked(View view) {
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
//            System.out.println("*** image W:" + imageBitmap.getWidth() + " image H:" + imageBitmap.getHeight());
//            System.out.println("*** image size:" + imageBitmap.getByteCount());

            //bookImageView.setImageBitmap(imageBitmap);
            bookImageView.setImageBitmap(imageBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // compress to 80% to reduce the data length to pass to web service
            // the images is not sharp however. Have to live with it for now, unless
            // we can upload the file separately.
            //imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            base64Picture = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // System.out.println("*** byteArray to store in table: " + byteArray.length + "  base64Picture:" + base64Picture.length());

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
        Gson gson = new GsonBuilder().create();
        editIntent.putExtra(getString(R.string.book_info_param), gson.toJson(bookForSale));
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
                            //requestDeleteBook4Sale(selectedItem[0] + 2);
                            deleteBook4Sale(reasons[selectedItem[0]]);
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

    private void archiveBook4Sale() {
        // set the last updateDate
        bookForSale.setUpdatedDate(new Date());
        Firebase delBook4SaleRef = FBUtility.getInstance().getFirebaseRef().child("deleted/bookForSale/" + bookForSale.getKey());
        delBook4SaleRef.setValue(bookForSale, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    Toast.makeText(getApplicationContext(), "Book for sale was successfully removed!", Toast.LENGTH_SHORT).show();
                    needsUpdating = true;
                    finish();
                }
            }
        });
    }

    private void deleteBook4Sale(String status) {
        bookForSale.setStatus(status);

        // remove the old record first
        Firebase book4SaleRef = FBUtility.getInstance().getFirebaseRef().child("bookForSale/" + bookForSale.getKey());
        book4SaleRef.setValue(null, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    archiveBook4Sale();
                }
            }
        });
    }

    protected void updateBook4Sale() {

        prgDialog.show();
        String title = titleEditText.getText().toString();

        // perform minimal validation
        if (title.length() == 0) {
            Utility.beep();
            Toast.makeText(getApplicationContext(), "Title is required", Toast.LENGTH_SHORT).show();
            prgDialog.hide();
            return;
        }

        // let's get the BookForSale object first
        String isbn = isbnEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String edition = editionEditText.getText().toString();
        String askingPrice = askingPriceEditText.getText().toString();
        String description = descEditText.getText().toString();
        String comment = commentEditText.getText().toString();
        String bookCondition = getBookConditionFromSpinner();

        bookForSale.setIsbn(isbn);
        bookForSale.setAuthor(author);
        bookForSale.setEdition(edition);
        bookForSale.setAskingPrice(askingPrice);
        bookForSale.setDescription(description);
        bookForSale.setComment(comment);
        bookForSale.setBookCondition(bookCondition);

        bookForSale.setUpdatedDate(new Date());

        Firebase book4SaleRef = FBUtility.getInstance().getFirebaseRef().child("bookForSale/" + bookForSale.getKey());
        book4SaleRef.setValue(bookForSale, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                } else {
                    Toast.makeText(getApplicationContext(), "Book for sale was successfully updated!", Toast.LENGTH_SHORT).show();
                    needsUpdating = true;
                    finish();
                }
            }
        });

    }

    private String getBookConditionFromSpinner() {
        if (bookConditionSpinner.getSelectedItemPosition() == 0) return "";

        return (String) bookConditionSpinner.getSelectedItem();
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

//    private Bitmap getBookImage() {
//        byte[] decodedString = Base64.decode(base64Picture, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//    }

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

            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    private void sendScreenImageName() {
        Log.i(TAG, "Setting screen name:" + getTitle());
        System.out.println("Setting screen name:" + getTitle());
        mTracker.setScreenName("Screen:" + getTitle());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}