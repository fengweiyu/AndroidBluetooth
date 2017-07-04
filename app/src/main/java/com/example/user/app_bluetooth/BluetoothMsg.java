package com.example.user.app_bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by user on 8/16/2016.
 */
public class BluetoothMsg {
    /**
     * 蓝牙连接类型
     *
     */
    public enum ServerOrCilent{
        NONE,
        SERVICE,
        CILENT
    };
    //蓝牙连接方式
    public static ServerOrCilent serviceOrCilent = ServerOrCilent.NONE;
    //连接蓝牙地址
    public static String BlueToothAddress = null,lastblueToothAddress=null;
    //通信线程是否开启
    public static boolean isOpen = false;
    public static BluetoothDevice BluetoothRemoteDevice=null;
}
