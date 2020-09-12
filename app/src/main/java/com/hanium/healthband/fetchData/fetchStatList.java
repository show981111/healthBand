package com.hanium.healthband.fetchData;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.hanium.healthband.GenerateData;
import com.hanium.healthband.model.Stat;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class fetchStatList extends AsyncTask<String, Void, ArrayList<Stat>> {

    //센서 데이터 종류, userID post
    //get

    private String sensorType;
    private CombinedChart chart;
    private XAxis xAxis;
    private String token;
    private String userID;

    private ArrayList<Stat> statArrayList = new ArrayList<>();

    public fetchStatList(String sensorType, CombinedChart chart, XAxis xAxis, String token, String userID) {
        this.sensorType = sensorType;
        this.chart = chart;
        this.xAxis = xAxis;
        this.token = token;
        this.userID = userID;
    }

    @Override
    protected ArrayList<Stat> doInBackground(String... strings) {

        String url = strings[0];

        if(TextUtils.isEmpty(token)&& TextUtils.isEmpty(sensorType) ){
            return null;
        }
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("wearerID", userID);
        httpBuilder.addQueryParameter("sensorName", sensorType);

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Authorization", "Token " + token)
                .url(httpBuilder.build())
                .build();

        Response response;
        try {
            response = okHttpClient.newCall(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("fetchStatList", e.getMessage());
            return null;
        }
        String jsonData;

        try {

            if(response.code() == 200) {
                jsonData = response.body().string();
                Log.d("fetchStatList", jsonData);
                JSONObject responseObject = new JSONObject(jsonData);
                Iterator<String> iter = responseObject.keys(); //This should be the iterator you want.
                while(iter.hasNext()){
                    String date = iter.next();
                    Log.d("fetchStatList", date);

                    JSONObject statObjectWithType = responseObject.getJSONObject(date);

                    JSONObject statObject = statObjectWithType.getJSONObject(sensorType);
                    float avg = (float) statObject.getInt("avg");
                    float max = (float) statObject.getInt("max");
                    float min = (float) statObject.getInt("min");
                    Log.d("fetchStatList", avg + " " + max + "  " + min);
                    Stat stat = new Stat(avg,max,min,date.substring(date.length() -4));

                    statArrayList.add(stat);
                }

                return statArrayList;
            }else{
                return null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(final ArrayList<Stat> stats) {
        super.onPostExecute(stats);
        if(stats != null){
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return stats.get((int) value).getDate();

                }
            });

            CombinedData data = new CombinedData();
            ArrayList<Entry> lineEntry = new ArrayList<>();
            ArrayList<CandleEntry> candleEntries = new ArrayList<>();

            for(int i = 0; i < stats.size(); i++){
                Log.d("fetchStatList",stats.get(i).getMax() + " " + stats.get(i).getMin() );
                Entry entry = new Entry(i,stats.get(i).getMean(),stats.get(i));
                CandleEntry candleEntry = new CandleEntry(i,stats.get(i).getMax(),stats.get(i).getMin(),
                        stats.get(i).getMax(),stats.get(i).getMin(), stats.get(i));
                lineEntry.add(entry);
                candleEntries.add(candleEntry);
            }
//        data.setData(generateLineData());
//        data.setData(generateCandleData());
            GenerateData generateData = new GenerateData();
            data.setData(generateData.generateLineData(lineEntry));
            String label = "데이터";
            switch (sensorType){
                case "heartRate":
                    label = "심박수";
                    break;
                case "temp":
                    label = "온도";
                    break;
                case "humid":
                    label = "습도";
                    break;

            }
            data.setData(generateData.generateCandleData(candleEntries, label));
            chart.setAutoScaleMinMaxEnabled(true);

            xAxis.setAxisMaximum(data.getXMax() + 0.25f);
            chart.setData(data);
            chart.invalidate();
        }
    }
}
