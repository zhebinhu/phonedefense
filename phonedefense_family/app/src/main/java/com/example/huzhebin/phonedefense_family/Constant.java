package com.example.huzhebin.phonedefense_family;

import android.os.Environment;

/**
 * Created by 34494 on 2017/2/16.
 */

public class Constant {
    /** 服务器基地址 */
    public static final String BASE_URL = "http:/192.168.1.109:8080";
    public static final String storeDirectory= Environment.getExternalStorageDirectory()+"/ISvideo";
    public static String familyid;
    public static boolean ispolling;
    public static boolean islistenning;
    public static String status = "空闲状态";
    public static int grade = 0;
}
