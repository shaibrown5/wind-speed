package com.example.wind_speed;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HomePage extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {

    Button logOut;
    EditText setPoint;
    Button windpeedButton;
    private GoogleMap mMap;
    private Spinner spinnerLocation;
    private static final String TAG = "home_page";
    private HashMap<String, double[]> map = new HashMap<String, double[]>();
    private static final String SERVER_ADDRESS = "http://10.0.2.2:8080/";
    private static  String USERNAME;
    private RequestQueue _queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        //insert lat and long
        map.put("Sydney",new double[]{-34, 151});
        map.put("Bali",new double[]{-8.319837279796443, 115.09333358682619});
        map.put("Rio",new double[]{-22.84630623087871, -43.34842123862497});
        map.put("Tel Aviv",new double[]{32.08889403117533, 34.744188917796855});

        //init RequestQueue
        _queue = Volley.newRequestQueue(this);

        //get user name
        Intent i = getIntent();
        USERNAME = i.getExtras().getString("username");
        Log.d(TAG, "username is : " + USERNAME);

        //init edit Text of set point value
        setPoint = (EditText) findViewById(R.id.setPoint);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        spinnerLocation = findViewById(R.id.locationSpin);
        spinnerLocation.setOnItemSelectedListener(this);

        String[] preDefLocations = getResources().getStringArray(R.array.pre_def_locations);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, preDefLocations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLocation.setAdapter(adapter);

        logOut = (Button) findViewById(R.id.logOutButton);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        //*** init thw wind get nonfiction button
        // if the value is valid, send a request to the server
        //else, rise a toast pop up
        windpeedButton = (Button) findViewById(R.id.winspeedButton);
        windpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String setPointString = setPoint.getText().toString();
                if (!setPointString.isEmpty() && setPointString.matches("-?(0|[1-9]\\d*)")){
                    Log.d(TAG,"set point value is " + setPointString );
                    int setPointint = Integer.parseInt(setPointString);
                    startCheck(setPointint,spinnerLocation.getSelectedItem().toString());
                }
                else {
                    Log.d(TAG,"enter a valid Set Point value");
                    Toast.makeText(HomePage.this, "enter setPoint value", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //firebase request to get token of the user
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "user token - " + token);
                    }
                });
    }

    /**
     * This method activtes when an item is changed in the list.
     * the relevant info is then retrieved
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.locationSpin){
            String chosenLocation = parent.getItemAtPosition(position).toString();
            Log.i(TAG, "the locations is: " + chosenLocation);
            Log.i(TAG, "lat and lon is: " + map.get(chosenLocation)[0]+" "+map.get(chosenLocation)[1]);
            LatLng pos = new LatLng(map.get(chosenLocation)[0], map.get(chosenLocation)[1]);
            mMap.clear();
            //mMap.addMarker(new MarkerOptions().position(pos).title("Marker in "+chosenLocation));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("lat", map.get(chosenLocation)[0]);
                requestObject.put("lon", map.get(chosenLocation)[1]);
            }
            catch (JSONException e) {}
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + USERNAME + "/now", requestObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG,  response.toString());
                            String Temp = "";
                            String windSpeed = "";
                            String windDeg = "";
                            try {
                                 Temp = response.getString("temp");
                                 JSONObject js = response.getJSONObject("wind");
                                 windSpeed = js.getString("speed");
                                 windDeg = js.getString("deg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG,  "temp is :" +Temp +" " + windSpeed + " " + windDeg );

                            MarkerOptions m = new MarkerOptions();
                            mMap.addMarker(new MarkerOptions().position(pos).title( windSpeed +"," + windDeg +","+ Temp));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Failed to send start notification pusher - " + error);
                            mMap.addMarker(new MarkerOptions().position(pos).title("Marker in "+chosenLocation));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                        }
                    });
            req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            _queue.add(req);

        }



    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "in nothing selected method");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow to be display on the marker
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);

                // Getting the position from the marker
                LatLng latLng = arg0.getPosition();
                String title = arg0.getTitle();
                List<String> items = Arrays.asList(title.split("\\s*,\\s*"));

                // Getting reference to the TextView to set wind speed
                TextView windSpeed = (TextView) v.findViewById(R.id.wind_speed);

                // Getting reference to the TextView to set wind deg
                TextView windDeg = (TextView) v.findViewById(R.id.wind_deg);

                // Getting reference to the TextView to set wind deg
                TextView temp = (TextView) v.findViewById(R.id.temp);

                // Setting the windSpeed
                windSpeed.setText("Wind Speed is :" + items.get(0));

                // Setting the windDeg
                windDeg.setText("Wind deg is :"+ items.get(1));

                // Setting the temperature
                temp.setText("Temperature is :"+ items.get(2));

                // Returning the view containing InfoWindow contents
                return v;

            }
        });
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
        this method get a set point value and a Location
        and send a request to the server to check periodically
        if the wind speed is greater then the set point value
    * */
    private void startCheck(int setPoint, String chosenLocation){
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("lat", map.get(chosenLocation)[0]);
            requestObject.put("lon", map.get(chosenLocation)[1]);
            requestObject.put("setPoint", setPoint);
        }
        catch (JSONException e) {}
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + USERNAME + "/start", requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG,  response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Failed to send start notification pusher - " + error);
                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        _queue.add(req);

    }
}
