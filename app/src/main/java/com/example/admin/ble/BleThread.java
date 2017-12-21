package com.example.admin.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Created by admin on 2016/10/28.
 */

public class BleThread extends Thread {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mBluetoothGattService;
    private BluetoothGattCharacteristic characteristic;
    private String name = "";

    private MqttClient mqttClient = null;

    private BleManagerThread bleManagerThread;
    private MqttManagerThread mqttManagerThread;

    private String dataValue;

    private boolean isPart = false;

    private TextView t;
    private TextView content;
    private Context context;
    private  String address;
    private int P_calibration,R_calibration,Y_calibration;

    public BleThread(final BluetoothDevice mBluetoothDevice)
    {
        this.mBluetoothDevice = mBluetoothDevice;
        mHandler = new Handler();
    }
    public BleThread(Context context,TextView t,TextView content,String address){

        this.context = context;
        this.t = t;
        this.content = content;
        this.address = address;
        mHandler = new Handler();
    }
    public BleThread(Context context,TextView t,TextView content,String address,final BluetoothDevice mBluetoothDevice){

        this.context = context;
        this.t = t;
        this.content = content;
        this.address = address;
        this.mBluetoothDevice = mBluetoothDevice;
        this.name = Globalvariable.tagMapping.get(address).toString();
        mHandler = new Handler();

    }
    public BleThread(Context context,TextView t,TextView content,String address,final BluetoothDevice mBluetoothDevice, BleManagerThread bleManagerThread){

        this.context = context;
        this.t = t;
        this.content = content;
        this.address = address;
        this.mBluetoothDevice = mBluetoothDevice;
        this.bleManagerThread = bleManagerThread;
        this.name = Globalvariable.tagMapping.get(address).toString();
        mHandler = new Handler();

    }
    public BleThread(Context context,TextView t,TextView content,String address,final BluetoothDevice mBluetoothDevice, BleManagerThread bleManagerThread, MqttManagerThread mqttManagerThread){

        this.context = context;
        this.t = t;
        this.content = content;
        this.address = address;
        this.mBluetoothDevice = mBluetoothDevice;
        this.bleManagerThread = bleManagerThread;
        this.name = Globalvariable.tagMapping.get(address).toString();
        this.mqttManagerThread = mqttManagerThread;
        mHandler = new Handler();

    }


    public BleThread(Context context,TextView t,TextView content,String address,final BluetoothDevice mBluetoothDevice, BleManagerThread bleManagerThread, MqttManagerThread mqttManagerThread, MqttClient mqttClient){

        this.context = context;
        this.t = t;
        this.content = content;
        this.address = address;
        this.mBluetoothDevice = mBluetoothDevice;
        this.bleManagerThread = bleManagerThread;
        this.name = Globalvariable.tagMapping.get(address).toString();
        this.mqttClient = mqttClient;
        isPart = true;
        this.mqttManagerThread = mqttManagerThread;
        mHandler = new Handler();

    }




    @Override
    public void run() {

        mHandler.post(new Runnable() {
            public void run() {
                t.append("嘗試與  連接 ......\n");
            }
        });


        mBluetoothGatt = this.mBluetoothDevice.connectGatt(context, false, mGattCallback);

        if(mBluetoothGatt == null) return;

        mHandler.post(new Runnable() {
            public void run() {
                t.append("連線成功\n");
            }
        });

    }

    private void ReConnect()
    {
        mBluetoothGatt = this.mBluetoothDevice.connectGatt(context, false, mGattCallback);
    }
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, "onConnectionStateChange()", Toast.LENGTH_LONG).show();

                }
            });

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "BluetoothProfile.STATE_CONNECTED", Toast.LENGTH_LONG).show();

                    }
                });
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                ReConnect();

                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "BluetoothProfile.STATE_DISCONNECTED ==> ReConnect()", Toast.LENGTH_LONG).show();

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
                    Toast.makeText(context, "onServicesDiscovered()", Toast.LENGTH_LONG).show();

                }
            });

            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt = gatt;
                mBluetoothGattService = gatt.getServices();

                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "BluetoothGatt.GATT_SUCCESS", Toast.LENGTH_LONG).show();


                        if (mBluetoothGattService == null || mBluetoothGattService.size() == 0) {
                            t.append("找不到服務 : \n");
                            return;
                        }
                        t.append("正在搜尋服務 : \n");
                        t.append("------------ (服務如下) -----------------\n");

                        for(BluetoothGattService bgs : mBluetoothGattService) {
                    //        t.append("UUID : " + bgs.getUuid().toString() + "\n");

                            List<BluetoothGattCharacteristic> gattCharacteristics = bgs.getCharacteristics();

                            for(BluetoothGattCharacteristic bgc : gattCharacteristics){

                        //        t.append(" >> Characteristic :" + bgc.getValue() + "\n");

                                mBluetoothGatt.setCharacteristicNotification(bgc ,true);
                                //      mBluetoothGatt.readCharacteristic(bgc);

                            }
                        }


                    }
                });

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
            //    System.out.println("String "+val);
                //Log.i("", "onCharacteristicChanged " + val + "\n");
            }

       //     Log.i("", "onCharacteristicRead " + characteristic.getValue());

            //   mBluetoothGatt.readCharacteristic(characteristic);

            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, "onCharacteristicRead()", Toast.LENGTH_LONG).show();

                }
            });

            if (status == BluetoothGatt.GATT_SUCCESS) {



                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "BluetoothGatt.GATT_SUCCESS", Toast.LENGTH_LONG).show();

                    }
                });
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

      //      System.out.println("onCharacteristicChanged");
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                String val = stringBuilder.toString();
                dataValue = val;
                //   content.setText(val);

//                t.append(" >>> value : "+val+"\n");
            //    Log.i("", "onCharacteristicChanged(Blethread.byte) " + data);
            //    Log.i("", "onCharacteristicChanged(Blethread.String) " + val);
                RawdataToPRY(val);

            }

            mHandler.post(new Runnable() {
                public void run() {
                    //     Toast.makeText(MainActivity.this, "onCharacteristicChanged()", Toast.LENGTH_LONG).show();
                  //  content.setText(dataValue);
                    Toast.makeText(context, "onCharacteristicChanged()", Toast.LENGTH_LONG).show();
              //      content.setText(dataValue);
                }
            });

        }

    };

    private void RawdataToPRY(String Rawdata){
   //     System.out.println("Rawdata: "+Rawdata.length()) ;
       // if(Rawdata.length()==24) {   //length= 17 冠榮 未來馬戲團,出夢入夢 ; length=24 建宜 電子肌膚
            StringBuilder ＰＲＹ = new StringBuilder();

            //建宜格式
            //Notification handle = 0x000a value: 01 63 01 94 01 6e 7e 7d
            //        [CON][80:CC:C2:8D:70:66][LE]>

            Globalvariable.row=Integer.parseInt(Rawdata.substring(6,8)+Rawdata.substring(9,11), 16);
            Globalvariable.yaw= Integer.parseInt(Rawdata.substring(0,2)+Rawdata.substring(3,5), 16);
            Globalvariable.pitch= Integer.parseInt(Rawdata.substring(12,14)+Rawdata.substring(15,17), 16);
            R_calibration=Globalvariable.row + Globalvariable.R_deviation;
            P_calibration=Globalvariable.pitch + Globalvariable.P_deviation;
            Y_calibration=Globalvariable.yaw + Globalvariable.Y_deviation;

            int offset=2;
            for(int i=0;i<(Rawdata.length()-1);i=i+3){
        //        System.out.println("PRY => "+Rawdata.substring(i,i+offset)+" "+(char)Integer.parseInt(Rawdata.substring(i,i+offset), 16));
                //52(16Hex)=>82(10dec)
                //82(10dec)=>char(R)
            }
       //     System.out.println("PRY2=>"+P_calibration+" "+R_calibration+" "+Y_calibration);

            String x = String.valueOf(P_calibration);
            String y = String.valueOf(R_calibration);
            String z = String.valueOf(Y_calibration);

            Map map = new LinkedHashMap();
            map.put("x", x);
            map.put("y", y);
            map.put("z", z);
            Map map1 = new LinkedHashMap();
            map1.put(this.name,map);


            String json = new JSONObject(map1).toString();
            System.out.println("Check =>" + json);


       if(isPart) {
           try {

               System.out.println("Active MQTT");

               MqttMessage mqttMessage = new MqttMessage(json.getBytes());
               mqttMessage.setQos(0);// qos set to 0
               mqttMessage.setRetained(false);//保留最後一筆訊息
               // publish a message to a Topic: "testtopic"


               mqttClient.publish("topic", mqttMessage);
               System.out.println("MQTT DATA => "+ json.getBytes());
           } catch (MqttException e) {
               e.printStackTrace();
           }
       }

        //      System.out.println("BBBB => "+Globalvariable.device_content.containsKey(this.name));

            if(Globalvariable.device_content.containsKey(this.name))   Globalvariable.device_content.remove(this.name);
            Globalvariable.device_content.put(this.name ,map1);

            if(Globalvariable.json_content.containsKey(this.name))   Globalvariable.json_content.remove(this.name);
            Globalvariable.json_content.put(this.name ,json);

//            Map map2 = new LinkedHashMap();
//
//             for(Map m : Globalvariable.device_content.values())
//             {
//                 map2.putAll(m);
//             }
//            System.out.println("JJJJ => "+ new JSONObject(map2).toString());

         //   Globalvariable.device_content.set(Globalvariable.device_type.indexOf(this.name), map1);

     //       System.out.println("SIZE => "+Globalvariable.device_content.size());

//            for(Map m : Globalvariable.device_content)
//            {
//                System.out.println("JSON Format => "+ new JSONObject(m).toString());
//            }
//
//            System.out.println("JSON Format => "+ new JSONObject(m).toString());

//            JSONObject jsonObjectJacky = new JSONObject(map1);
//            System.out.println("RESULT ==> " + jsonObjectJacky.toString());
//
//            this.bleManagerThread.update(map1);
//            System.out.println("UPDATED ==> "+ Globalvariable.updated);
//
//            String json = this.bleManagerThread.createJSONformat();
//
//
//            try {
//
//                System.out.println("Check --> " + json );
//                this.mqttManagerThread.setMessage(json);
//
//            } catch (MqttException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }finally {
//               // System.out.println("Check" + this.name + ": " + jsonObjectJacky);
//            }

//            System.out.println("TEST=>"+x+" "+y+" "+z);
//
//            BleWearable b = new BleWearable("BLE",x,y,z);
//            this.bleManagerThread.createJSONformat(b);



       //     this.bleManagerThread.update("BLE",x,y,z);
        //    System.out.println("JSON => "+ this.bleManagerThread.createJSONformat(null));


        //}



    };



}
