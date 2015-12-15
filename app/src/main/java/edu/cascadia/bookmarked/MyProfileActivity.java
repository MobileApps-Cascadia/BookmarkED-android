package edu.cascadia.bookmarked;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by seanchung on 11/30/15.
 */
public class MyProfileActivity extends RegisterActivity{

    private final int EDIT_PROFILE_REQUEST = 10;

    private final static String getUserInfoURI = "bookmarked/user/getuserinfo";

    private String userID;
    private Tracker mTracker;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userID = getIntent().getStringExtra("UserID");

        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);

        // set title to my profile
        ((TextView) findViewById(R.id.titleTextView)).setText(getString(R.string.title_activity_myprofile));

        getUserProfile();

        // Obtain the shared Tracker instance.
       /* AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        sendAnalytics();*/

        //Get a Tracker (should auto-report)
        ((AnalyticsApplication) getApplication()).getTracker(AnalyticsApplication.TrackerName.APP_TRACKER);
    }


   /* private void sendAnalytics() {
        mTracker.setScreenName("My Profile Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }*/
    protected void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_my_profile) {
            editProfile();
            return true;
        }  else if (item.getItemId() == R.id.action_cancel) {
            super.onBackPressed();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateFields(String jsonString) {
        try {
            adjustControls();

            JSONObject jsonObject = new JSONObject(jsonString);

            firstnameEditText.setText(jsonObject.getString("firstname"));
            lastnameEditText.setText(jsonObject.getString("lastname"));
            emailEditText.setText(jsonObject.getString("username"));
            phoneEditText.setText(jsonObject.getString("phone"));
            zipcodeEditText.setText(jsonObject.getString("zipcode"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // set controls for viewing mode
    private void adjustControls() {
        // hide password fields and buttons
        findViewById(R.id.passwordTextView).setVisibility(View.GONE);
        pwdEditText.setVisibility(View.GONE);
        findViewById(R.id.btnRegister).setVisibility(View.GONE);

        // display menu to change password
        findViewById(R.id.btnChangePassword).setVisibility(View.VISIBLE);

        if (editMode) return;  // don't disable controls

        // also set edit text to readonly
        firstnameEditText.setEnabled(false);
        lastnameEditText.setEnabled(false);
        emailEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        zipcodeEditText.setEnabled(false);

        // don't let the controls to be focusable,
        // so keyboard won't show up
        firstnameEditText.setFocusable(false);
        lastnameEditText.setFocusable(false);
        emailEditText.setFocusable(false);
        phoneEditText.setFocusable(false);
        zipcodeEditText.setFocusable(false);
    }

    private void getUserProfile() {

        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("username", userID);

        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";
        client.get(hostAddress + getUserInfoURI, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {

                prgDialog.hide();

                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has no error message, populate the fields
                    if (obj.has("error_msg") == false) {
                        populateFields(response);
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
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_404_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_500_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void editProfile() {
        Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
        editProfileIntent.putExtra("UserID", userID);
        startActivityForResult(editProfileIntent, EDIT_PROFILE_REQUEST);
    }

    public void onChangePassword(View view) {
        Intent changePwdIntent = new Intent(this, ChangePasswordActivity.class);
        changePwdIntent.putExtra(getString(R.string.user_id_param), userID);
        startActivity(changePwdIntent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_PROFILE_REQUEST) {
                if (data.hasExtra("NewUsername")) {
                    userID = data.getExtras().getString("NewUsername");
                    // refresh the screen with new data
                    getUserProfile();
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
