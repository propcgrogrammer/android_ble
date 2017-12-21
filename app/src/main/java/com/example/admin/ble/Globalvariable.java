package com.example.admin.ble;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Pack200;

/**
 * Created by tsai on 2016/11/19.
 */

public class Globalvariable {



    public static int Y_deviation, P_deviation, R_deviation;
    public static int pitch, row ,yaw;
    private static final int MAX_CONNECT = 7;

    public static Map<String,String> tagMapping = new HashMap<String,String>() { {

//        put("80:CC:C6:2D:97:3F","LeftArm");
//        put("80:CC:C2:8E:01:FF","LeftForeArm");
//        put("80:CC:C2:AC:6D:33","LeftHand");
//        put("80:CC:C2:E5:65:14","RightArm");
//        put("80:CC:C2:88:62:DE","RightForeArm");
//        put("80:CC:C2:8A:3B:0D","RightHand");

        put("80:CC:C2:89:FA:41","LeftArm");
        put("80:CC:C2:AD:89:B3","LeftForeArm");
        put("80:CC:C2:87:C1:F1","LeftHand");
        put("80:CC:C2:8B:99:F5","RightArm");
        put("80:CC:C2:8D:A5:88","RightForeArm");
        put("80:CC:C2:A8:62:97","RightHand");
    }
    };
    public static String updated = "";
    public static ArrayList<String> device_type = new ArrayList<>();

    public static Map<String,Map> device_content = new HashMap<String,Map>();
    public static Map<String,String> json_content = new HashMap<String,String>();


//    public static  ArrayList<Map> device_content = new ArrayList<Map>();

    public Globalvariable()
    {

    }




//    public static String createJSONFormat()
//    {
//
//        Map tmp = new HashMap();
//
//        if(device_content.size() == 0)  return "";
//
//        for(Map map : device_content)
//        {
//            tmp.putAll(map);
//        }
//        return new JSONObject(tmp).toString();
//
//    }


}
