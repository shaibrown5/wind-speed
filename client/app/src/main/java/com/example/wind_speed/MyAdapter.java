package com.example.wind_speed;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<com.example.wind_speed.MyAdapter.MyViewHolder> {

    private String[] m_day;
    private String[] m_windSpeed;
    private String[] m_windDeg;
    private String[] m_temp;
    Context context;

    public MyAdapter(Context ct, String[] day, String[] windSpeed, String[] windDeg, String[] temp){
        context = ct;
        m_day = day;
        m_windSpeed = windSpeed;
        m_windDeg = windDeg;
        m_temp = temp;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.day_row_card, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.dateText.setText("date " + m_day[position].split(",")[0]);
        holder.windSpeedtxt.setText("wind speed " + m_windSpeed[position]);
        holder.windDegtxt.setText("wind deg " + m_windDeg[position]);
        holder.tempTxt.setText("temp " + m_temp[position]);
    }

    @Override
    public int getItemCount() {
        return m_day.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView dateText;
        private TextView windSpeedtxt;
        private TextView windDegtxt;
        private TextView tempTxt;

        public MyViewHolder(@NonNull View view){
            super(view);
            dateText = view.findViewById(R.id.dateText);
            windSpeedtxt = view.findViewById(R.id.windSpeedText);
            windDegtxt = view.findViewById(R.id.windDegText);
            tempTxt = view.findViewById(R.id.tempText);
        }
    }
}
