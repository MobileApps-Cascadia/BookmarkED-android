package edu.cascadia.bookmarked;

/**
 * Created by DebraW on 6/10/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;


public class BuySearchBookActivity extends Activity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_search_book);
        initPostWantedAdButton();
      //  addListenerOnButtonTextbookList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_wanted_ad, menu);
        return true;
    }

    // Method for Post a Wanted Ad button
    private void initPostWantedAdButton(){
        Button adButton = (Button) findViewById(R.id.buttonPostAd);
        adButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(BuySearchBookActivity.this, PostWantedAdActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });
    }
}
