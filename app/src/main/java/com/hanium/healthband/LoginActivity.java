package com.hanium.healthband;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.hanium.healthband.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText et_loginEmail = findViewById(R.id.et_loginEmail);
        final EditText et_loginPW = findViewById(R.id.et_loginPassword);
        Button bt_loginWithID = findViewById(R.id.bt_login);
        Button bt_loginWithBluetooth = findViewById(R.id.bt_loginBluetooth);
        Button bt_register = findViewById(R.id.bt_loginRegister);

        bt_loginWithID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = et_loginEmail.getText().toString();
                String userPW = et_loginPW.getText().toString();
                LoginTask loginTask = new LoginTask(userID, userPW);
                loginTask.execute("http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/custom/login/");

            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(goToRegister);
            }
        });
    }

    class LoginTask extends AsyncTask<String, Void, User>{

        private String userID;
        private String userPW;
        private ArrayList<User> linkedUserArrayList = new ArrayList<>();
        private String message = "";

        public LoginTask(String userID, String userPW) {
            this.userID = userID;
            this.userPW = userPW;
        }

        @Override
        protected User doInBackground(String... strings) {
            String url = strings[0];

            if(TextUtils.isEmpty(userID)){
                return null;
            }
            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("username", userID)
                    .add("password", userPW)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            Response response = null;
            try {
                response = okHttpClient.newCall(request).execute();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("loginTask", e.getMessage());
                return null;
            }

            String jsonData = null;
            if(response != null ){
                try {
                    jsonData = response.body().string();
                    JSONObject responseObject = new JSONObject(jsonData);
                    Log.d("loginTask", jsonData);
                    if(responseObject.getString("status").equals("success")) {
                        String token = responseObject.getString("key");
                        JSONObject userDataObject = responseObject.getJSONObject("userdata");

                        String username = userDataObject.getString("username");
                        String name = userDataObject.getString("name");
                        String user_type = userDataObject.getString("user_type");
                        String phone_number = userDataObject.getString("phone_number");

                        JSONObject linkedUserListObj = responseObject.getJSONObject("linked_users");
                        Iterator<String> iter = linkedUserListObj.keys(); //This should be the iterator you want.
                        while(iter.hasNext()){
                            String key = iter.next();
                            Log.d("loginTask", key);
                            JSONObject linkedUserData = linkedUserListObj.getJSONObject(key);
                            String linked_username = linkedUserData.getString("username");
                            String linked_phone_number = linkedUserData.getString("phone_number");
                            String linked_user_type;
                            if(user_type.equals("P")){
                                linked_user_type = "W";
                            }else{
                                linked_user_type = "P";
                            }
                            User linkedUser = new User(linked_username,linked_username,linked_user_type,linked_phone_number);
                            linkedUserArrayList.add(linkedUser);
                        }

//                        for(int i = 0; i < linkedUserList.length(); i++){
//                            JSONObject linkedUserData = linkedUserList.getJSONObject(i);
//                            String linked_username = linkedUserData.getString("username");
//                            String linked_name = linkedUserData.getString("name");
//                            String linked_user_type = linkedUserData.getString("user_type");
//                            String linked_phone_number = linkedUserData.getString("phone_number");
//
//                            User linkedUser = new User(linked_username,linked_name,linked_user_type,linked_phone_number);
//                            linkedUserArrayList.add(linkedUser);
//                        }
                        User user = new User(username, name, phone_number, user_type);
                        return user;
                    }else{
                        message = "아이디 비밀번호를 다시한번 확인해주세요!";
                        return null;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }else{
                message = "오류가 발생하였습니다! 다시한번 시도해주세요!";
                return null;
            }


        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            Log.d("Login", user.getName() + user.getUser_type() + user.getPhone_number());
            if(user == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(message)
                        .setNegativeButton("확인", null)
                        .create()
                        .show();
            }else{
                if(user.getUser_type().equals("P")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putParcelableArrayListExtra("LinkedUserList", linkedUserArrayList);
                    intent.putExtra("userData", user);
                    LoginActivity.this.startActivity(intent);
                }else{
                    Intent intent = new Intent(LoginActivity.this, DeviceScanActivity.class);
                    intent.putParcelableArrayListExtra("LinkedUserList", linkedUserArrayList);
                    intent.putExtra("userData", user);
                    LoginActivity.this.startActivity(intent);
                }
                Log.d("loginTask", user.getName());
            }
        }
    }
}
