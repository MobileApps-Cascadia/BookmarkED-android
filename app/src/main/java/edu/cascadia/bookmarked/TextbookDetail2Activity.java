package edu.cascadia.bookmarked;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by Hiromi on 6/12/2015.
 */

public class TextbookDetail2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_detail2);
        addListenerOnButtonTextbookList();
        addListenerOnButtonBuy();
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



    // Buy Book navigation button
    public void addListenerOnButtonBuy() {
        ImageView btnBuy2 = (ImageView)findViewById(R.id.imageButtonBuy);
        btnBuy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentbuy2 = new Intent(TextbookDetail2Activity.this, BuySearchBookActivity.class);
                startActivity(intentbuy2);
            }
        });
    }

    // Wanted Book List navigation button
    public void addListenerOnButtonTextbookList() {
        ImageView btnList2 = (ImageView)findViewById(R.id.imageButtonBookWanted);
        btnList2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentbooklist2 = new Intent(TextbookDetail2Activity.this, TextbookListActivity.class);
                startActivity(intentbooklist2);
            }
        });
    }
}