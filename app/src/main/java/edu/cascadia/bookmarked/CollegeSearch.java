package edu.cascadia.bookmarked;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Toast;

public class CollegeSearch extends Activity {

private Spinner spinner1;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_search);
        addListenerOnButton();
        addListenerOnButtonBuy();
        addListenerOnButtonTextbookList();
    }


   /* public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new MyOnItemSelectedListener());
    }*/

    //This method is created for the mouse click event which will take us to the next activity.
public void addListenerOnButton() {

    spinner1 = (Spinner)findViewById(R.id.spinner1);
    btnSubmit = (Button)findViewById(R.id.btnSubmit);

    btnSubmit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Toast.makeText(CollegeSearch.this, "Result: " + "\nSpinner 1: " + String.valueOf(spinner1.getSelectedItem()), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CollegeSearch.this, BuySearchBookActivity.class);
            startActivity(intent);

        }
    });
}

    public void addListenerOnButtonBuy() {


        ImageView btnBuy = (ImageView)findViewById(R.id.imageButtonBuy1);

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentbuy = new Intent(CollegeSearch.this, BuySearchBookActivity.class);
                startActivity(intentbuy);

            }
        });
    }


    public void addListenerOnButtonTextbookList() {


        ImageView btnList = (ImageView)findViewById(R.id.imageButtonBookWanted1);

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentbooklist = new Intent(CollegeSearch.this, TextbookListActivity.class);
                startActivity(intentbooklist);

            }
        });
    }


}