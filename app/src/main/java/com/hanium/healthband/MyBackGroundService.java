package com.hanium.healthband;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hanium.healthband.model.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class MyBackGroundService extends Service {

    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.hanium.healthband.started_from_notification";

    private static final long UPDATE_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_INTERVAL_IN_MIL = UPDATE_INTERVAL_IN_MIL/2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler myServiceHandler;
    private Location mLocation;

    private mySocket mySocket;

    private User user;
    private ArrayList<User> linkedUserArrayList = new ArrayList<>();


    public MyBackGroundService(){

    }

    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread("getLocation");
        handlerThread.start();
        myServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean startedFromNotification =intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);

        if(startedFromNotification){
            removeLocationUpdate();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    public void removeLocationUpdate() {
        try{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestingLocationUpdates(this, false);
            stopSelf();
        }catch (SecurityException ex){
            Common.setRequestingLocationUpdates(this, true);
            Log.e("getLoc", "Lost location permission remove updates" + ex);

        }
    }

    private void getLastLocation() {

        try{
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                mLocation = task.getResult();
                            }else{
                                Log.e("GETLOCATION", "FAILED" );
                            }
                        }
                    });
        }catch (SecurityException ex){
            Log.e("GETLOCATION", "LOST PERMISSION" + ex);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MIL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_IN_MIL);
    }

    private void onNewLocation(Location lastLocation) {
        mLocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));

        Log.w("GET UPDATE LOCATION",mLocation.getLatitude() + " " + mLocation.getLongitude() );
        mySocket.sendDataToServer(mLocation.getLatitude() + " " + mLocation.getLongitude());

        //Update notification content if running as a foreground service
//        if(serviceIsRunningInForeGround(this)){
//            mNotificationManager.notify(NOTI_ID, getNotification());
//        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, MyBackGroundService.class);
        String text = mLocation.getLatitude() + " " + mLocation.getLongitude();

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntnet = PendingIntent.getActivity(this, 0, new Intent(this, DeviceControlActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.bt_bluetooth,"Launch", activityPendingIntnet)
                .addAction(R.drawable.heart, "Remove", servicePendingIntent)
                .setContentText(text)
                .setContentTitle("title")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();

    }

    private boolean serviceIsRunningInForeGround(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE) ){
            if(getClass().getName().equals(service.service.getClassName())){
                if(service.foreground){
                    return true;
                }
            }
        }

        return false;
    }

    private final IBinder mBinder = new LocationLocalBinder();

    public void requestLocationUpdates() {
        Common.setRequestingLocationUpdates(this,true);
        startService(new Intent(getApplicationContext(), MyBackGroundService.class));
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }catch (SecurityException ex){
            Log.e("getLoc", "security exceiot" + ex);
        }
    }

    public class LocationLocalBinder extends Binder {
        MyBackGroundService getService(){
            return MyBackGroundService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        user = intent.getParcelableExtra("user");
        linkedUserArrayList = intent.getParcelableArrayListExtra("linkedUserArrayList");
        Log.w("GET DATA FROM SERVICE", user.getName() + linkedUserArrayList.get(0).getName());
        mySocket = new mySocket("http://52.79.230.118:8000",user, linkedUserArrayList );
        mySocket.connectToServer();
        mySocket.joinLink();
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(!mChangingConfiguration && Common.requestingLocationUpdate(this)){
            startForeground(NOTI_ID, getNotification());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        myServiceHandler.removeCallbacks(null);
        mySocket.disconnect();
        super.onDestroy();
    }
}
