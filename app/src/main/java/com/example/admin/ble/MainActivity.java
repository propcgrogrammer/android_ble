package com.example.admin.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;


import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Exchanger;

public class MainActivity extends AppCompatActivity {

    private TextView t = null;
    private Button calibration = null;
    private TextView content = null;
    private String dataValue = null;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static int Y_base=0,P_base=0,R_base=0;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mBluetoothGattService;
    private BluetoothGattCharacteristic characteristic;

    private static final String MAC_ADDRESS = "80:CC:C6:25:67:F7";



    private ArrayList<BluetoothDevice> bluetoothDeviceLst = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothGatt> bluetoothGattLst = new ArrayList<BluetoothGatt>();

    private static final int MAX_CONNECT = 7;
    private static int CURRENT_CONNECT = 0;


    private ArrayList<BleThread> bleThreads = new ArrayList<>(MAX_CONNECT);
    private static ArrayList<BleWearable> bleWearables = new ArrayList<>(MAX_CONNECT);

    private static ArrayList<Map> bleMaps = new ArrayList<>(MAX_CONNECT);


    private final String SERVER_IP = "140.119.163.200";
    private final String Client_ID = "topic";
    private String TOPIC_NAME = "topic";
    private String CONTENT = "";
    private boolean isConnected = false;
    private static MqttClient mqttClient = null;



    private Map<String,String> tagMapping = new HashMap<String,String>() { {
        put("LeftArm","");
        put("LeftForeArm","");
        put("LeftHand","");
        put("RightArm","");
        put("RightForeArm","");
        put("RightHand","");
    }
    };

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = (TextView) findViewById(R.id.textView);
        calibration = (Button)findViewById(R.id.Calibration);

        calibration.setOnClickListener(new Button.OnClickListener(){

            @Override

            public void onClick(View v) {

                // TODO Auto-generated method stub
                Globalvariable.Y_deviation = Y_base - Globalvariable.yaw;
                Globalvariable.P_deviation = P_base - Globalvariable.pitch;
                Globalvariable.R_deviation = R_base - Globalvariable.row;

                System.out.println("GOOD!GOOD!GOOD!GOOD!GOOD!");
            }

        });



        Toast.makeText(this,"onCreate()",Toast.LENGTH_LONG).show();

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();

        }


        try {

            MemoryPersistence persistence = new MemoryPersistence();
            this.mqttClient = new MqttClient("tcp://" + this.SERVER_IP + ":1883", this.Client_ID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            this.mqttClient.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        scanLeDevice(true);

    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);

    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;

            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;

            }
            final int heartRate = characteristic.getIntValue(format, 1);

            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_CONNECTED");
        intentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED");
        intentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
        intentFilter.addAction("com.example.bluetooth.le.ACTION_DATA_AVAILABLE");
        intentFilter.addAction("com.example.bluetooth.le.EXTRA_DATA");

        return intentFilter;
    }

    private class LocalBinder extends Binder {

    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        BluetoothManager mBluetoothManager= null;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {

                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {

            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {

            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (address != null && address.equals(new String())
                && mBluetoothGatt != null) {

            if (mBluetoothGatt.connect()) {

                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {

            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "onConnectionStateChange()", Toast.LENGTH_LONG).show();

                }
            });

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "BluetoothProfile.STATE_CONNECTED", Toast.LENGTH_LONG).show();

                    }
                });
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                t.append("與  "+MAC_ADDRESS+"  失去連線，重新連線中");
                tryToConn();

                if(mBluetoothGatt == null) t.append("與  "+MAC_ADDRESS+"  連線成功");
                else t.append("與  "+MAC_ADDRESS+"  連線失敗");

                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "BluetoothProfile.STATE_DISCONNECTED", Toast.LENGTH_LONG).show();

                    }
                });
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("", "gatt.getServices().size() " + gatt.getServices().size());
            Log.i("", "gatt.getServices() " + gatt.getServices().isEmpty());


            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "onServicesDiscovered()", Toast.LENGTH_LONG).show();

                }
            });
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt = gatt;

                mBluetoothGattService = gatt.getServices();


                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "BluetoothGatt.GATT_SUCCESS", Toast.LENGTH_LONG).show();


                        if (mBluetoothGattService == null || mBluetoothGattService.size() == 0) {
                            t.append("找不到服務 : \n");
                            return;
                        }
                        t.append("正在搜尋服務 : \n");
                        t.append("------------ (服務如下) -----------------\n");

                        for(BluetoothGattService bgs : mBluetoothGattService) {
                            t.append("UUID : " + bgs.getUuid().toString() + "\n");

                            List<BluetoothGattCharacteristic> gattCharacteristics = bgs.getCharacteristics();

                            for(BluetoothGattCharacteristic bgc : gattCharacteristics){

                                t.append(" >> Characteristic :" + bgc.getValue() + "\n");

                                mBluetoothGatt.setCharacteristicNotification(bgc ,true);
                                mBluetoothGatt.readCharacteristic(bgc);

                            }
                        }


                    }
                });
            } else {

            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                String val = stringBuilder.toString();
                t.append(" >>> value : "+val);
                Log.i("", "onCharacteristicChanged " + val + "\n");
            }

            Log.i("", "onCharacteristicRead " + characteristic.getValue());

         //   mBluetoothGatt.readCharacteristic(characteristic);

            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "onCharacteristicRead()", Toast.LENGTH_LONG).show();

                }
            });

            if (status == BluetoothGatt.GATT_SUCCESS) {



                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "BluetoothGatt.GATT_SUCCESS", Toast.LENGTH_LONG).show();

                    }
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {


            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                String val = stringBuilder.toString();
                dataValue = val;
             //   content.setText(val);

                t.append(" >>> value : "+val+"\n");
                Log.i("", "onCharacteristicChanged(Mainactivity.byte) " + data);
                Log.i("", "onCharacteristicChanged(Mainactivity.String) " + val);

            }



        //    Log.i("", "onCharacteristicChanged " + new String(characteristic.getValue()));

            mHandler.post(new Runnable() {
                public void run() {
               //     Toast.makeText(MainActivity.this, "onCharacteristicChanged()", Toast.LENGTH_LONG).show();
                    Toast.makeText(MainActivity.this, "onCharacteristicChanged()", Toast.LENGTH_LONG).show();
                    content.setText(dataValue);
                }
            });
        }

    };

    private void tryToConn(){

        Toast.makeText(MainActivity.this, "tryToConn()", Toast.LENGTH_LONG).show();
        t.append("嘗試與 "+MAC_ADDRESS+"  連線\n");
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MAC_ADDRESS);
        if(device == null)
        {
            Toast.makeText(this,"連線失敗",Toast.LENGTH_LONG).show();
            t.append("無法取得device物件\n");
            return;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);


        t.append("嘗試與 80:CC:C2:89:4A:DF  連線\n");

    }
    private void scanLeDevice(final boolean enable) {

        Toast.makeText(MainActivity.this, "scanLeDevice()", Toast.LENGTH_LONG).show();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    BleManagerThread bleManagerThread = new BleManagerThread(bleMaps);
                    bleManagerThread.start();

                    MqttManagerThread mqttManagerThread = new MqttManagerThread(null,null);

                    MqttClient mqttClient = null;

                    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("80:CC:C2:89:FA:41");

                    BleThread mBleThread = new BleThread(MainActivity.this,t,content,"80:CC:C2:89:FA:41",device, bleManagerThread, mqttManagerThread);
                    mBleThread.start();

                    final BluetoothDevice device2 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:AD:89:B3");

                    BleThread mBleThread2 = new BleThread(MainActivity.this,t,content,"80:CC:C2:AD:89:B3",device2, bleManagerThread,mqttManagerThread , mqttClient);
                    mBleThread2.start();

                    final BluetoothDevice device3 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:87:C1:F1");

                    BleThread mBleThread3 = new BleThread(MainActivity.this,t,content,"80:CC:C2:87:C1:F1",device3, bleManagerThread,mqttManagerThread);
                    mBleThread3.start();

                    final BluetoothDevice device4 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:8B:99:F5");

                    BleThread mBleThread4 = new BleThread(MainActivity.this,t,content,"80:CC:C2:8B:99:F5",device4, bleManagerThread,mqttManagerThread);
                    mBleThread4.start();

                    final BluetoothDevice device5 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:8D:A5:88");

                    BleThread mBleThread5 = new BleThread(MainActivity.this,t,content,"80:CC:C2:8D:A5:88",device5, bleManagerThread,mqttManagerThread);
                    mBleThread5.start();

                    final BluetoothDevice device6 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:A8:62:97");
   
                    BleThread mBleThread6 = new BleThread(MainActivity.this,t,content,"80:CC:C2:A8:62:97",device6, bleManagerThread,mqttManagerThread);
                    mBleThread6.start();

                    mqttManagerThread.start();

//                    MqttPubThread mqttPubThread = new MqttPubThread(mqttManagerThread);
//                    mqttPubThread.start();

                    /*final BluetoothDevice device1 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:89:4A:DF");
                    BleThread mBleThread1 = new BleThread(MainActivity.this,t,content,"80:CC:C2:89:4A:DF",device1);
                    mBleThread1.start();

                    final BluetoothDevice device2 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:88:32:2B");
                    BleThread mBleThread2 = new BleThread(MainActivity.this,t,content,"80:CC:C2:88:32:2B",device2);
                    mBleThread2.start();

                    final BluetoothDevice device3 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:8D:70:66");
                    BleThread mBleThread3 = new BleThread(MainActivity.this,t,content,"80:CC:C2:8D:70:66",device3);
                    mBleThread3.start();

                    final BluetoothDevice device4 = mBluetoothAdapter.getRemoteDevice("80:CC:C6:2D:97:3F");
                    BleThread mBleThread4 = new BleThread(MainActivity.this,t,content,"80:CC:C6:2D:97:3F",device4);
                    mBleThread4.start();

                    final BluetoothDevice device5 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:A8:62:97");
                    BleThread mBleThread5 = new BleThread(MainActivity.this,t,content,"80:CC:C2:A8:62:97",device5);
                    mBleThread5.start();

                    final BluetoothDevice device6 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:AD:57:3D");
                    BleThread mBleThread6 = new BleThread(MainActivity.this,t,content,"80:CC:C2:AD:57:3D",device6);
                    mBleThread6.start();

                    final BluetoothDevice device7 = mBluetoothAdapter.getRemoteDevice("80:CC:C2:88:F0:17");
                    BleThread mBleThread7 = new BleThread(MainActivity.this,t,content,"80:CC:C2:88:F0:17",device7);
                    mBleThread7.start();
                    */


             //       BleThread mBleThread = new BleThread(MainActivity.this,t,content,"80:CC:C2:AC:40:0A");
             //       mBleThread.start();



                //    tryToConn();

                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {

            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {


                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           t.append("DeviceName : " + device.getName()  + "\nDevice Address : "+ device.getAddress() + "\nRSSI :" + rssi + "  dBm\n");
                            t.append("------------------------------------------------------------------------------\n");
                        }
                    });



                }};



}
