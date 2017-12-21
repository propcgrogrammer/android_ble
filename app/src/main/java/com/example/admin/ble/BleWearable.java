package com.example.admin.ble;

import java.util.ArrayList;

/**
 * Created by admin on 2016/12/23.
 */

public class  BleWearable {


    public  String name = "BLE";
    public  String pitch_x = "";
    public  String row_y = "";
    public  String yow_z = "";


    public BleWearable(){};
    public BleWearable(String name,String x,String y,String z)
    {
        this.name = name;
        this.pitch_x = x;
        this.row_y = y;
        this.yow_z = z;
    }

}
