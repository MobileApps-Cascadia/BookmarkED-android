package edu.cascadia.bookmarked;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

public class Login extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "Login";

    private final static int REGISTER_USER_REQUEST = 11;

    // Progress Dialog Object
    private ProgressDialog prgDialog;
    // Email Edit View Object
    private EditText emailEditText;
    // Password Edit View Object
    private EditText pwdEditText;
    // Error Msg TextView Object
    private TextView errorMsgTextView;

    private boolean loginOK = false;
    private String lastUsername;
    private String userName;

    //private LoginButton buttonFBLogin;
    private CallbackManager callbackManager;
    private AccessTokenTracker mFacebookAccessTokenTracker;

    private static final int RC_GOOGLE_LOGIN = 3;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
     * without waiting. */
    private boolean mGoogleLoginClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can resolve them when the user clicks
     * sign-in. */
    private ConnectionResult mGoogleConnectionResult;


    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            prgDialog.hide();
            Log.i(TAG, provider + " authentication successful");

            if (provider.equals("facebook") || provider.equals("google")) {

                // check if profile exists. The first time the OAuth user
                // logs in, the user profile will not be available yet.
                checkUserProfileExists(authData);
            } else {
                // update preferred user if necessary
                userName = emailEditText.getText().toString();
                if (!lastUsername.equals(userName)) {
                    updateUsernamePref();
                }

                // user profile must exist if we come this far
                // let's load the profile for later use
                loadCurrentUserProfile();
            }

        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            prgDialog.hide();
            Toast.makeText(getApplicationContext(), "Error:" + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // check if facebook user profile exists, create one
        // if it does not
        private void checkUserProfileExists(final AuthData authData) {
            FBUtility.getInstance().getCurrentFBUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        loadCurrentUserProfile();
                    } else {
                        // facebook user profile does not exist. Create one
                        userName = authData.getProviderData().get("displayName").toString();
                        String[] userNames = userName.split(" ");
                        final String myEmail = authData.getProviderData().get("email").toString();
                        User userProfile = new User(userNames[0], userNames[1], myEmail, "", "", new Date());
                        createNewUserProfile(FBUtility.getInstance().getUserUid(), userProfile);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });

        }

        private void loadCurrentUserProfile() {
            // add the listener for reading data once
            FBUtility.getInstance().getCurrentFBUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        System.out.println("Fetched:" + dataSnapshot.getValue());
                        User userProfile = dataSnapshot.getValue(User.class);
                        System.out.println("*** User:" + userProfile.getFirstName() + " " + userProfile.getLastName() + " email:" + userProfile.getEmail());
                        FBUtility.getInstance().setCurrentUserProfile(userProfile);

                        // exit point for successful login
                        loginOK = true;
                        finish();
                    } else {
                        Utility.showErrorDialog(getApplicationContext(), "Snapshot does not exist");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });

        }

        private void createNewUserProfile(String uid, User userProfile) {
            Firebase userProfileRef = FBUtility.getInstance().getFirebaseRef().child("users/" + uid);
            userProfileRef.setValue(userProfile, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError == null) {
                        Toast.makeText(getApplicationContext(), "User profile was successfully created!", Toast.LENGTH_SHORT).show();
                        loadCurrentUserProfile();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this.getApplicationContext());

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "edu.cascadia.bookmarked",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        setContentView(R.layout.login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpFacebookButton();

        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i(TAG, "Facebook.AccessTokenTracker.OnCurrentAccessTokenChanged");
                onFacebookAccessTokenChange(currentAccessToken);
            }
        };

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

        setupGoogleLogin();
    }

    private void setupGoogleLogin() {
        /* *************************************
         *               GOOGLE                *
         ***************************************/
        /* Load the Google login button */
        SignInButton mGoogleLoginButton = (SignInButton) findViewById(R.id.login_with_google);
        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleLoginClicked = true;
                if (!mGoogleApiClient.isConnecting()) {
                    if (mGoogleConnectionResult != null) {
                        resolveSignInError();
                    } else if (mGoogleApiClient.isConnected()) {
                        getGoogleOAuthTokenAndLogin();
                    } else {
                    /* connect API now */
                        Log.d(TAG, "Trying to connect to Google API");
                        mGoogleApiClient.connect();
                    }
                }
            }
        });
        /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(new Scope("email"))
                .build();
    }

    /* ************************************
     *              GOOGLE                *
     **************************************
     */
    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    private void getGoogleOAuthTokenAndLogin() {
        //Toast.makeText(getApplicationContext(), "*** in getGoogleOAuthTokenAndLogin ***", Toast.LENGTH_SHORT).show();

        prgDialog.show();

        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(Login.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    FBUtility.getInstance().getFirebaseRef().authWithOAuthToken("google", token, new AuthResultHandler("google"));
                } else if (errorMessage != null) {
                    prgDialog.hide();
                    Toast.makeText(getApplicationContext(),"Error:" + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        Firebase ref = FBUtility.getInstance().getFirebaseRef();

        if (token != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    token,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                            Log.v("LoginActivity", response.toString());

                            // Application code
                            try {
                                String email = jsonObject.getString("email");
                                String name = jsonObject.getString("name");
                                //String userLocation = jsonObject.getString("user_location");
                                //Toast.makeText(getApplicationContext(), "Email from FB:" + email + " name: " + name, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //String birthday = object.getString("birthday"); // 01/31/1980 format
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email");
            request.setParameters(parameters);
            request.executeAsync();

            ref.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler("facebook"));
        } else {
        /* Logged out of Facebook so do a logout from the Firebase app */
            ref.unauth();
        }
    }

    private void setUpFacebookButton() {
        LoginButton buttonFBLogin = (LoginButton) findViewById(R.id.buttonFBLogin);
        buttonFBLogin.setReadPermissions(Arrays.asList("public_profile", "email"));

        if (buttonFBLogin.getText().equals("Log out")) {
            // force logout so the facebook button will have proper text
            LoginManager.getInstance().logOut();
        }

        callbackManager = CallbackManager.Factory.create();
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

        if(Utility.isNotNull(email) && Utility.isNotNull(password)) {
            prgDialog.show();
            FBUtility.getInstance().authWithPassword(email, password,
                    new AuthResultHandler("password"));

        } else{
            Utility.beep();
            errorMsgTextView.setText("Please provide email and password");
        }
    }

    public void navigateToRegisterActivity(View view) {
        //Toast.makeText(getApplicationContext(), "To display register screen", Toast.LENGTH_SHORT).show();
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivityForResult(registerIntent, REGISTER_USER_REQUEST);
    }

    private void updateUsernamePref() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getResources().getString(R.string.pref_username), emailEditText.getText().toString());
        editor.commit();
    }

    @Override
    public void onConnected(Bundle bundle) {
        getGoogleOAuthTokenAndLogin();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(TAG, result.toString());
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REGISTER_USER_REQUEST && resultCode == RESULT_OK) {
            // login registered user automatically
            emailEditText.setText(data.getExtras().getString("RegisteredUser"));

            // update preferred user if necessary
            if (!lastUsername.equals(emailEditText.getText().toString())) {
                updateUsernamePref();
            }

            // let's load the profile
            //FBUtility.getInstance().loadCurrentUserProfile();

            loginOK = true;
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        if (loginOK) {
            // testing
            Intent data = new Intent();
            //data.putExtra("LoginUser", userName); // emailEditText.getText().toString());
            data.putExtra("UserID", FBUtility.getInstance().getUserUid());
            System.out.println("***Returning userID: " + FBUtility.getInstance().getUserUid() + " as login user");
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // need to stop tracking the facebook token state change
        // when no longer needed. Otherwise, relogin using
        // facebook will have unpredictable state.
        if (mFacebookAccessTokenTracker != null) {
            mFacebookAccessTokenTracker.stopTracking();
        }
    }
}
