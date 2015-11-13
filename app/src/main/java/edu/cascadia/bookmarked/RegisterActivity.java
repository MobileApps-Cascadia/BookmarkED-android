package edu.cascadia.bookmarked;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private final static String registerURI = "bookmarked/register/doregister";

    // Progress Dialog Object
    private ProgressDialog prgDialog;
    // Error Msg TextView Object
    private TextView errorMsgTextView;
    // First name Edit View Object
    private EditText firstnameEditText;
    // Last name Edit View Object
    private EditText lastnameEditText;
    // Email Edit View Object
    private EditText emailEditText;
    // Phone Edit View Object
    private EditText phoneEditText;
    // Passwprd Edit View Object
    private EditText pwdEditText;
    private EditText zipcodeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        initComponents();
    }

    private void initComponents() {
        // Find Error Msg Text View control by ID
        errorMsgTextView = (TextView) findViewById(R.id.register_error);
        // Find Name Edit View control by ID
        firstnameEditText = (EditText) findViewById(R.id.registerFirstName);
        // Find Name Edit View control by ID
        lastnameEditText = (EditText) findViewById(R.id.registerLastName);
        // Find Email Edit View control by ID
        emailEditText = (EditText) findViewById(R.id.registerEmail);
        // Find phone Edit View control by ID
        phoneEditText = (EditText) findViewById(R.id.registerPhone);
        // Find Password Edit View control by ID
        pwdEditText = (EditText) findViewById(R.id.registerPassword);
        zipcodeEditText = (EditText) findViewById(R.id.registerZipcode);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }

    //retrieve user current zipcode
    @TargetApi(Build.VERSION_CODES.M)
    public void fetchZipcode(View view) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                String message = String.format(
                        "New working Location \n Longitude: %1$s \n Latitude: %2$s",
                        location.getLongitude(), location.getLatitude()
                );
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        try {
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            // Or use LocationManager.GPS_PROVIDER

//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//            } else {
//                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//                String message = String.format(
//                        "New Location \n Longitude: %1$s \n Latitude: %2$s",
//                        lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude()
//                );
//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//            }

                //

         } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Location permission not enabled", Toast.LENGTH_SHORT).show();
        }

        //zipcodeEditText.setText("98012");
    }


    public void registerUser(View view) {
        String firstname = firstnameEditText.getText().toString();
        String lastname = lastnameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String password = pwdEditText.getText().toString();
        String zipcode = zipcodeEditText.getText().toString();
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if(Utility.isNotNull(firstname) && Utility.isNotNull(firstname) &&
                Utility.isNotNull(email) && Utility.isNotNull(password)){
            // When Email entered is Valid
            if(Utility.validate(email)){
                if(Utility.validateZipcode(zipcode)){
                    // clear error message, in case there was a message previously
                    errorMsgTextView.setText("");
                    // Put Http parameter name with value of Name Edit View control
                    params.put("name", firstname + lastname);
                    // Put Http parameter username with value of Email Edit View control
                    params.put("username", email);
                    // Put Http parameter password with value of Password Edit View control
                    params.put("password", password);
                    params.put("zipcode", zipcode);
                    // Invoke RESTful Web Service with Http parameters
                    invokeWS(params);
                } else {
                    errorMsgTextView.setText("Please enter a valid zipcode");
                }
            }
            // When Email is invalid
            else{
                Utility.beep();
                errorMsgTextView.setText("Please enter valid email");
            }
        }
        // When any of the Edit View control left blank
        else{
            Utility.beep();
            Toast.makeText(getApplicationContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    private void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        String hostAddress = "http://" + Utility.getServerAddress(getApplicationContext()) + "/";

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(hostAddress + registerURI, params ,new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if(obj.getBoolean("status")){
                        // Set Default Values for Edit View controls
                        setDefaultValues();
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), "You are successfully registered!", Toast.LENGTH_SHORT).show();
                        // return to previous screen
                        onBackPressed();
                    }
                    // Else display error message
                    else {
                        errorMsgTextView.setText(obj.getString("error_msg"));
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }
            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_404_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.http_500_error), Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues(){
        firstnameEditText.setText("");
        lastnameEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        pwdEditText.setText("");
        zipcodeEditText.setText("");
    }

}
