package com.hanium.healthband.postData;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.hanium.healthband.model.SensorData;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class postEvent extends AsyncTask<String, Void, String> {


    private String token;
    private String eventInvoked;

    public postEvent(String token, String eventInvoked) {
        this.token = token;
        this.eventInvoked = eventInvoked;
    }


    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        Log.w("post sensor res", token );
        if(TextUtils.isEmpty(token)){
            return null;
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("fallEvent", eventInvoked)
                .add("heartEvent", "F")
                .add("heatIllEvent", "N")
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Token " + token)
                .url(url)
                .post(formBody)
                .build();
        Response response;
        try {
            Log.w("post sensor res", "sending");
            response = okHttpClient.newCall(request).execute();
            String res = response.body().string();
            return  res;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("post sensor res", "error");
            return null;
        }
    }

    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);
        Log.w("post sensor res", "result "+res);
    }
}
