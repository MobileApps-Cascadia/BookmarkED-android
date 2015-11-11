package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private final static String loginURI = "bookmarked/login/dologin";

    // Progress Dialog Object
    private ProgressDialog prgDialog;
    // Email Edit View Object
    private EditText emailEditText;
    // Passwprd Edit View Object
    private EditText pwdEditText;
    // Error Msg TextView Object
    private TextView errorMsgTextView;

    private boolean loginOK = false;
    private String lastUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find Email Edit View control by ID
        emailEditText = (EditText)findViewById(R.id.loginEmail);
        // Find Password Edit View control by ID
        pwdEditText = (EditText)findViewById(R.id.loginPassword);
        // Find Error Msg Text View control by ID
        errorMsgTextView = (TextView)findViewById(R.id.login_error);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        // populate the email field with the last successful user
        lastUsername = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_username), "");

        if (Utility.isNotNull(lastUsername)) {
            emailEditText.setText(lastUsername);
            pwdEditText.requestFocus();
        }

    }

    /**
     * Method gets triggered when Login button is clicked
     *
     * @param view
     */
    public void loginUser(View view){
        // Get Email Edit View Value
        String email = emailEditText.getText().toString();
        // Get Password Edit View Value
        String password = pwdEditText.getText().toString();
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Email Edit View and Password Edit View have values other than Null
        if(Utility.isNotNull(email) && Utility.isNotNull(password)){
            // When Email entered is Valid
            if(Utility.validate(email)){
                // Put Http parameter username with value of Email Edit View control
                params.put("username", email);
                // Put Http parameter password with value of Password Edit Value control
                params.put("password", password);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }
            // When Email is invalid
            else{
                Utility.beep();
                errorMsgTextView.setText("Please enter a valid email");
            }
        }
        // When any of the Edit View control left blank
        else{
            Utility.beep();
            errorMsgTextView.setText("Please provide email and password");
        }

    }

    public void navigateToRegisterActivity(View view) {
        //Toast.makeText(getApplicationContext(), "To display register screen", Toast.LENGTH_SHORT).show();
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
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
        String hostAddress = "http://" + Utility.getServerAddress(this) + "/";

        client.get(hostAddress + loginURI, params, new AsyncHttpResponseHandler() {
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
                        loginOK = true;
                        Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_SHORT).show();

                        // update preferred user if necessary
                        if ( !lastUsername.equals(emailEditText.getText().toString()) ) {
                            updateUsernamePref();
                        }

                        // return to previous screen automatically
                        finish();
                    }
                    // Else display error message
                    else {
                        errorMsgTextView.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateUsernamePref() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getResources().getString(R.string.pref_username), emailEditText.getText().toString());
        editor.commit();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("LoginResult", loginOK);
        data.putExtra("LoginUser", emailEditText.getText().toString());
        setResult(RESULT_OK, data);

        super.finish();
    }
}
