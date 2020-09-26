package com.hanium.healthband;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.hanium.healthband.Api.API;
import com.hanium.healthband.fetchData.fetchStatList;
import com.hanium.healthband.model.Stat;
import com.hanium.healthband.model.User;


public class ChartActivity extends AppCompatActivity {

    private TextView tv_min;
    private TextView tv_max;
    private String token;
    private TextView tv_mean;
    private String sensorType;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        tv_min = findViewById(R.id.tv_min);
        tv_max = findViewById(R.id.tv_max);
        tv_mean = findViewById(R.id.tv_mean);
        TextView tv_title = findViewById(R.id.tv_chartTitle);
        String label = "";
        Intent getIntent = getIntent();
        if(getIntent != null) {
            token = getIntent.getStringExtra("token");
            user = getIntent.getParcelableExtra("user");
            sensorType = getIntent.getStringExtra("sensorType");
            if(sensorType != null && sensorType.equals("heartRate")){
                label = "심박수";
            }else{
                label = "주변 소음";
            }
            String title = " ";
            if(user != null){
                title = user.getName() + "님의 "+ label+ "차트";
            }
            tv_title.setText(title);
        }


        CombinedChart chart = findViewById(R.id.combined_chart);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);

        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        });

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setAxisMinimum(-0.25f);
        xAxis.setGranularity(1f);


        fetchStatList fetchStatList = new fetchStatList(sensorType,chart, xAxis, token,user.getUsername() );
        fetchStatList.execute(API.GetSensor);

        if(label.equals("심박수")){
            fetchStatList.execute(API.HEART);
        }else if(label.equals("주변 소음")){
            fetchStatList.execute(API.SOUND);
        }



        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                Stat charData = (Stat) e.getData();
                tv_max.setText(String.valueOf(charData.getMax()));
                tv_min.setText(String.valueOf(charData.getMin()));
                tv_mean.setText(String.valueOf(charData.getMean()));

            }

            @Override
            public void onNothingSelected() {

            }
        });

    }

}
