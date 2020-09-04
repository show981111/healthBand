package com.hanium.healthband.postData;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.hanium.healthband.model.SensorData;
import com.hanium.healthband.model.User;
import com.hanium.healthband.recyclerView.guardiansListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class postSensorData extends AsyncTask<String, Void, String> {


    private String token;

    private SensorData sensorData;

    public postSensorData(String token, SensorData sensorData) {
        this.token = token;
        this.sensorData = sensorData;
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];

        if(TextUtils.isEmpty(token)){
            return null;
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("heartRate", sensorData.getHeartRate())
                .add("humid", sensorData.getHumidity())
                .add("temp", sensorData.getTemperature())
                .add("stepCount", sensorData.getSteps())
                .add("sound", sensorData.getSound())
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Token " + token)
                .url(url)
                .post(formBody)
                .build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            String res = response.body().string();
            return  res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);

    }
}
