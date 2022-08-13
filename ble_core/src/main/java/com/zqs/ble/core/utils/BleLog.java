package com.zqs.ble.core.utils;

import android.util.Log;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.utils.fun.DebugFunction;

/*
 *   @author zhangqisheng
 *   @date 2022-07-13
 *   @description
 */
public class BleLog {

    private static String LOG_TAG="QsBle";

    public static void i(String msg){
        i(LOG_TAG, msg);
    }

    public static void e(String msg){
        e(LOG_TAG, msg);
    }

    public static void d(String msg) {
        d(LOG_TAG, msg);
    }

    public static void w(String msg) {
        w(LOG_TAG, msg);
    }

    public static void i(String tag,String msg){
        if (BleDebugConfig.isDebug){
            Log.i(tag, getFuntionStack()+msg);
        }
    }

    public static void e(String tag,String msg){
        if (BleDebugConfig.isDebug){
            Log.e(tag, getFuntionStack()+msg);
        }
    }

    public static void d(String tag,String msg) {
        if (BleDebugConfig.isDebug){
            Log.d(tag, getFuntionStack()+msg);
        }
    }

    public static void w(String tag,String msg) {
        if (BleDebugConfig.isDebug){
            Log.w(tag, getFuntionStack()+msg);
        }
    }

    private static String getFuntionStack(){
        if (!BleDebugConfig.isPrintFunStack) return "";
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement st : stackTraceElements) {
                if (st.isNativeMethod()) {
                    continue;
                }
                if (st.getClassName().equals(Thread.class.getName())) {
                    continue;
                }
                if (st.getClassName().equals(BleLog.class.getName())) {
                    continue;
                }
                //return st.className + " (" + st.fileName + ":" + st.lineNumber + ")"
                return ("[ Thread:" + Thread.currentThread().getName() + ", at " + st.getClassName() + "." + st.getMethodName()
                        + "(" + st.getFileName() + ":" + st.getLineNumber() + ")" + " ]\n");
            }
        }
        return "";
    }

}
