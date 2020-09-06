package com.hanium.healthband;

import android.util.Log;

import com.google.gson.Gson;
import com.hanium.healthband.model.LinkedInfo;
import com.hanium.healthband.model.Message;
import com.hanium.healthband.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class mySocket {

    private static final String TAG = "mySocket";
    private String url;
    private Socket mSocket;
    private User user;
    private ArrayList<User> linkedUserArrayList = new ArrayList<>();

    public mySocket(String url, User user, ArrayList<User> linkedUserArrayList) {
        this.url = url;
        this.user = user;
        this.linkedUserArrayList = linkedUserArrayList;
    }

    public void connectToServer(){
        try {
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        joinLink();
        waitEvent();
    }

    public void joinLink(){
        LinkedInfo msg = new LinkedInfo(user.getUser_type() ,user.getUsername(), linkedUserArrayList);
        Gson gson = new Gson();
        try {
            JSONObject obj = new JSONObject(gson.toJson(msg));
            mSocket.emit("joinLink", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void waitEvent(){
        //mSocket.on("welcome", receiveMessage);

        mSocket.on("welcome", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject messageJson = new JSONObject(args[0].toString());
                    String receivedData = messageJson.getString("text");
                    Log.w(TAG, "received Data from socekt" + messageJson);
                    Log.w(TAG, "received Data from socekt" + receivedData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mSocket.on("sendData", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject messageJson = new JSONObject(args[0].toString());
                    String receivedData = messageJson.getString("text");
                    Log.w(TAG, "received Data from socekt" + messageJson);
                    Log.w(TAG, "received Data from socekt" + receivedData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendDataToServer(String content){
        Gson gson = new Gson();
        try {
            Message msg = new Message(user.getUsername(), content);
            JSONObject obj = new JSONObject(gson.toJson(msg));
            mSocket.emit("androidMessage", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(mSocket == null){
                Log.w(TAG, "null!!!");
            }else{
                mSocket.emit("login", user.getUsername());
            }
            Log.d(TAG, "Socket is connected with " +  user.getUsername());
        }
    };

    public void disconnect(){
        mSocket.disconnect();
    }


}
