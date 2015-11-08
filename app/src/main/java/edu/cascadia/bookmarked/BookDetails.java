package edu.cascadia.bookmarked;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;

public class BookDetails extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_details);


        GetServerData.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // Server Request URL
                String serverURL = "http://" + Utility.getServerAddress(getActivity()) + "/";

                // Create Object and call AsyncTask execute Method
                new LoadService().execute(serverURL);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Class with extends AsyncTask class
    private class LoadService extends AsyncTask<String, Void, Void> {

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;
        private final String TAG = null;
        String name = null;
        private ProgressDialog Dialog = new ProgressDialog(BookDetails.this);

       // TextView uiUpdate = (TextView) findViewById(R.id.textView2);

        protected void onPreExecute() {
            // NOTE: You can call UI Element here.

            // UI Element
           // uiUpdate.setText("");
            //Dialog.setMessage("Loading service..");
           // Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            try {

                // NOTE: Don't call UI Element here.

                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);

            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            // Close progress dialog
            Dialog.dismiss();
            Log.e(TAG, "------------------------------------- Output: "
                    + Content);
            try {

                // Load json data and display
                JSONObject json = new JSONObject(Content);

                TextView name = (TextView)findViewById(R.id.title);
                name.setText(json.getString("Title"));

                TextView email = (TextView)findViewById(R.id.isbn);
                email.setText(json.getString("isbn"));

                TextView status = (TextView)findViewById(R.id.author);
                status.setText(json.getString("Author"));

                TextView face = (TextView)findViewById(R.id.edition);
                face.setText(json.getString("edition"));

                TextView face = (TextView)findViewById(R.id.description);
                face.setText(json.getString("description"));

                TextView face = (TextView)findViewById(R.id.price);
                face.setText(json.getString("price"));

                RelativeLayout serv = (RelativeLayout)findViewById(R.id.serviced);
                serv.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

           // uiUpdate.setText("Raw Output : " + Content);
        }

    }
}