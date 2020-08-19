package com.hanium.healthband;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.hanium.healthband.model.User;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText et_userName = findViewById(R.id.et_registerName);
        final EditText et_userEmail = findViewById(R.id.et_registerEmail);
        final EditText et_userPhone = findViewById(R.id.et_registerPhone);
        final EditText et_userPassword = findViewById(R.id.et_registerPassword);
        EditText et_userPasswordCheck = findViewById(R.id.et_registerPasswordCheck);
        RadioGroup rg_userType = findViewById(R.id.rg_registerType);

        Button bt_register = findViewById(R.id.bt_registerSend);
        int radioButtonID = rg_userType.getCheckedRadioButtonId();
        final String userType;
        if(radioButtonID == 0 ){
            userType = "w";
        }else{
            userType = "p";
        }
        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send user information to server
                RegisterTask registerTask = new RegisterTask(et_userEmail.getText().toString(), et_userPassword.getText().toString(),
                            et_userName.getText().toString(), et_userPhone.getText().toString(),userType);
                registerTask.execute("http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/rest-auth/registration");
            }
        });


    }

    class RegisterTask extends AsyncTask<String, Void, String> {
        private String userID;
        private String userPW;
        private String userName;
        private String userPhone;
        private String userType;
        private String userArduinoCode = "not Set";

        public RegisterTask(String userID, String userPW, String userName, String userPhone, String userType) {
            this.userID = userID;
            this.userPW = userPW;
            this.userName = userName;
            this.userPhone = userPhone;
            this.userType = userType;
        }

        private String TAG = "RegisterTask";
        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];

            if(TextUtils.isEmpty(userID)){
                return null;
            }
            OkHttpClient okHttpClient = new OkHttpClient();

            Log.d(TAG, userID);
            Log.d(TAG, userPW);
            RequestBody formBody = new FormBody.Builder()
                    .add("username", userID)
                    .add("password", userPW)
                    .add("user_type", userType)
                    .add("name", userName)
                    .add("phone_number", userPhone)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("loginTask", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null){
                Log.d(TAG, s);
                Intent goToLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                //DeviceScanActivity.class.getLayoutInflater();
                RegisterActivity.this.startActivity(goToLogin);
            }
        }
    }
}
