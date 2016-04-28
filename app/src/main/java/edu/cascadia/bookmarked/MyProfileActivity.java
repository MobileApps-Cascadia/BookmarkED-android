package edu.cascadia.bookmarked;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by seanchung on 11/30/15.
 */
public class MyProfileActivity extends RegisterActivity{

    private final int EDIT_PROFILE_REQUEST = 10;

    private boolean editMode = false;
    private boolean oAuthUser = false;

    private String authProvider;

    protected User userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);

        // set title to my profile
        //((TextView) findViewById(R.id.titleTextView)).setText(getString(R.string.title_activity_myprofile));

        userProfile = FBUtility.getInstance().getCurrentUserProfile();
    }

    @Override
    protected void onStart() {
        super.onStart();

        authProvider = FBUtility.getInstance().getAuthenticatedData().getProvider();

        oAuthUser = (authProvider.equals("facebook") || authProvider.equals("google"));

        populateFields();
    }

    protected void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!oAuthUser) {
            getMenuInflater().inflate(R.menu.menu_my_profile, menu);
        }
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

    protected void populateFields() {
        adjustControls();

        if (userProfile != null) {
            firstnameEditText.setText(userProfile.getFirstName());
            lastnameEditText.setText(userProfile.getLastName());
            emailEditText.setText(userProfile.getEmail());
            phoneEditText.setText(userProfile.getPhone());
            zipcodeEditText.setText(userProfile.getZipcode());

            if (oAuthUser) {
                Utility.beep();
                oAuthUserNotice();
            }
        }
    }

    // set controls for viewing mode
    private void adjustControls() {
        // hide password fields and buttons
        //findViewById(R.id.passwordTextView).setVisibility(View.GONE);
        //pwdEditText.setVisibility(View.GONE);
        findViewById(R.id.btnRegister).setVisibility(View.GONE);

        // display menu to change password, if
        // not a facebook user
        if (!oAuthUser) {
            findViewById(R.id.btnChangePassword).setVisibility(View.VISIBLE);
        }

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

    private void oAuthUserNotice() {
        Toast.makeText(getApplicationContext(), authProvider + " profile is read-only mode", Toast.LENGTH_SHORT).show();
    }

    private void editProfile() {
        if (oAuthUser) {
            oAuthUserNotice();
            return;
        }

        Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
        //editProfileIntent.putExtra("UserID", userID);
        startActivityForResult(editProfileIntent, EDIT_PROFILE_REQUEST);
    }

    public void onChangePassword(View view) {
        Intent changePwdIntent = new Intent(this, ChangePasswordActivity.class);
        changePwdIntent.putExtra("email", userProfile.getEmail());
        startActivity(changePwdIntent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_PROFILE_REQUEST) {
                if (data.hasExtra("ProfileUpdated")) {
                    userProfile = FBUtility.getInstance().getCurrentUserProfile();
                    // refresh the screen with new data
                    populateFields();
                }
            }
        }
    }

}
