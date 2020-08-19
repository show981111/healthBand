package com.hanium.healthband.postData;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.hanium.healthband.LoginActivity;
import com.hanium.healthband.model.User;
import com.hanium.healthband.recyclerView.guardiansListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class postGuardian extends AsyncTask<String, Void, User> {

    private String wearerID;
    private String protectorID;
    private Context c;

    private ArrayList<User> linkedUserArrayList;
    private guardiansListAdapter guardiansListAdapter;

    public postGuardian(Context c, String wearerID, String protectorID,ArrayList<User> linkedUserArrayList, guardiansListAdapter guardiansListAdapter ) {
        this.c = c;
        this.wearerID = wearerID;
        this.protectorID = protectorID;
        this.linkedUserArrayList =linkedUserArrayList;
        this.guardiansListAdapter = guardiansListAdapter;
    }

    @Override
    protected User doInBackground(String... strings) {
        String url = strings[0];

        if(TextUtils.isEmpty(wearerID) && TextUtils.isEmpty(protectorID) ){
            return null;
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("wearer", wearerID)
                .add("protector", protectorID)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("postGuardian", e.getMessage());
            return null;
        }
        String jsonData;
        try {
            jsonData = response.body().string();
            JSONObject responseObject = new JSONObject(jsonData);

            if(responseObject.getString("status").equals("success")) {
                JSONObject userDataObject = responseObject.getJSONObject("userdata");

                String username = userDataObject.getString("username");
                String name = userDataObject.getString("name");
                String user_type = userDataObject.getString("user_type");
                String phone_number = userDataObject.getString("phone_number");

                User addedGuardian = new User(username, name, phone_number, user_type);
                return addedGuardian;
            }else{
                return null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(User user) {
        super.onPostExecute(user);
        if(user != null){
            linkedUserArrayList.add(user);
            guardiansListAdapter.notifyDataSetChanged();
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage("성공적으로 추가되었습니다!")
                    .setNegativeButton("확인", null)
                    .create()
                    .show();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage("등록되지 않은 사용자입니다!")
                    .setNegativeButton("확인", null)
                    .create()
                    .show();
        }
    }
}
