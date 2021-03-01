package com.example.wind_speed;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerLocation;
    private static final String TAG = "home_page";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        spinnerLocation = findViewById(R.id.locationSpin);
        spinnerLocation.setOnItemSelectedListener(this);

        String[] preDefLocations = getResources().getStringArray(R.array.pre_def_locations);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, preDefLocations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLocation.setAdapter(adapter);
    }

    /**
     * This method is activated when an item is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.locationSpin){
            String chosenLocation = parent.getItemAtPosition(position).toString();
            Log.i(TAG, "the locations is: " + chosenLocation);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "in nothing selected method");
    }
}
