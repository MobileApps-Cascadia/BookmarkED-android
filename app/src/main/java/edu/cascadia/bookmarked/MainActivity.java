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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnFragmentInteractionListener {

    private final int LOG_IN_REQUEST = 1;
    private final int POST_A_BOOK_REQUEST =2;

    private boolean userLoggedIn = false;
    private boolean preferencesChanged = false; // did preferences change?

    private BookListFragment bookListFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        insertBook4SaleListFragments();


        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferenceChangeListener);

    }

    private void insertBook4SaleListFragments() {

        // insert book for sale list view
        bookListFragment = BookListFragment.newInstance("sell");

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.list_fragment_container, bookListFragment);
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
                if (!userLoggedIn) {
                    Utility.beep();
                    Toast.makeText(this, getString(R.string.must_login), Toast.LENGTH_SHORT).show();
                    doLogin();
                    return true;
                }
                Intent bookIntent = new Intent(this, BookDetailActivity.class);
                startActivityForResult(bookIntent, POST_A_BOOK_REQUEST);
                return true;

            case R.id.action_mypostings:
                Intent myPostingIntent = new Intent(this, MyPostingActivity.class);
                startActivity(myPostingIntent);
                return true;

            case R.id.action_setting:
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void doLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivityForResult(intent, LOG_IN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == LOG_IN_REQUEST) {
                if (data.hasExtra("LoginResult")) {
                    userLoggedIn = data.getExtras().getBoolean("LoginResult");
                }
            } else if (requestCode == POST_A_BOOK_REQUEST) {
                if (data.hasExtra("NewPosting")) {
                    if (data.getExtras().getBoolean("NewPosting")) {
                        bookListFragment.refreshList();
                    }
                }
            }
        }
    }

    @Override
    public void onMyPostingBookClicked(BookItem bookItem) {
        Intent bookIntent = new Intent(this, BookDetailActivity.class);
        // pass the book for sale information to detail activity
        bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);
        startActivity(bookIntent);
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
}
