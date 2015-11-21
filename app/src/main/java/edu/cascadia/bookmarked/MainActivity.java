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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnFragmentInteractionListener {

    private final int LOG_IN_REQUEST = 1;
    private final int POST_A_BOOK_REQUEST = 2;
    private final int POST_BOOK_WANTED_REQUEST = 3;

    private static boolean userLoggedIn = false;
    private boolean preferencesChanged = false; // did preferences change?

    private BookListFragment book4SaleListFragment;
    private BookListFragment bookWantedListFragment;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            bookWantedListFragment = BookListFragment.newInstance("buy-view", userID);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_login:
                doLogin();
                return true;

            case R.id.action_register:
                Intent registerIntent = new Intent(this, RegisterActivity.class);
                startActivity(registerIntent);
                return true;

            case R.id.action_postABook:
                if (userNotLoggedIn()) {
                    return true;
                }

                postABookForSale();
                return true;

            case R.id.action_mypostings:
                if (userNotLoggedIn()) {
                    return true;
                }

                Intent myPostingIntent = new Intent(this, MyPostingActivity.class);
                myPostingIntent.putExtra("UserID", userID);
                startActivity(myPostingIntent);
                return true;

            case R.id.action_setting:
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
                return true;

            case R.id.action_sync_book:
                // currently only handle refresh for book for sale only
                if (!findViewById(R.id.forSaleButton).isEnabled()) {
                    book4SaleListFragment.refreshList();
                } else {
                    bookWantedListFragment.refreshList();
                }
                return true;
            case R.id.action_logout:
                doLogout();
                return true;
            case R.id.action_post_book_wanted:
                Intent bookWantedIntent = new Intent(this, BookWantedActivity.class);
                bookWantedIntent.putExtra("BookAction", "AddNew");
                bookWantedIntent.putExtra("UserID", userID);
                startActivityForResult(bookWantedIntent, POST_BOOK_WANTED_REQUEST);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean userNotLoggedIn() {
        if (!userLoggedIn) {
            Utility.beep();
            Toast.makeText(this, getString(R.string.must_login), Toast.LENGTH_SHORT).show();
            doLogin();
            return true;
        }

        return false;
    }

    private void doLogout() {
        // perform logout only if user was logged in
        if (userLoggedIn) {
            userLoggedIn = !userLoggedIn;
            Toast.makeText(this, "You're logged out", Toast.LENGTH_SHORT).show();
        }
    }


    private void doLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivityForResult(intent, LOG_IN_REQUEST);
    }

    private void postABookForSale() {
        Intent bookIntent = new Intent(this, BookDetailActivity.class);
        // pass user id to detail
        bookIntent.putExtra("UserID", userID);
        bookIntent.putExtra("BookAction", "AddNew");

        startActivityForResult(bookIntent, POST_A_BOOK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == LOG_IN_REQUEST) {
                if (data.hasExtra("LoginResult")) {
                    userLoggedIn = data.getExtras().getBoolean("LoginResult");
                    userID = data.getExtras().getString("LoginUser");
                }
            } else if (requestCode == POST_A_BOOK_REQUEST) {
                if (data.hasExtra("NewPosting")) {
                    if (data.getExtras().getBoolean("NewPosting")) {
                        book4SaleListFragment.refreshList();
                    }
                }
            } else if (requestCode == POST_BOOK_WANTED_REQUEST) {
                if (data.hasExtra("NewPosting")) {
                    if (data.getExtras().getBoolean("NewPosting")) {
                        if (bookWantedListFragment != null) {
                            bookWantedListFragment.refreshList();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMyPostingBookClicked(BookItem bookItem, String listType) {
        // pass the book for sale information to detail activity
        if (listType.contains("sell")) {
            Intent bookIntent = new Intent(this, BookDetailActivity.class);
            bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);

            if (listType.equals("sell-view")) {
                bookIntent.putExtra("BookAction", "ViewExisting");
            } else if (listType.equals("my-sell-list")) {
                bookIntent.putExtra("BookAction", "AllowEdit");
            }

            startActivity(bookIntent);
        } else {
            // "buy-view" or "my-buy-list"
            Intent bookIntent = new Intent(this, BookWantedActivity.class);
            bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);

            if (listType.equals("buy-view")) {
                bookIntent.putExtra("BookAction", "ViewExisting");
            } else if (listType.equals("my-buy-list")) {
                bookIntent.putExtra("BookAction", "AllowEdit");
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
    }

    public void onWantedButtonClicked(View view) {
        view.setEnabled(false);
        findViewById(R.id.forSaleButton).setEnabled(true);
        findViewById(R.id.bookForSaleTextView).setVisibility(View.GONE);
        findViewById(R.id.bookWantedTextView).setVisibility(View.VISIBLE);
        showBookWantedListFragments();
    }


}
