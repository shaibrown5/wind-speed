package com.example.wind_speed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class WeekActivity extends AppCompatActivity {

    Button backButton;
    private String userName;
    private RequestQueue m_queue;
    private static final String TAG = "week-activity";
    private static final String m_REQUEST_URL = "http://10.0.2.2:8080/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weekly_data);
        m_queue = Volley.newRequestQueue(this);

        //get user name
        Intent intent = getIntent();
//        userName = intent.getExtras().getString("username");
//        lon = intent.getExtras().getDouble("lon");
//        lat = intent.getExtras().getDouble("lat");
//        Log.d(TAG, "username is : " + userName);
//        Log.d(TAG, "lon is : " + lon);
//        Log.d(TAG, "lat is : " + lat);

        String[] day = intent.getExtras().getStringArray("day");
        Log.d("WEEK ACTIVITY", "[THE STRING ARR DAY" + day[0].split(",")[0] + "  " +day[2]);
        String[] windSpeed = intent.getExtras().getStringArray("windSpeed");
        String[] windDeg = intent.getExtras().getStringArray("windDeg");
        String[] temp = intent.getExtras().getStringArray("temp");

        RecyclerView rView = (RecyclerView)findViewById(R.id.recView);
        MyAdapter myAdapter = new MyAdapter(this, day, windSpeed, windDeg, temp);
        rView.setAdapter(myAdapter);
        rView.setLayoutManager(new LinearLayoutManager(this));


        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                intent.putExtra("username",userName);
                startActivity(intent);
            }
        });
    }

//    private void getWeekData(){
//        JSONObject requestObject = new JSONObject();
//
//        try {
//            requestObject.put("lat", lat);
//            requestObject.put("lon", lon);
//        }
//        catch (JSONException e) {
//            Log.e(TAG, "[ERROR] adding lat and long");
//        }
//
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,  m_REQUEST_URL + userName + "/forecast",
//                requestObject, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                // response: D/week-activity: [RESPONSE] {"day_1":"3\/6\/2021, 11:00:00 AM","wind_speed_1":4.23,"wind_deg_1":1,"temp_1":17.3,"day_2":"3\/7\/2021, 11:00:00 AM","wind_speed_2":2.99,"wind_deg_2":337,"temp_2":17.4,"day_3":"3\/8\/2021, 11:00:00 AM","wind_speed_3":3.35,"wind_deg_3":244,"temp_3":17.01,"day_4":"3\/9\/2021, 11:00:00 AM","wind_speed_4":2.34,"wind_deg_4":35,"temp_4":18.84,"day_5":"3\/10\/2021, 11:00:00 AM","wind_speed_5":6.16,"wind_deg_5":180,"temp_5":23.52,"day_6":"3\/11\/2021, 11:00:00 AM","wind_speed_6":10.35,"wind_deg_6":267,"temp_6":17.97,"day_7":"3\/12\/2021, 11:00:00 AM","wind_speed_7":4.32,"wind_deg_7":322,"temp_7":16.05}
//                //"day_1"  :"3\/6\/2021, 11:00:00 AM",  "wind_speed_1":4.23,  "wind_deg_1":1,  "temp_1":17.3
//                Log.i(TAG, "[RESPONSE] Post went through");
//                Log.d(TAG, "[RESPONSE] " + response.toString());
//                String[] day = new String[8];
//                String[] windSpeed = new String[8];
//                String[] windDeg = new String[8];
//                String[] temp = new String[8];
//
//                try{
//                    for (int i = 1; i < 9 ; i++) {
//                        day[i-1] = response.getString(("day_" + i).split(",")[0]);
//                        windSpeed[i-1] = response.getString("wind_speed_" + i);
//                        windDeg[i-1] = response.getString("wind_deg_" + i);
//                        temp[i-1] = response.getString("temp_" + i);
//                    }
//
//                    RecyclerView rView = (RecyclerView)findViewById(R.id.weekList);
//                    MyAdapter myAdapter = new MyAdapter(WeekActivity.this, day, windSpeed, windDeg, temp);
//                    rView.setAdapter(myAdapter);
//                    rView.setLayoutManager(new LinearLayoutManager(WeekActivity.this));
//
//                }
//                catch (JSONException js){
//                    js.printStackTrace();
//                }
//
//
//
//            }
//        },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, "[RESPONSE ERROR] Failed to get information- " + error);
//                    }
//                });
//
//        m_queue.add(req);
//    }
}
