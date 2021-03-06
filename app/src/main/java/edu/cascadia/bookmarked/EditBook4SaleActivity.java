package edu.cascadia.bookmarked;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by seanchung on 11/18/15.
 */
public class EditBook4SaleActivity extends BookDetailActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookAction = getIntent().getStringExtra(getString(R.string.book_action_param));
        jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));

        initComponents();

        setTitle("Edit book for sale");
        populateFields(jsonString);
        //findViewById(R.id.contactSellerButton).setVisibility(View.GONE);
        //hideContactSellerButton();

        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_book, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_post_book) {
            requestUpdateBook4Sale();
            //Toast.makeText(this, "To save changes", Toast.LENGTH_LONG).show();
            return true;
        } else if (item.getItemId() == R.id.action_cancel) {
            // To do: add confirmation to cancel and loose data
            super.onBackPressed();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void finish() {
//        Intent data = new Intent();
//        data.putExtra("NewPosting", newUpdate);
//        data.putExtra("updateJson", jsonString);
//
//        setResult(RESULT_OK, data);
//
//        super.finish();
//    }

}
