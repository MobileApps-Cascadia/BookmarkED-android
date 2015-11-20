package edu.cascadia.bookmarked;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import static android.R.color.holo_green_light;

public class MyPostingActivity extends AppCompatActivity implements BookListFragment.OnFragmentInteractionListener {

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = getIntent().getStringExtra("UserID");
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
//            }
//        });

        insertBookListFragments();
    }

    private void insertBookListFragments() {

        // insert book for sale list view
        BookListFragment itemFragment = BookListFragment.newInstance("my-sell-list", userID);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, itemFragment);
        fragmentTransaction.commit();

        // insert book wanted list view
        BookListFragment itemFragment2 = BookListFragment.newInstance("my-buy-list", userID);

        FragmentManager fm2 = getFragmentManager();
        FragmentTransaction fragmentTransaction2 = fm.beginTransaction();
        fragmentTransaction2.replace(R.id.fragment_container2, itemFragment2);
        fragmentTransaction2.commit();
    }

    @Override
    public void onMyPostingBookClicked(BookItem bookItem, String listType) {
        //Toast.makeText(this,"To display book:" + bookItem.isbn,Toast.LENGTH_SHORT).show();
        Intent bookIntent = new Intent(this, BookDetailActivity.class);
        // pass the book for sale information to detail activity
        bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);
        bookIntent.putExtra("BookAction", "AllowEdit");

        startActivity(bookIntent);
    }
}
