package com.example.admin.ble;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by admin on 2016/12/26.
 */

public class MqttPubThread extends Thread {

    private MqttManagerThread mqttManagerThread = null;
    private MqttClient mqttClient = null;
    private String TOPIC_NAME = "xyz";

    public MqttPubThread(){};
    public MqttPubThread(MqttManagerThread mqttManagerThread)
    {
        this.mqttManagerThread = mqttManagerThread;
        this.mqttClient = mqttManagerThread.getMqttClient();
    }
    public MqttPubThread(MqttManagerThread mqttManagerThread , String topic_name)
    {
        this.mqttManagerThread = mqttManagerThread;
        this.TOPIC_NAME = topic_name;
        this.mqttClient = mqttManagerThread.getMqttClient();
    }
    @Override
    public void run(){
        try {
            Thread.sleep(500);
            String content = Globalvariable.updated;
            setMessage(content);

        }catch (MqttException e) {
                e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public void setMessage(final String message) throws MqttException, InterruptedException {

        //      Thread.sleep(1000);
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(0);// qos set to 0
        mqttMessage.setRetained(false);//保留最後一筆訊息
        // publish a message to a Topic: "testtopic"
        this.mqttClient.publish(this.TOPIC_NAME, mqttMessage);
   //     System.out.println("Send Message =>"+ message);

    }
}
