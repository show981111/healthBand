package com.hanium.healthband.postData;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class postToken extends AsyncTask<String, Void, String> {

    private String token;
    private String fcmToken;
    private String username;
    private String TAG = "POST_TOKEN";

    public postToken(String token, String fcmToken, String username) {
        this.token = token;
        this.fcmToken = fcmToken;
        this.username = username;
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        Log.w(TAG, token );
        if(TextUtils.isEmpty(token)){
            return null;
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("fcm_token", fcmToken)
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Token " + token)
                .url(url)
                .put(formBody)
                .build();
        Response response;
        try {
            Log.w(TAG, "sending");
            response = okHttpClient.newCall(request).execute();
            String res = response.body().string();
            return  res;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "error");
            return null;
        }
    }

    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);
        Log.w(TAG , "result "+res);
    }
}
