package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private final static String registerURI = "bookmarked/user/doregister";
    private final static String verificationURI = "bookmarked/user/verifyregistration";

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
    // Passwprd Edit View Object
    protected EditText pwdEditText;
    protected EditText zipcodeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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

        initComponents();
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
        // Find Password Edit View control by ID
        pwdEditText = (EditText)findViewById(R.id.registerPassword);
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
        String phone = phoneEditText.getText().toString();
        String password = pwdEditText.getText().toString();
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

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(firstname) && Utility.isNotNull(lastname) &&
                Utility.isNotNull(email) && Utility.isNotNull(password)) {
            // clear error message, in case there was a message previously
            errorMsgTextView.setText("");
            // Put Http parameter firstname with value of First Name Edit View control
            params.put("firstname", firstname);
            // Put Http parameter lastname with value of Last Name Edit View control
            params.put("lastname", lastname);
            // Put Http parameter email with value of Email Edit View control
            params.put("username", email);
            // Put Http parameter phone with value of phone Edit View control
            params.put("phone", phone);
            // Put Http parameter password with value of Password Edit View control
            params.put("password", password);
            // Put Http parameter zipcode with value of zip code View control
            params.put("zipcode", zipcode);
            // Invoke RESTful Web Service with Http parameters
            sendRegistrationRequest(params);
        }
        // When Email is invalid
        else {
            Utility.beep();
            Toast.makeText(getApplicationContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    private void sendRegistrationRequest(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(hostAddress + registerURI, params ,new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if(obj.getBoolean("status")){
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), "You are successfully registered!", Toast.LENGTH_SHORT).show();

                        // now validate the registration by entering the code sent
                        // from the web service.
                        validateRegistrationCode();
                    }
                    // Else display error message
                    else {
                        errorMsgTextView.setText(obj.getString("error_msg"));
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
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_404_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_500_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    private void sendVerificationRequest(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(hostAddress + verificationURI, params ,new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if(obj.getBoolean("status")){
                        // Set Default Values for Edit View controls
                        setDefaultValues();
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), "You are successfully verified!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    // Else display error message
                    else {
                        errorMsgTextView.setText(obj.getString("error_msg"));
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
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_404_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_500_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues(){
        firstnameEditText.setText("");
        lastnameEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        pwdEditText.setText("");
        zipcodeEditText.setText("");
    }

    private void validateRegistrationCode() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Verification code");

        alertDialogBuilder.setIcon(R.drawable.ic_lock_open_black_24dp);

        alertDialogBuilder.setMessage("Please check your email for the registration confirmation and enter the verification code");
        // Set an EditText view to get user input
        final EditText codeEditText = new EditText(getApplicationContext());

        codeEditText.setTextColor(Color.BLACK);
        codeEditText.setPadding(20,0,20,0);
        codeEditText.requestFocus();

        alertDialogBuilder.setView(codeEditText);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Verify", null);

//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // if this button is clicked, perform actual delete
//                        doDeleteBook4Sale();
//                    }
//                })
//
//        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // if this button is clicked, just close
//                // the dialog box and do nothing
//                dialog.cancel();
//            }
//        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (codeEditText.getText().length() > 0) {
                            //Toast.makeText(getApplicationContext(), "Item selected", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            RequestParams requestParams = new RequestParams();
                            requestParams.add("username", emailEditText.getText().toString());
                            requestParams.add("password", pwdEditText.getText().toString());
                            requestParams.add("verificationcode", codeEditText.getText().toString());
                            // send request to web service
                            sendVerificationRequest(requestParams);
                        } else {
                            Utility.beep();
                            Toast.makeText(getApplicationContext(), "Please enter verification code", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        // show it
        alertDialog.show();
    }

}
