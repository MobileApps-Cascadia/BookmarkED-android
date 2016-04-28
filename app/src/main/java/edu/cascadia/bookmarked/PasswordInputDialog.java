package edu.cascadia.bookmarked;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by seanchung on 4/10/16.
 */
public class PasswordInputDialog extends DialogFragment {
    private EditText mEditText;

    private TextView msgTextView;
    private Button btnOK;
    private Button btnCancel;

    public interface PasswordInputDialogListener {
        void onFinishEditDialog(String pwd);
    }

    public PasswordInputDialog() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_input_password, container);
        mEditText = (EditText) view.findViewById(R.id.input);
        msgTextView = (TextView) view.findViewById(R.id.msgTextView);
        btnOK = (Button) view.findViewById(R.id.btnOk);
        btnCancel = (Button) view.findViewById(R.id.btnCancel);

        msgTextView.setText("To change the email address, you need to enter your password");

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptPassword();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelPassword();
            }
        });
        getDialog().setTitle("Password");

        // Show soft keyboard automatically
        mEditText.requestFocus();
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        mEditText.setOnEditorActionListener(this);

        return view;
    }

    private void acceptPassword() {
        PasswordInputDialogListener activity = (PasswordInputDialogListener) getActivity();
        activity.onFinishEditDialog(mEditText.getText().toString());
        this.dismiss();
    }

    private void cancelPassword() {
        PasswordInputDialogListener activity = (PasswordInputDialogListener) getActivity();
        activity.onFinishEditDialog("");
        this.dismiss();
    }

//    @Override
//    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//        if (EditorInfo.IME_ACTION_DONE == actionId) {
//            // Return input text to activity
//            PasswordInputDialogListener activity = (PasswordInputDialogListener) getActivity();
//            activity.onFinishEditDialog(mEditText.getText().toString());
//            this.dismiss();
//            return true;
//        }
//        return false;
//    }
}
