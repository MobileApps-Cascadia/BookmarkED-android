package edu.cascadia.bookmarked;

import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

/**
 * Created by seanchung on 1/21/16.
 */
public class FBUtility {
    private final String FIREBASE_URI = "https://fbbookmarked.firebaseio.com/";
    private static FBUtility instance = null;
    private static Firebase bkFirebase;

    // Data from the authenticated user
    private AuthData mAuthData = null;

    private User currentUserProfile = null;

    private FBUtility() {
        bkFirebase = new Firebase(FIREBASE_URI);
    }

    public static FBUtility getInstance() {
        if (instance == null) {
            instance = new FBUtility();
        }

        return instance;
    }

    public Firebase getFirebaseRef() {
        return bkFirebase;
    }

    // return firebase user (currently logged in) reference
    public Firebase getCurrentFBUserRef() {

        if (mAuthData != null) {
            return bkFirebase.child("users/" + mAuthData.getUid());
        }

        return null;  // user not authenticated
    }

    public void setAuthenticatedData(AuthData authData) {
        mAuthData = authData;
    }

    public AuthData getAuthenticatedData() {
        return mAuthData;
    }

    public String getUserUid() {
        if (mAuthData != null)
            return mAuthData.getUid();

        return "";
    }

    public User getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void setCurrentUserProfile(User userProfile) {
        currentUserProfile = userProfile;
    }

    public void updateCurrentUserProfile(final User userProfile) {
        //Firebase userProfileRef = FBUtility.getInstance().getFirebaseRef().child("users/" + uid);
        getCurrentFBUserRef().setValue(userProfile, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    //Toast.makeText(getApplicationContext(), "Error: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Failed to update user profile");
                } else {
                    // update successful, let's update
                    // the currentUserProfile
                    currentUserProfile = userProfile;
                    System.out.println( "User profile was successfully updated!");
                }
            }
        });

    }

    public void authWithPassword(String email, String password, Firebase.AuthResultHandler authResultHandler) {
        bkFirebase.authWithPassword(email, password, authResultHandler);
    }

    public void createAccount(String email, String password, Firebase.ValueResultHandler valueResultHandler) {
        bkFirebase.createUser(email,
                password,
                valueResultHandler);
    }

    public boolean currentUserProfileExist() {
        if (mAuthData != null) {
            Firebase userRef = getFirebaseRef().child("users/" + getUserUid());
            String userKey = userRef.getKey();
            //System.out.println("userKey " + userKey);
            return userKey != null && userKey.length() > 0;
        }

        return false;
    }

    public void getUserProfileByID(String uid, ValueEventListener valueEventListener) {
        bkFirebase.child("users/" + uid).addListenerForSingleValueEvent(valueEventListener);
    }
}
