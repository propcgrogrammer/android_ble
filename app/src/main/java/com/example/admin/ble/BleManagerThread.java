package com.example.admin.ble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.*;

/**
 * Created by admin on 2016/12/23.
 */

public class BleManagerThread extends Thread {

    private ArrayList<BleWearable> wearables = null;
    private BleWearable bleWearable = null;
    private ArrayList<Map> bleMaps = new ArrayList<>();

    private MqttManagerThread mqttManagerThread = null;

    public BleManagerThread()
    {

    }

    public BleManagerThread(ArrayList<Map> bleMaps)
    {
        this.bleMaps = bleMaps;
    }

    public void update(Map map)
    {
        this.bleMaps.add(map);
        Globalvariable.updated = createJSONformat();
        System.out.println("BMT => "+Globalvariable.updated);
    }
    public void update(String name, String x, String y,String z)
    {
        for(int i=0;i<this.wearables.size();i++)
        {
            if(this.wearables.get(i).name.equals(name))
            {
                this.wearables.get(i).pitch_x = x;
                this.wearables.get(i).row_y = y;
                this.wearables.get(i).yow_z = z;
                System.out.println("update =>"+this.wearables);
                return;
            }
        }
        this.wearables.add(new BleWearable(name,x,y,z));
        System.out.println("update =>"+this.wearables);

    }
    public String createJSONformat()
    {
        Map map = new LinkedHashMap();

        for(Map m : this.bleMaps)
        {
            map.putAll(m);
        }

        System.out.println("JSON => "+new JSONObject(map).toString());
        return new JSONObject(map).toString();
    }
    public String createJSONformat(BleWearable bleWearable)
    {
        HashMap<String,BleWearable> tmp = new LinkedHashMap<String,BleWearable>();
        tmp.put("BLE",bleWearable);
        System.out.println("JSON =>"+ new JSONObject(tmp).toString());
        return new JSONObject(tmp).toString();
    }
    public String createJSONformat(ArrayList<BleWearable> wearables)
    {
        System.out.println("createJSONformat");

        if(this.wearables == null)    this.wearables = wearables;

        Map<String, HashMap<String,String>> wear = new LinkedHashMap<>();

        for(final BleWearable ble : wearables)
        {
              HashMap<String,String> tmp = new LinkedHashMap<String,String>(){
                  {
                      put("x",ble.pitch_x);
                      put("y",ble.row_y);
                      put("z",ble.yow_z);
                  }
              };
              wear.put(ble.name.toString(), tmp);
        }
        return new JSONObject(wear).toString();
    }

    @Override
    public void run() {

    }
}
