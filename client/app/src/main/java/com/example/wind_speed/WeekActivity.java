package com.example.wind_speed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class WeekActivity extends AppCompatActivity {

    Button backButton;
    private String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weekly_data);

        Intent intent = getIntent();
        userName = intent.getExtras().getString("username");
        String[] day = intent.getExtras().getStringArray("day");
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
}
