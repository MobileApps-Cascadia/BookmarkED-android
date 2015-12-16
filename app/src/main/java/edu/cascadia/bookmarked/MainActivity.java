package edu.cascadia.bookmarked;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnFragmentInteractionListener {

    private final String TAG = "Main Activity";

    private final int LOG_IN_REQUEST = 1;
    private final int POST_BOOK4SALE_REQUEST = 2;
    private final int POST_BOOK_WANTED_REQUEST = 3;
    private final int MY_POSTINGS_REQUEST = 4;

    private final int POST_BOOK4SALE_PENDING_LOGIN_REQUEST = 21;
    private final int POST_BOOKWANTED_PENDING_LOGIN_REQUEST = 22;
    private final int MY_POSTINGS_PENDING_LOGIN_REQUEST = 23;

    private static boolean userLoggedIn = false;
    private boolean preferencesChanged = false; // did preferences change?

    private BookListFragment book4SaleListFragment;
    private BookListFragment bookWantedListFragment;

    private static String userID;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            //System.out.println("Main onCreate and savedInstanceState is not null");
            return;
        }
        setContentView(R.layout.activity_main);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showBook4SaleListFragments();

        // disable the for sale button initially
        findViewById(R.id.forSaleButton).setEnabled(false);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferenceChangeListener);

        sendScreenImageName();
    }

    private void sendScreenImageName() {
        Log.i(TAG, "Setting screen name:" + getTitle());
        mTracker.setScreenName("Screen:" + getTitle());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void showBook4SaleListFragments() {

        // insert book for sale list view
        if (book4SaleListFragment == null) {
            book4SaleListFragment = BookListFragment.newInstance("sell-view", "");
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.list_fragment_container, book4SaleListFragment);
        fragmentTransaction.commit();
    }

    private void showBookWantedListFragments() {

        // insert book wanted list view
        if (bookWantedListFragment == null) {
            bookWantedListFragment = BookListFragment.newInstance("wanted-view", userID);
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.list_fragment_container, bookWantedListFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        MenuItem menuItem = menu.findItem(R.id.action_myprofile);
//        if (menuItem != null) {
//            menuItem.setVisible(userLoggedIn);
//        }
//
//        MenuItem logoutMenuItem = menu.findItem(R.id.action_logout);
//        if (logoutMenuItem != null) {
//            logoutMenuItem.setVisible(userLoggedIn);
//        }
//
        MenuItem loginMenuItem = menu.findItem(R.id.action_login);
        if (loginMenuItem != null) {
            loginMenuItem.setVisible(!userLoggedIn);
        }

        menu.setGroupVisible(R.id.menu_group_login, userLoggedIn);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

//            case R.id.action_register:
//                Intent registerIntent = new Intent(this, RegisterActivity.class);
//                startActivity(registerIntent);
//                return true;

            case R.id.action_login:
                doLogin(LOG_IN_REQUEST);
                return true;

            case R.id.action_postABook:
                if (userNotLoggedIn(POST_BOOK4SALE_PENDING_LOGIN_REQUEST)) {
                    return true;
                }

                postABookForSale();
                return true;

            case R.id.action_mypostings:
                if (userNotLoggedIn(MY_POSTINGS_PENDING_LOGIN_REQUEST)) {
                    return true;
                }

                doMyPostings();
                return true;

            case R.id.action_post_book_wanted:
                if (userNotLoggedIn(POST_BOOKWANTED_PENDING_LOGIN_REQUEST)) {
                    return true;
                }

                doPostBookWanted();
                return true;

            case R.id.action_share:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Share")
                        .build());
                setShareIntent();
                return true;

//            case R.id.action_setting:
//                Intent settingIntent = new Intent(this, SettingActivity.class);
//                startActivity(settingIntent);
//                return true;

            case R.id.action_myprofile:
                // menu is only available when user logged in
                Intent profileIntent = new Intent(this, MyProfileActivity.class);
                profileIntent.putExtra("UserID", userID);
                startActivity(profileIntent);
                return true;


            case R.id.action_logout:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Logout")
                        .build());
                doLogout();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://bookmarked.com/download");
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    private boolean userNotLoggedIn(int requestType) {
        if (!userLoggedIn) {
            Utility.beep();
            Toast.makeText(this, getString(R.string.must_login), Toast.LENGTH_SHORT).show();
            doLogin(requestType);
            return true;
        }

        return false;
    }

    private void doLogout() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Logout")
                .build());

        // perform logout only if user was logged in
        if (userLoggedIn) {
            userLoggedIn = !userLoggedIn;
            userID = "";
            Toast.makeText(this, "You're logged out", Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }
    }


    private void doLogin(int requestType) {
        Intent intent = new Intent(this, Login.class);
        startActivityForResult(intent, requestType);
    }

    private void postABookForSale() {
        Intent bookIntent = new Intent(this, BookDetailActivity.class);
        // pass user id to detail
        bookIntent.putExtra(getString(R.string.user_id_param), userID);
        bookIntent.putExtra(getString(R.string.book_action_param), "AddNew");

        startActivityForResult(bookIntent, POST_BOOK4SALE_REQUEST);
    }

    private void doMyPostings() {
        Intent myPostingIntent = new Intent(this, MyPostingActivity.class);
        myPostingIntent.putExtra(getString(R.string.user_id_param), userID);
        startActivityForResult(myPostingIntent, MY_POSTINGS_REQUEST);
    }

    private void doPostBookWanted() {
        Intent bookWantedIntent = new Intent(this, BookWantedActivity.class);
        bookWantedIntent.putExtra(getString(R.string.book_action_param), "AddNew");
        bookWantedIntent.putExtra(getString(R.string.user_id_param), userID);
        startActivityForResult(bookWantedIntent, POST_BOOK_WANTED_REQUEST);
    }

    private void updateLoginUser(Intent data) {
        // login successful. Get the userID
        userID = data.getExtras().getString("LoginUser");
        userLoggedIn = true;
        invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case LOG_IN_REQUEST:
                updateLoginUser(data);
            break;

            case POST_BOOK4SALE_REQUEST:
                if (data.hasExtra("NewPosting")) {
                    if (data.getExtras().getBoolean("NewPosting")) {
                        book4SaleListFragment.refreshList();
                    }
                }
                break;

            case POST_BOOK_WANTED_REQUEST:
                if (data.hasExtra("NewPosting")) {
                    if (data.getExtras().getBoolean("NewPosting")) {
                        if (bookWantedListFragment != null) {
                            bookWantedListFragment.refreshList();
                        }
                    }
                }
                break;

            case MY_POSTINGS_REQUEST:
                if (data.hasExtra("NewPosting")) {
                    if (data.getExtras().getBoolean("NewPosting")) {
                        book4SaleListFragment.refreshList();
                        if (bookWantedListFragment != null) {
                            bookWantedListFragment.refreshList();
                        }
                    }
                }
                break;

            case POST_BOOK4SALE_PENDING_LOGIN_REQUEST:
                updateLoginUser(data);
                postABookForSale();
                break;

            case POST_BOOKWANTED_PENDING_LOGIN_REQUEST:
                updateLoginUser(data);
                doPostBookWanted();
                break;

            case MY_POSTINGS_PENDING_LOGIN_REQUEST:
                updateLoginUser(data);
                doMyPostings();
                break;
        }

    }

    @Override
    public void onMyPostingBookClicked(BookItem bookItem, String listType) {
        // pass the book for sale information to detail activity
        if (listType.contains("sell")) {
            Intent bookIntent = new Intent(this, BookDetailActivity.class);
            bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);

            if (listType.equals("sell-view")) {
                bookIntent.putExtra(getString(R.string.book_action_param), "ViewExisting");
            } else if (listType.equals("my-sell-list")) {
                bookIntent.putExtra(getString(R.string.book_action_param), "AllowEdit");
            }

            startActivity(bookIntent);
        } else {
            // "wanted-view" or "my-wanted-list"
            Intent bookIntent = new Intent(this, BookWantedActivity.class);
            bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);

            if (listType.equals("wanted-view")) {
                bookIntent.putExtra(getString(R.string.book_action_param), "ViewExisting");
            } else if (listType.equals("my-wanted-list")) {
                bookIntent.putExtra(getString(R.string.book_action_param), "AllowEdit");
            }

            startActivity(bookIntent);
        }
    }

    // listener for changes to the app's SharedPreferences
    private OnSharedPreferenceChangeListener preferenceChangeListener =
            new OnSharedPreferenceChangeListener()
            {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key)
                {
                    preferencesChanged = true; // user changed app settings
                    Toast.makeText(MainActivity.this,
                            "preference was changed", Toast.LENGTH_SHORT).show();
                } // end method onSharedPreferenceChanged
            }; // end anonymous inner class


    public void onForSaleButtonClicked(View view) {
        view.setEnabled(false);
        findViewById(R.id.wantedButton).setEnabled(true);
        findViewById(R.id.bookWantedTextView).setVisibility(View.GONE);
        findViewById(R.id.bookForSaleTextView).setVisibility(View.VISIBLE);
        showBook4SaleListFragments();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Displaying book for sale listing")
                .build());

    }

    public void onWantedButtonClicked(View view) {
        view.setEnabled(false);
        findViewById(R.id.forSaleButton).setEnabled(true);
        findViewById(R.id.bookForSaleTextView).setVisibility(View.GONE);
        findViewById(R.id.bookWantedTextView).setVisibility(View.VISIBLE);
        showBookWantedListFragments();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Displaying book wanted listing")
                .build());

    }

    public void onRefreshButtonClicked(View view) {
        // refresh the active/shown book list only
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Refresh book listing")
                .build());

        System.out.println("in onRefreshButtonClicked");
        if (!findViewById(R.id.forSaleButton).isEnabled()) {
            book4SaleListFragment.refreshList();
        } else {
            bookWantedListFragment.refreshList();
        }

    }
}
