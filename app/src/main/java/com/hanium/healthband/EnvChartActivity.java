package com.hanium.healthband;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.common.api.Api;
import com.hanium.healthband.Api.API;
import com.hanium.healthband.fetchData.fetchStatList;
import com.hanium.healthband.model.Stat;
import com.hanium.healthband.model.User;


public class EnvChartActivity extends AppCompatActivity {

    private TextView tv_env_min;
    private TextView tv_env_max;
    private TextView tv_env_mean;
    private TableRow tr_env;

    private TextView tv_temp;
    private TextView tv_humid;

    private CombinedChart chart;
    private CombinedData data;

    private String token;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_env_chart);

        tv_env_min = findViewById(R.id.tv_env_min);
        tv_env_max = findViewById(R.id.tv_env_max);
        tr_env = findViewById(R.id.tr_env);
        tv_temp = findViewById(R.id.tv_temp);
        tv_humid = findViewById(R.id.tv_humid);
        tv_env_mean = findViewById(R.id.tv_env_mean);
        TextView tv_title = findViewById(R.id.tv_envchartTitle);

        Intent getIntent = getIntent();
        if(getIntent != null) {
            token = getIntent.getStringExtra("token");
            user = getIntent.getParcelableExtra("user") ;
            String title = " ";
            if(user!=null){
                title = user.getName() + "님의 온/습도 차트";
            }
            tv_title.setText(title);
        }



        chart = findViewById(R.id.env_combined_chart);
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

        final XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setAxisMinimum(-0.25f);
        xAxis.setGranularity(1f);


        data = new CombinedData();



        fetchStatList fetchStatList = new fetchStatList("tempHumid",chart, xAxis, token, user.getUsername(), "temp");
        fetchStatList.execute(API.GetSensor);

        tv_temp.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                Stat charData = (Stat) e.getData();
                tv_env_max.setText(String.valueOf(charData.getMax()));
                tv_env_min.setText(String.valueOf(charData.getMin()));
                tv_env_mean.setText(String.valueOf(charData.getMean()));

            }

            @Override
            public void onNothingSelected() {

            }
        });

        tv_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tv_humid.setBackgroundColor(Color.WHITE);
                fetchStatList fetchStatList = new fetchStatList("tempHumid",chart, xAxis, token, user.getUsername() , "temp");
                fetchStatList.execute(API.GetSensor);
            }
        });

        tv_humid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(chart.getData() != null)
                    chart.getData().clearValues();

//                chart.invalidate();
//                chart.clear();
//
//
//                data.clearValues();

                fetchStatList fetchStatList = new fetchStatList("tempHumid",chart, xAxis, token ,user.getUsername(), "humid");
                fetchStatList.execute(API.GetSensor);

//                data.setData(generateCandleData(CandleEntries));
//                //chart.invalidate();
//                chart.setData(data);
//                chart.notifyDataSetChanged();
//                chart.invalidate();
                tv_temp.setBackgroundColor(Color.WHITE);
                v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });


    }

}
