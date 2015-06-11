package edu.cascadia.bookmarked;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by DebraW on 6/11/2015.
 */


public class TextbookListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_list);
        addListenerOnButtonBuy();
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


    public void addListenerOnButtonBuy() {


        ImageView btnBuy4 = (ImageView)findViewById(R.id.imageButtonBuy2);

        btnBuy4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentbuy4 = new Intent(TextbookListActivity.this, BuySearchBookActivity.class);
                startActivity(intentbuy4);

            }
        });
    }

}
