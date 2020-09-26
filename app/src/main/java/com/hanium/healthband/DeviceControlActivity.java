package com.hanium.healthband;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hanium.healthband.Api.API;
import com.hanium.healthband.model.User;
import com.hanium.healthband.postData.postGuardian;
import com.hanium.healthband.recyclerView.guardiansListAdapter;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static com.hanium.healthband.LoginActivity.token;



/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;
    private TextView tv_temperature;
    private TextView tv_humidity;
    private TextView tv_heartRate;
    private TextView tv_sound;


    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService = new BluetoothLeService();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private ArrayList<BluetoothGattCharacteristic> knownChars =
            new ArrayList<BluetoothGattCharacteristic>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private final String connected = "connected";

    private RecyclerView guardiansRecyclerView;
    private ImageButton ib_addGuardian;

    MyBackGroundService mService = null;
    boolean mBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if(mBound){
            unbindService(mLocServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private final ServiceConnection mLocServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackGroundService.LocationLocalBinder binder = (MyBackGroundService.LocationLocalBinder) service;
            mService =binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "serviceCOnnect called");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("connected");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("disconnected");
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                if(intent.getStringExtra(BluetoothLeService.HUMIDITY_DATA) != null ){
                    tv_humidity.setText(intent.getStringExtra(BluetoothLeService.HUMIDITY_DATA));
                }
                if(intent.getStringExtra(BluetoothLeService.TEMPERATURE_DATA) != null){
                    tv_temperature.setText(intent.getStringExtra(BluetoothLeService.TEMPERATURE_DATA));
                }
                if(intent.getStringExtra(BluetoothLeService.HEART_RATE_DATA) != null){
                    tv_heartRate.setText(intent.getStringExtra(BluetoothLeService.HEART_RATE_DATA));
                }
                if(intent.getStringExtra(BluetoothLeService.SOUND_DATA) != null){
                    tv_sound.setText(intent.getStringExtra(BluetoothLeService.SOUND_DATA));
                }


            }
        }
    };
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        Log.w("mGattChars", String.valueOf(mGattCharacteristics.size()));
                        for(int i = 0; i< mGattCharacteristics.size(); i++) {


//                            final BluetoothGattCharacteristic characteristic =
//                                    mGattCharacteristics.get(groupPosition).get(childPosition);
                            final BluetoothGattCharacteristic characteristic =
                                    mGattCharacteristics.get(groupPosition).get(i);
                            Log.w("mGattChars", characteristic.toString() + "dsad " + i);
                            final int charaProp = characteristic.getProperties();
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                // If there is an active notification on a characteristic, clear
                                // it first so it doesn't update the data field on the user interface.
                                if (mNotifyCharacteristic != null) {
                                    mBluetoothLeService.setCharacteristicNotification(
                                            mNotifyCharacteristic, false);
                                    mNotifyCharacteristic = null;
                                }

                                mBluetoothLeService.readCharacteristic(characteristic);

                            }
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                mNotifyCharacteristic = characteristic;
                                mBluetoothLeService.setCharacteristicNotification(
                                        characteristic, true);
                            }

                        }
                        return true;
                    }
                    return false;
                }
            };
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText("empty data");
    }

    public static ArrayList<User> linkedUserArrayList = new ArrayList<>();
    guardiansListAdapter guardiansListAdapter;
    private User user;
    private TextView tv_userName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        final Intent intent = getIntent();
        if(intent == null){
            Log.d("device", "intent is null");
        }else{
            mDeviceName = intent.getStringExtra("DEVICE_NAME");
            mDeviceAddress = intent.getStringExtra("DEVICE_ADDRESS");
            linkedUserArrayList = intent.getParcelableArrayListExtra("LinkedUserList");
            user = intent.getParcelableExtra("userData");

            tv_userName = findViewById(R.id.tv_userName);
            tv_userName.setText(user.getName());


            guardiansRecyclerView = findViewById(R.id.rv_guardian);
            guardiansListAdapter = new guardiansListAdapter(DeviceControlActivity.this,linkedUserArrayList);
            guardiansRecyclerView.setLayoutManager(new LinearLayoutManager(DeviceControlActivity.this, LinearLayoutManager.VERTICAL,false));
            guardiansRecyclerView.setAdapter(guardiansListAdapter);
        }

        Intent bindLocServiceIntent = new Intent(DeviceControlActivity.this, MyBackGroundService.class);
        bindLocServiceIntent.putExtra("user",user);
        Log.w("send user", user.getName());
        bindLocServiceIntent.putParcelableArrayListExtra("linkedUserArrayList", linkedUserArrayList);
        //new Intent(DeviceControlActivity.this,
        //                        MyBackGroundService.class)
        bindService(bindLocServiceIntent,
                mLocServiceConnection,
                Context.BIND_AUTO_CREATE);
//        mService.requestLocationUpdates();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //2초마다 위치정보 GET
                mService.requestLocationUpdates();
            }
        }, 2000);

        RelativeLayout rl_heart = findViewById(R.id.rl_heart);
        RelativeLayout rl_env = findViewById(R.id.rl_tempHumi);
        RelativeLayout rl_sound = findViewById(R.id.rl_sound);

        rl_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControlActivity.this, ChartActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", user);
                intent.putExtra("sensorType", "heartRate");

                DeviceControlActivity.this.startActivity(intent);
            }
        });

        rl_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControlActivity.this, ChartActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", user);
                intent.putExtra("sensorType", "sound");
                DeviceControlActivity.this.startActivity(intent);
            }
        });

        rl_env.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControlActivity.this, EnvChartActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", user);
                DeviceControlActivity.this.startActivity(intent);
            }
        });



        Log.d("NAME", mDeviceName + mDeviceAddress);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        tv_temperature = findViewById(R.id.tv_temperature);
        tv_humidity = findViewById(R.id.tv_humidity);
        tv_heartRate = findViewById(R.id.tv_heartRate);
        tv_sound = findViewById(R.id.tv_hearing);


        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        ib_addGuardian = findViewById(R.id.ib_addGuardian);
        ib_addGuardian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText input = new EditText(DeviceControlActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                AlertDialog dialog = new AlertDialog.Builder(DeviceControlActivity.this)
                        .setTitle("보호자 추가")
                        .setMessage("보호자의 아이디를 입력해주세요!")
                        .setView(input)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String editTextInput = input.getText().toString();
                                postGuardian postGuardian = new postGuardian(DeviceControlActivity.this,user.getUsername(),editTextInput, linkedUserArrayList, guardiansListAdapter,token);
                                postGuardian.execute(API.postLink);
                                Log.d("onclick","editext value is: "+ editTextInput);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });
        //get guardian's List
//        guardiansRecyclerView = findViewById(R.id.rv_guardian);
//        fetchGuardiansList fetchGuardiansList = new fetchGuardiansList(DeviceControlActivity.this, userID, guardiansRecyclerView);
//        fetchGuardiansList.execute("API");


    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                unbindService(mServiceConnection);
                mBluetoothLeService.close();
                mBluetoothLeService.disconnect();
                mService.removeLocationUpdate();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateConnectionState(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(message);
            }
        });
    }
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
            tv_temperature.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "unknown Service";
        String unknownCharaString = "unknown Characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {//발견된 서비스들을 순회하면서 하나의 서비스에 대해서 연산 수
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();//발견된 서비스의 uuid
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));//현재 발견된 서비스의 uuid가 샘플에 있는 uuid면 이름을 지정해줌
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);//발견된 서비스들을 더하는 곳

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();//해당 서비스의 charateristic들을 구함
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {//한 서비스에 대한 characteristic을 돌면서
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));//알고있는 characteristic이라면 이름을 정해줌
                currentCharaData.put(LIST_UUID, uuid);
                if (!SampleGattAttributes.lookup(uuid, unknownCharaString).equals(unknownCharaString)) {
                    knownChars.add(gattCharacteristic);
                }
                gattCharacteristicGroupData.add(currentCharaData);
                Log.w("chars", gattCharacteristic.toString());

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

            }
        Log.w("loop", String.valueOf(knownChars.size()));

        for (int i = 0; i < knownChars.size(); i++) {
            final BluetoothGattCharacteristic characteristic =
                    knownChars.get(i);
            final int charaProp = characteristic.getProperties();

            mBluetoothLeService.readCharacteristic(characteristic);

            mNotifyCharacteristic = characteristic;
            Log.w("loop", mNotifyCharacteristic.getUuid().toString() + "dsad " + i);
        }

        //}
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Common.KEY_REQUESTING_LOCATION_UPDATES)){

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event){
        if(event != null){
            Log.w("GETLOCATION IN ACTIVITY",event.getLocation().getLatitude() + " "
                    + event.getLocation().getLatitude());
            Toast.makeText(DeviceControlActivity.this,event.getLocation().getLatitude() + " "
                            + event.getLocation().getLatitude(), Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(DeviceControlActivity.this, "event is null", Toast.LENGTH_SHORT).show();
        }
    }

}