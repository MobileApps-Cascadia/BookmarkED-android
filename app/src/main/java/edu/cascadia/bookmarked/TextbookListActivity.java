package edu.cascadia.bookmarked;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Arrays;
import android.widget.ListView;

/**
 * Created by DebraW on 6/11/2015.
 */


public class TextbookListActivity extends Activity {


    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_list);
        addListenerOnButtonBuy();


        mainListView = (ListView) findViewById( R.id.mainListView );


        // Create and populate a List of book names.
        String[] books = new String[] { "Mobile Application Development", "C# Advanced", "Calculus", "Java Programming",
                "JavaScript", "English Composition", "Database Introduction"};
        ArrayList<String> bookList = new ArrayList<String>();
        bookList.addAll( Arrays.asList(books) );

        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, bookList);
        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter(listAdapter );
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
