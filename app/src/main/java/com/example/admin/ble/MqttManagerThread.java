package com.example.admin.ble;

import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Handler;


/**
 * Created by admin on 2016/12/26.
 */

public class MqttManagerThread extends Thread{

    private final String SERVER_IP = "140.119.163.200";
    private final String Client_ID = "topic";
    private String TOPIC_NAME = "topic";
    private String CONTENT = "";
    private boolean isConnected = false;
    private MqttClient mqttClient = null;


    Handler handler ;

    public MqttClient getMqttClient(){return this.mqttClient;};

    public MqttManagerThread(){};

    public MqttManagerThread(String topic, String ip, String content)
    {
        if(topic == null || topic.equals(""))  setTOPIC_NAME();
        else TOPIC_NAME = topic;

        if(content != null)  CONTENT = content;
    }
    public MqttManagerThread(String topic, String ip)
    {
        if(topic == null || topic.equals(""))  setTOPIC_NAME();
        else TOPIC_NAME = topic;

    }
    public void setCONTENT(final String content)
    {
        this.CONTENT = content;
    }
    public void setTOPIC_NAME()
    {
        TOPIC_NAME = "topic";
    }

    private void createJSON()
    {
        System.out.println("#### MQTT !!!");

        Map map2 = new LinkedHashMap();


        for(Map m : Globalvariable.device_content.values()) {
            System.out.println("DEBUG[m]  => "+ m.keySet().toString());
            map2.putAll(m);
        }

        String json = new JSONObject(map2).toString();

        map2.clear();

        System.out.println("SIZE => "+Globalvariable.device_content.size());
        System.out.println("JSON FORMAT => "+ json);
    }

    @Override
    public void run() {
        try {


            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient("tcp://" + this.SERVER_IP + ":1883", this.Client_ID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts);

//           // String message = Globalvariable.updated;
//          //  System.out.println("MSG =>"+message);
            while(true)
            {


             //       Thread.sleep(2000);

                System.out.println("#### MQTT !!!");

                Map map2 = new LinkedHashMap();


                for(Map m : Globalvariable.device_content.values()) {
                    System.out.println("DEBUG[m]  => "+ m.keySet().toString());

               //     map2.putAll(m);
                }

                for(String s : Globalvariable.json_content.values())
                {

                    System.out.println("DEBUG_JSON => "+ s);

                    MqttMessage mqttMessage = new MqttMessage(s.getBytes());
                    mqttMessage.setQos(0);// qos set to 0
                    mqttMessage.setRetained(false);//保留最後一筆訊息
                    // publish a message to a Topic: "testtopic"
                    mqttClient.publish("topic", mqttMessage);
                }

                String json = new JSONObject(map2).toString();

                map2.clear();

                System.out.println("SIZE => "+Globalvariable.device_content.size());
                System.out.println("JSON FORMAT => "+ json);

                Log.i("JSON","JSON"+json);

              //      MqttMessage mqttMessage = new MqttMessage(Globalvariable.updated.getBytes());



//                    MqttMessage mqttMessage = new MqttMessage(json.getBytes());
//                    mqttMessage.setQos(0);// qos set to 0
//                    mqttMessage.setRetained(false);//保留最後一筆訊息
//                    // publish a message to a Topic: "testtopic"
//                    mqttClient.publish("topic", mqttMessage);
//                    System.out.println("JSON BYTES =>"+ json.getBytes());
//                 //   System.out.println("Send Message =>"+ Globalvariable.updated);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void setMessage(final String message) throws MqttException, InterruptedException {

        String msg = Globalvariable.updated;
        System.out.println("Updated => "+msg);

  //      Thread.sleep(1000);
//        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
//        mqttMessage.setQos(0);// qos set to 0
//        mqttMessage.setRetained(false);//保留最後一筆訊息
//        // publish a message to a Topic: "testtopic"
//        mqttClient.publish(this.TOPIC_NAME, mqttMessage);
//        System.out.println("Send Message =>"+ message);

    }
    public void stopMqttManagerThread() throws MqttException {
        this.isConnected = false;
        mqttClient.disconnect();
    }


}
