package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class ChangePasswordActivity extends AppCompatActivity {

    private final static String changePwdURI = "bookmarked/user/updateuserpassword";

    // Progress Dialog Object
    private ProgressDialog prgDialog;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = getIntent().getStringExtra(getString(R.string.user_id_param));

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }


    public void changePassword(View view) {
        // sanity check
        if (Utility.isNotNull(userID) == false) {
            Toast.makeText(this, "Username unknown. Cannot change password", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText currPwdEditText = (EditText) findViewById(R.id.currentPassword);

        // verify that new password matches confirmed password
        EditText newPwdEditText = (EditText) findViewById(R.id.newPassword);
        EditText retypeNewPwdEditText = (EditText) findViewById(R.id.retypeNewPassword);

        String currPwd = currPwdEditText.getText().toString();
        String newPwd = newPwdEditText.getText().toString();
        String retypeNewPwd = retypeNewPwdEditText.getText().toString();

        if (Utility.isNotNull(currPwd) == false) {
            Utility.beep();
            Toast.makeText(this, "Current password cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPwd.equals(retypeNewPwd) == false) {
            Utility.beep();
            Toast.makeText(this, "New password and confirmed new password are different", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String encNewPwd = Utility.encryptPassword(newPwd);
            if (encNewPwd.length() > 100) {
                Toast.makeText(this, "Password too long", Toast.LENGTH_SHORT).show();
                return;
            }

            doChangePassword(Utility.encryptPassword(currPwd), encNewPwd);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to encrypt password." + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void onCancelBtnClicked(View view) {
        super.onBackPressed();
    }

    private void doChangePassword(String currPwd, String newPwd) {
        // Show Progress Dialog
        prgDialog.show();

        RequestParams params = new RequestParams();
        params.add("username", userID);
        params.add("currentpass", currPwd);
        params.add("newpass", newPwd);

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(hostAddress + changePwdURI, params ,new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), "Password was successfully changed!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    // Else display error message
                    else {
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
}
