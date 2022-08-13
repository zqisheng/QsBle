package com.zqs.ble.core;

/*
 *   @author zhangqisheng
 *   @date 2022-08-05
 *   @description
 */
public class BleDebugConfig {
    //全局debug开关
    public static boolean isDebug = false;
    //日志是否打印函数栈
    public static boolean isPrintFunStack = false;
    //是否打开blethread的日志
    public static boolean isOpenBleThreadLog = false;
    //是否打开扫描日志
    public static boolean isOpenScanLog = false;
    //是否打开写特征日志
    public static boolean isOpenWriteLog = false;
    //是否打开gatt回调日志
    public static boolean isOpenGattCallbackLog = false;
}
