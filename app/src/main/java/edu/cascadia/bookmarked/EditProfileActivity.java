package edu.cascadia.bookmarked;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by seanchung on 11/30/15.
 */
public class EditProfileActivity extends MyProfileActivity implements PasswordInputDialog.PasswordInputDialogListener {

    private boolean profileUpdated = false;
    private String previousEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set title to my profile
        //((TextView) findViewById(R.id.titleTextView)).setText("Edit Profile");

        //enableControls();
        setEditMode(true);

        // save previous email
        previousEmail = userProfile.getEmail();

//        populateFields();
//        enableControls();
//        firstnameEditText.requestFocus();

        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_profile) {
            saveProfile();
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

    private void changeEmail() {
        FragmentManager fm = getSupportFragmentManager();
        PasswordInputDialog pwdInputDialog = new PasswordInputDialog();
        pwdInputDialog.show(fm, "fragment_edit_name");
    }

    private void saveProfile() {
        //Verify that user email, lastname and firstname are not blank
        String firstname = firstnameEditText.getText().toString();
        String lastname =  lastnameEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();

        if (Utility.isNotNull(firstname) && Utility.isNotNull(lastname) && Utility.isNotNull(newEmail)) {
            if (!previousEmail.equals(newEmail)) {
                changeEmail();
            } else {
                updateUserProfile();
                profileUpdated = true;
                finish();
            }
        } else {
            Utility.beep();
            Toast.makeText(this, "First, last names and email address are required", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfile() {
        userProfile.setFirstName(firstnameEditText.getText().toString());
        userProfile.setLastName(lastnameEditText.getText().toString());
        userProfile.setEmail(emailEditText.getText().toString());
        userProfile.setPhone(phoneEditText.getText().toString());
        userProfile.setZipcode(zipcodeEditText.getText().toString());
        // also update the last updatedDate
        userProfile.setUpdatedDate(new Date());

        FBUtility.getInstance().updateCurrentUserProfile(userProfile);

    }

    @Override
    public void finish() {
        if (profileUpdated) {
            Intent data = new Intent();
            data.putExtra("ProfileUpdated", true);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    @Override
    public void onFinishEditDialog(String pwd) {
        if (pwd.length() > 0) {
            FBUtility.getInstance().getFirebaseRef().changeEmail(previousEmail, pwd, emailEditText.getText().toString(),
                    new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Email was successfully changed", Toast.LENGTH_SHORT).show();
                            updateUserProfile();
                            profileUpdated = true;
                            finish();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getApplicationContext(), "Failed to change the email", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            profileUpdated = false;
            finish();
        }
    }
}
