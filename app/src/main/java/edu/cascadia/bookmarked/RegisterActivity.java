package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Date;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private final static String SECRET = "$-BookmarkEd_Falls_2015-$";
    private final static int CHANGE_PWD_REQUEST = 25;

    // Progress Dialog Object
    protected ProgressDialog prgDialog;
    // Error Msg TextView Object
    private TextView errorMsgTextView;
    // First name Edit View Object
    protected EditText firstnameEditText;
    // Last name Edit View Object
    protected EditText lastnameEditText;
    // Email Edit View Object
    protected EditText emailEditText;
    // Phone Edit View Object
    protected EditText phoneEditText;
    protected EditText zipcodeEditText;

    private boolean registerComplete = false;
    private boolean accountCreated = false;

    private int wrongPwdCount = 0;

    // flow of the registration steps:
    // 1. validate user enters required fields
    // 2. create a new firebase account based on email and secret password
    // 3. once account created, request a reset so that user will get
    //    a temporary password from the email (a way to validate email)
    // 4. user login using temporary password and force to put new password.
    //    user has 3 attempts to enter correct temp password before the app
    //    abort the registration
    // 5. when new password is set, app creates a new user profile to complete
    //    the registration process

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponents();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

    }

    private void initComponents() {
        // Find Error Msg Text View control by ID
        errorMsgTextView = (TextView)findViewById(R.id.register_error);
        // Find Name Edit View control by ID
        firstnameEditText = (EditText)findViewById(R.id.registerFirstName);
        // Find Name Edit View control by ID
        lastnameEditText = (EditText)findViewById(R.id.registerLastName);
        // Find Email Edit View control by ID
        emailEditText = (EditText)findViewById(R.id.registerEmail);
        // Find phone Edit View control by ID
        phoneEditText = (EditText)findViewById(R.id.registerPhone);
        // Find zipcode Edit View control by ID
        zipcodeEditText = (EditText) findViewById(R.id.registerZipcode);
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }

    public void registerUser(View view) {
        String firstname = firstnameEditText.getText().toString();
        String lastname = lastnameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        //String phone = phoneEditText.getText().toString();
        String zipcode = zipcodeEditText.getText().toString();
        if (!Utility.validateEmail(email)) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            Utility.beep();
            emailEditText.requestFocus();
            return;
        }

        // zipcode can either be blank or 5 digit long.
        if (Utility.isNotNull(zipcode) && !Utility.validateZipcode(zipcode)) {
            Toast.makeText(this, "Zipcode must be 5 digit long", Toast.LENGTH_SHORT).show();
            Utility.beep();
            zipcodeEditText.requestFocus();
            return;
        }

        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(firstname) && Utility.isNotNull(lastname) &&
                Utility.isNotNull(email) ) {
            // clear error message, in case there was a message previously
            errorMsgTextView.setText("");
            createNewAccount(email);
        }
        // When Email is invalid
        else {
            Utility.beep();
            Toast.makeText(getApplicationContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestForTempPassword(final String email) {
        FBUtility.getInstance().getFirebaseRef().resetPassword(email, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                validateTempPassword(email);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(),
                        "Failed to send email for the temporary password", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createNewAccount(final String email) {
        prgDialog.show();

        FBUtility.getInstance().createAccount(email, SECRET,
                new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        System.out.println("Successfully created user account with uid: " + result.get("uid"));
                        accountCreated = true;
                        prgDialog.hide();

                        // request to send temporary password
                        requestForTempPassword(email);

                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        // there was an error
                        Utility.beep();
                        prgDialog.hide();

                        Toast.makeText(getApplicationContext(), "Failed to create user:" + firebaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserProfile(String uid) {
        prgDialog.show();
        String firstname = firstnameEditText.getText().toString();
        String lastname = lastnameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String zipcode = zipcodeEditText.getText().toString();

        final User userProfile = new User(firstname, lastname, email, phone, zipcode, new Date());

        System.out.println("*** Saving email: " + email);

        Firebase userProfileRef = FBUtility.getInstance().getFirebaseRef().child("users/" + uid);
        userProfileRef.setValue(userProfile, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    prgDialog.hide();
                    //Utility.showErrorDialog(getApplicationContext(), firebaseError.getMessage());
                    Toast.makeText(getApplicationContext(), "Error: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    prgDialog.hide();
                    Toast.makeText(getApplicationContext(), "User profile was successfully created!", Toast.LENGTH_SHORT).show();
                    FBUtility.getInstance().setCurrentUserProfile(userProfile);

                    registerComplete = true;
                    finish();
                }
            }
        });
    }

    private void validateTempPassword(final String email) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Password");

        alertDialogBuilder.setIcon(R.drawable.ic_lock_open_black_24dp);

        alertDialogBuilder.setMessage("Please check your email for the registration confirmation and enter the temporary password");
        // Set an EditText view to get user input
        final EditText tmpPwdEditText = new EditText(getApplicationContext());

        tmpPwdEditText.setTextColor(Color.BLACK);

        tmpPwdEditText.setPadding(20, 0, 20, 0);
        tmpPwdEditText.requestFocus();

        alertDialogBuilder.setView(tmpPwdEditText);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Login", null);

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (tmpPwdEditText.getText().length() > 0) {
                            //Toast.makeText(getApplicationContext(), "Item selected", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            loginWithTempPwd(email, tmpPwdEditText.getText().toString());
                        } else {
                            Utility.beep();
                            Toast.makeText(getApplicationContext(), "Please enter the correct password", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        // show it
        alertDialog.show();
    }

    private void loginWithTempPwd(final String email, final String tmpPwd) {
        prgDialog.show();

        FBUtility.getInstance().authWithPassword(email, tmpPwd,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        prgDialog.hide();

                        //getNewPassword(email, tmpPwd);
                        changePassword(email, tmpPwd);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        prgDialog.hide();
                        Toast.makeText(getApplicationContext(), "Error: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        wrongPwdCount++;
                        if (wrongPwdCount > 2) {
                            // abort registration
                            finish();
                        }
                        // try validating temp password again
                        validateTempPassword(email);
                    }
                });
    }

    private void changePassword(final String email, final String tmpPwd) {
        Intent changePwdIntent = new Intent(this, ChangePasswordActivity.class);
        changePwdIntent.putExtra("email", email);
        changePwdIntent.putExtra("pwd", tmpPwd);
        startActivityForResult(changePwdIntent, CHANGE_PWD_REQUEST);
    }

    private void removeAccount() {
        FBUtility.getInstance().getFirebaseRef().removeUser(emailEditText.getText().toString(), SECRET, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                System.out.println("Account with email:" + emailEditText.getText().toString() + " was successfully removed");
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                System.out.println("Failed to remove account with email:" + emailEditText.getText().toString() + " " + firebaseError.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CHANGE_PWD_REQUEST) {
            // password successfully changed
            if  (resultCode == RESULT_OK) {
                createUserProfile(FBUtility.getInstance().getUserUid());
            } else {
                Toast.makeText(getApplicationContext(),"You must change your password before continuing", Toast.LENGTH_SHORT).show();
                changePassword(data.getExtras().getString("email"), data.getExtras().getString("tmpPwd"));
            }
        }
    }

    @Override
    public void finish() {
        if (registerComplete) {
            Intent data = new Intent();
            data.putExtra("RegisteredUser", emailEditText.getText().toString());
            setResult(RESULT_OK, data);
        } else {
            if (accountCreated) {
                System.out.println("Removing account...");
                removeAccount();
            }
        }
        super.finish();
    }
}
