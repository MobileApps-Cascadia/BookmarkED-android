package edu.cascadia.bookmarked;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.ListFragment;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

//import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p>
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class BookListFragment extends ListFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "UserID";

    private String listType;

    private BooksAdapter listAdapter;

    private OnFragmentInteractionListener mListener;
    // Progress Dialog Object
    private ProgressDialog prgDialog;

    class BookForSaleComp implements Comparator<BookForSale> {

        @Override
        public int compare(BookForSale bookForSale1, BookForSale bookForSale2) {
            return bookForSale2.getCreatedDate().compareTo(bookForSale1.getCreatedDate());
        }
    }


    class BookWantedComp implements Comparator<BookWanted> {

        @Override
        public int compare(BookWanted bookWanted1, BookWanted bookWanted2) {
            return bookWanted2.getCreatedDate().compareTo(bookWanted1.getCreatedDate());
        }
    }

    private BookListItem bookListItem;
    //private String serverURI;

    public static BookListFragment newInstance(String param1, String param2) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookListFragment() {
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onMyPostingBookClicked(bookListItem.ITEMS.get(position), listType);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        if (getArguments() != null) {
            listType = getArguments().getString(ARG_PARAM1);
            //userID = getArguments().getString(ARG_PARAM2);
        }

        if (!isNetworkAvailable()) {
            Utility.beep();
            showNoNetworkWarning();
            return;
        }

        // load data and call web service if first time get here
        if (listAdapter == null) {
            bookListItem = new BookListItem();

            listAdapter = new BooksAdapter(getActivity(), (ArrayList<BookItem>) bookListItem.ITEMS);

            listAdapter.setForSaleListItem(listType.contains("sell"));

            setListAdapter(listAdapter);
//            if (listType.startsWith("my")) {
//                getBooks();
//            } else {
//                invokeWS();
//            }

            getBooks();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoNetworkWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("No network connection!");

        // set dialog message
        alertDialogBuilder
                .setMessage("Please connect to the network and refresh the list")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // do nothing
                            }
                        }
                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            //serverURI = Utility.getServerAddress(activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onMyPostingBookClicked(BookItem bookItem, String listType);
    }

    private void getBooksForSale() {
        prgDialog.show();

        Firebase bookForSaleRef = FBUtility.getInstance().getFirebaseRef().child("bookForSale");
        bookForSaleRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listAdapter.clear();
                System.out.println("*** Book for sale data changed. # of record:" + snapshot.getChildrenCount());
                List<BookForSale> al = new ArrayList<BookForSale>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    BookForSale bookForSale = data.getValue(BookForSale.class);
                    //bookForSale.setKey(data.getKey());
                    al.add(bookForSale);
                }
                Collections.sort(al, new BookForSaleComp());

                for (BookForSale bookForSale : al) {
                    Gson gson = new GsonBuilder().create();
                    String tmpStr = gson.toJson(bookForSale);
                    try {
                        BookItem bookItem = new BookItem(bookForSale.getIsbn(), bookForSale.getTitle(), tmpStr);
                        listAdapter.add(bookItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                prgDialog.hide();
            }

            @Override
            public void onCancelled(FirebaseError error) {
                prgDialog.hide();
            }

        });
    }

    private void getBooksWanted() {
        prgDialog.show();

        Firebase bookWantedRef = FBUtility.getInstance().getFirebaseRef().child("bookWanted");
        bookWantedRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //System.out.println("*** Book wanted data changed. # of record:" + snapshot.getChildrenCount());
                listAdapter.clear();
                //System.out.println(snapshot.getValue());
                List<BookWanted> al = new ArrayList<BookWanted>();
                for(DataSnapshot data : snapshot.getChildren()) {
                    BookWanted bookWanted = data.getValue(BookWanted.class);
                    //bookWanted.setKey(data.getKey());
                    al.add(bookWanted);
                }
                // sort the list by created date in descending order
                Collections.sort(al, new BookWantedComp());

                for(BookWanted bookWanted : al) {
                    Gson gson = new GsonBuilder().create();
                    String tmpStr = gson.toJson(bookWanted);
                    try {
                        BookItem bookItem = new BookItem(bookWanted.getIsbn(), bookWanted.getTitle(), tmpStr);
                        listAdapter.add(bookItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                prgDialog.hide();
            }

            @Override
            public void onCancelled(FirebaseError error) {
                prgDialog.hide();
            }
        });

    }


    private void getMyBooksForSale() {
        prgDialog.show();

        Firebase bookForSaleRef = FBUtility.getInstance().getFirebaseRef().child("bookForSale");
        String userId = FBUtility.getInstance().getAuthenticatedData().getUid();
        Query queryRef = bookForSaleRef.orderByChild("userId").equalTo(userId);
        queryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listAdapter.clear();
                System.out.println("*** MyBook for sale data changed. # of record:" + snapshot.getChildrenCount());

                List<BookForSale> al = new ArrayList<BookForSale>();
                for(DataSnapshot data : snapshot.getChildren()) {
                    BookForSale bookForSale = data.getValue(BookForSale.class);
                    //bookForSale.setKey(data.getKey());
                    al.add(bookForSale);
                }
                Collections.sort(al, new BookForSaleComp());

                for(BookForSale bookForSale : al) {
                    Gson gson = new GsonBuilder().create();
                    String tmpStr = gson.toJson(bookForSale);
                    try {
                        BookItem bookItem = new BookItem(bookForSale.getIsbn(), bookForSale.getTitle(), tmpStr);
                        listAdapter.add(bookItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                prgDialog.hide();
            }

            @Override
            public void onCancelled(FirebaseError error) {
                prgDialog.hide();
            }

        });
    }

    private void getMyBooksWanted() {
        prgDialog.show();

        Firebase bookWantedRef = FBUtility.getInstance().getFirebaseRef().child("bookWanted");
        String userId = FBUtility.getInstance().getAuthenticatedData().getUid();
        Query queryRef = bookWantedRef.orderByChild("userId").equalTo(userId);
        queryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listAdapter.clear();
                System.out.println("*** MyBook wanted data changed. # of record:" + snapshot.getChildrenCount());
                // System.out.println(snapshot.getValue());
                List<BookWanted> al = new ArrayList<BookWanted>();
                for(DataSnapshot data : snapshot.getChildren()) {
                    BookWanted bookWanted = data.getValue(BookWanted.class);
                    //bookWanted.setKey(data.getKey());
                    al.add(bookWanted);
                }
                // sort the list by created date in descending order
                Collections.sort(al, new BookWantedComp());

                for(BookWanted bookWanted : al) {
                    Gson gson = new GsonBuilder().create();
                    String tmpStr = gson.toJson(bookWanted);
                    try {
                        BookItem bookItem = new BookItem(bookWanted.getIsbn(), bookWanted.getTitle(), tmpStr);
                        listAdapter.add(bookItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                prgDialog.hide();
            }

            @Override
            public void onCancelled(FirebaseError error) {
                prgDialog.hide();
            }
        });

    }

    private void getBooks() {

        if (listType.equals("sell-view")) {
            getBooksForSale();
        } else if (listType.equals("wanted-view")) {
            getBooksWanted();
        } else if (listType.startsWith("my-buy")) {
            getMyBooksWanted();
        } else if (listType.startsWith("my-sell")) {
            getMyBooksForSale();
        }
    }

    private void addBookToAdapter(JSONObject jsonObject) throws ParseException {

        try {
            String isbn = jsonObject.getString("isbn");
            String title = jsonObject.getString("title");

            BookItem bookItem = new BookItem(isbn, title, jsonObject.toString());

            listAdapter.add(bookItem);
        } catch (JSONException e) {
            System.out.println("Exception in addBookToAdapter. e:" + e.getMessage());
            e.printStackTrace();
        }

    }

    public void refreshList() {
        // for now, just reload the data from backend
        // should be optimized later by adding only to the adapter
        // don't need to refresh in firebase???
        //listAdapter.clear();
        //getBooks();
    }
}
