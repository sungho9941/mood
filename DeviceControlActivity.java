/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.wekit1;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = com.example.wekit1.DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    //private int[] RGBFrame = {0,0,0};
    TextView textView1, textView2, textView3;
    private TextView isSerial;
    private TextView mConnectionState;
    private TextView mDataField;
    private SeekBar mRed,mGreen,mBlue;
    private String mDeviceName;
    private String mDeviceAddress;
  //  private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
     private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    Button button5, button6, button7, button8;
    ImageView imageView, imageView2;//
    Bitmap bitmap;//
    View view2;//


    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
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
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        textView1=(TextView)findViewById(R.id.textView1);
        textView2=(TextView)findViewById(R.id.textView2);
        textView3=(TextView)findViewById(R.id.textView3);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
         mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        mDataField = (TextView) findViewById(R.id.data_value);
        mRed = (SeekBar) findViewById(R.id.seekRed);
        mRed.setMax(255);// zz
        mRed.setProgress(0);//zz
        mRed.setOnSeekBarChangeListener(SeekBarChangeListener_R);//zz
        mGreen = (SeekBar) findViewById(R.id.seekGreen);
        mGreen.setMax(255);//zz
        mGreen.setProgress(0);//zz
        mGreen.setOnSeekBarChangeListener(SeekBarChangeListener_G);//zz
        mBlue = (SeekBar) findViewById(R.id.seekBlue);
        mBlue.setMax(255);//zz
        mBlue.setProgress(0);//zz
        mBlue.setOnSeekBarChangeListener(SeekBarChangeListener_B);//zz
        view2 = (View)findViewById(R.id.view2); //
        view2.setBackgroundColor(Color.rgb(mRed.getProgress(), mGreen.getProgress(), mBlue.getProgress()));

        button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(this::onClick);//zz
        button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(this::onClick);//zz
        button7 = (Button) findViewById(R.id.button7);
        button7.setOnClickListener(this::onClick);//zz
        button8 = (Button) findViewById(R.id.button8);
        button8.setOnClickListener(this::onClick);//zz
        imageView2=(ImageView)findViewById(R.id.imageView2);//
        imageView2.setImageResource(R.drawable.logo);//
        imageView = findViewById(R.id.imageView);
        imageView.setDrawingCacheEnabled(true);

        imageView.buildDrawingCache(true);
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {

                bitmap = imageView.getDrawingCache();
                int pixel = bitmap.getPixel((int) event.getX(), (int) event.getY());

                red_value = Color.red(pixel);
                green_value = Color.green(pixel);
                blue_value = Color.blue(pixel);
                textView1.setText("R:" + red_value);
                textView2.setText("G:" + green_value);
                textView3.setText("B:" + blue_value);
                mRed.setProgress(red_value);
                mGreen.setProgress(green_value);
                mBlue.setProgress(blue_value);


            }
            return false;
        });
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {

        if (data != null) {
            mDataField.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

 
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            
            // If the service exists for HM 10 Serial, say so.
            if(SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") { isSerial.setText("Yes, serial :-)"); } else {  isSerial.setText("No, serial :-("); } 
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

     		// get characteristic when UUID matches RX/TX UUID
    		 characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
    		 characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }
        
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
//    private void readSeek(SeekBar seekBar, final int pos) {
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
//        	@Override
//        	   public void onProgressChanged(SeekBar seekBar, int progress,
//                                             boolean fromUser) {
//        			RGBFrame[pos]=progress;
//        		}
//
//        	   @Override
//        	   public void onStartTrackingTouch(SeekBar seekBar) {
//        	    // TODO Auto-generated method stub
//        	   }
//
//        	   @Override
//        	   public void onStopTrackingTouch(SeekBar seekBar) {
//        	    // TODO Auto-generated method stub
//              		makeChange();
//        	   }
//        });
//    }
    // on change of bars write char 
private SeekBar.OnSeekBarChangeListener SeekBarChangeListener_R = new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        red_value=progress;
        textView1.setText("R:" + red_value);
        textView2.setText("G:" + green_value);
        textView3.setText("B:" + blue_value);
        view2.setBackgroundColor(Color.rgb(mRed.getProgress(), mGreen.getProgress(), mBlue.getProgress()));
        makeChange();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        makeChange();
    }
};
    private SeekBar.OnSeekBarChangeListener SeekBarChangeListener_G = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            green_value=progress;
            textView1.setText("R:" + red_value);
            textView2.setText("G:" + green_value);
            textView3.setText("B:" + blue_value);
            view2.setBackgroundColor(Color.rgb(mRed.getProgress(), mGreen.getProgress(), mBlue.getProgress()));
            makeChange();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            makeChange();
        }
    };
    private SeekBar.OnSeekBarChangeListener SeekBarChangeListener_B = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            blue_value=progress;
            textView1.setText("R:" + red_value);
            textView2.setText("G:" + green_value);
            textView3.setText("B:" + blue_value);
            view2.setBackgroundColor(Color.rgb(mRed.getProgress(), mGreen.getProgress(), mBlue.getProgress()));
            makeChange();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            makeChange();
        }
    };

    private void makeChange() {
        String str = (String.format("%03d", red_value)) + "" + (String.format("%03d",  green_value)) + "" + (String.format("%03d", blue_value)) + "\n";
         Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();

        if(mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);

        }
    }
    int red_value;
    int green_value;
    int blue_value;

    public void onClick(View v){
        if(v == button5){
            red_value = 255;
            green_value = 0;
            blue_value = 0;
            textView1.setText("R:" + red_value);
            textView2.setText("G:" + green_value);
            textView3.setText("B:" + blue_value);


        }
        if(v == button6){
            red_value = 0;
            green_value = 255;
            blue_value = 0;
            textView1.setText("R:" + red_value);
            textView2.setText("G:" + green_value);
            textView3.setText("B:" + blue_value);

        }
        if(v == button7){
            red_value = 0;
            green_value = 0;
            blue_value = 255;
            textView1.setText("R:" + red_value);
            textView2.setText("G:" + green_value);
            textView3.setText("B:" + blue_value);
        }
        if(v == button8){
            red_value = 0;
            green_value = 0;
            blue_value = 0;
            textView1.setText("R:" + red_value);
            textView2.setText("G:" + green_value);
            textView3.setText("B:" + blue_value);
        }
        mRed.setProgress(red_value);
        mGreen.setProgress(green_value);
        mBlue.setProgress(blue_value);
    }
}