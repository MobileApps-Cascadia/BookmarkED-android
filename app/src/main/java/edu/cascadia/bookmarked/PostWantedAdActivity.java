package edu.cascadia.bookmarked;

/**
 * Created by Hiromi on 6/10/2015.
 */


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

//public class PostWantedAd {
//}

public class PostWantedAdActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_wanted_ad);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_wanted_ad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Adding postAd method to collect data from Post Wanted Ad form
    public void postAd(View button) {
        final EditText titleField = (EditText) findViewById(R.id.editTextBookTitle);
        String booktitle = titleField.getText().toString();

        final EditText authorField = (EditText) findViewById(R.id.editTextBookAuthor);
        String bookauthor = authorField.getText().toString();

        final EditText detailsField = (EditText) findViewById(R.id.editTextBookDetails);
        String bookdetails = detailsField.getText().toString();
    }
}