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

    private boolean newUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("***in EditBook4SaleActivity.onCreate() ***");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookAction = getIntent().getStringExtra("BookAction");
        jsonString = getIntent().getStringExtra(getString(R.string.book_info_param));

        initComponents();

        setTitle("Edit book for sale");
        populateFields(jsonString);
        //findViewById(R.id.contactSellerButton).setVisibility(View.GONE);
        hideContactSellerButton();
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
            // TODO: need to get the updated book_for_sale record and pass the
            // result back to the previous activity so that we can just update one
            // entry in the adapter.
            newUpdate = true;   // temporary
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_cancel) {
            // To do: add confirmation to cancel and loose data
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("NewPosting", newUpdate);
        data.putExtra("updateJson", jsonString);

        setResult(RESULT_OK, data);

        super.finish();
    }

}
