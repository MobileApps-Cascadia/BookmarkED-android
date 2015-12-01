package edu.cascadia.bookmarked;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by seanchung on 11/30/15.
 */
public class EditProfileActivity extends MyProfileActivity {

    private final static String updateUserInfoURI = "bookmarked/user/updateuserinfo";

    private String userID;

    private boolean profileUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userID = getIntent().getStringExtra("UserID");

        // set title to my profile
        ((TextView) findViewById(R.id.titleTextView)).setText("Edit Profile");

        // enableControls();
        setEditMode(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_post_book) {
            saveProfile();
            return true;
        }  else if (item.getItemId() == R.id.action_cancel) {
            // To do: add confirmation to cancel and loose data
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableControls() {
        // enable edit texts
        firstnameEditText.setEnabled(true);
        lastnameEditText.setEnabled(true);
        emailEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        zipcodeEditText.setEnabled(true);

        // enable focusable
        firstnameEditText.setFocusable(true);
        lastnameEditText.setFocusable(true);
        emailEditText.setFocusable(true);
        phoneEditText.setFocusable(true);
        zipcodeEditText.setFocusable(true);
    }

    private void requestUpdateProfile() {

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("username", userID);
        params.put("firstname", firstnameEditText.getText().toString());
        params.put("lastname", lastnameEditText.getText().toString());
        params.put("newusername", emailEditText.getText().toString());
        params.put("phone", phoneEditText.getText().toString());
        params.put("zipcode", zipcodeEditText.getText().toString());


        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + updateUserInfoURI, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {


                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        // Display book for sale successfully posted using Toast
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_profile_updated), Toast.LENGTH_SHORT).show();
                        profileUpdated = true;
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

    private void saveProfile() {
        //Verify that user email, lastname and firstname are not blank
        String firstname = firstnameEditText.getText().toString();
        String lastname =  lastnameEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();

        if (Utility.isNotNull(firstname) && Utility.isNotNull(lastname) && Utility.isNotNull(newEmail)) {
            requestUpdateProfile();
        } else {
            Utility.beep();
            Toast.makeText(this, "First, last names and email address are required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        if (profileUpdated) {
            Intent data = new Intent();
            data.putExtra("NewUsername", emailEditText.getText().toString());
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

}
