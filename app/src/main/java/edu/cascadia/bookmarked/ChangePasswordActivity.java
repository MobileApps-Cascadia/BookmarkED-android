package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class ChangePasswordActivity extends AppCompatActivity {

    // Progress Dialog Object
    private ProgressDialog prgDialog;
    private String userEmail;
    private String tmpPwd;
    private boolean settingNewPassword;
    private boolean passwordChanged = false;

    private EditText currPwdEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userEmail = getIntent().getStringExtra("email");
        tmpPwd = getIntent().getStringExtra("pwd");
        settingNewPassword = Utility.isNotNull(tmpPwd);

        currPwdEditText = (EditText) findViewById(R.id.currentPassword);

        // hide the current password input field
        // and the cancel button
        if (settingNewPassword) {
            TextView currPwdTextView = (TextView) findViewById(R.id.currPwdTextView);
            currPwdTextView.setVisibility(View.GONE);
            currPwdEditText.setVisibility(View.GONE);

            Button cancelButton = (Button) findViewById(R.id.btnLinkCancel);
            cancelButton.setVisibility(View.GONE);
        }

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }


    public void changePassword(View view) {
        // sanity check
        if (Utility.isNotNull(userEmail) == false) {
            Toast.makeText(this, "Username unknown. Cannot change password", Toast.LENGTH_SHORT).show();
            return;
        }

        // verify that new password matches confirmed password
        EditText newPwdEditText = (EditText) findViewById(R.id.newPassword);
        EditText retypeNewPwdEditText = (EditText) findViewById(R.id.retypeNewPassword);

        String currPwd = settingNewPassword ? tmpPwd : currPwdEditText.getText().toString();
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

        prgDialog.show();

        FBUtility.getInstance().getFirebaseRef().changePassword(userEmail, currPwd, newPwd, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                prgDialog.hide();

                passwordChanged = true;
                finish();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                prgDialog.hide();
                Toast.makeText(getApplicationContext(), "Failed to change password. " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onCancelBtnClicked(View view) {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        Intent data = new Intent();

        if (settingNewPassword) {
            if (passwordChanged) {
                setResult(RESULT_OK, data);
            } else {
                data.putExtra("email", userEmail);
                data.putExtra("tmpPwd", tmpPwd);

                setResult(RESULT_CANCELED, data);
            }
        }
        super.finish();
    }
}