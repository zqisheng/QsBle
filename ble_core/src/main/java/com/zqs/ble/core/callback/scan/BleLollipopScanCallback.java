package com.zqs.ble.core.callback.scan;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.SimpleBle;
import com.zqs.ble.core.api.IBleCallback;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.abs.IScanErrorCallback;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.deamon.message.scan.StopScanMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class BleLollipopScanCallback extends ScanCallback {

    final static BleLollipopScanCallback INSTANCE = new BleLollipopScanCallback();

    private BleLollipopScanCallback(){}

    IScanCallback scanCallback;
    SimpleBle simpleBle;
    private WrapScanConfig config;

    private Map<String, Boolean> record;

    public void setConfig(WrapScanConfig config) {
        this.config = config;
        if (config ==null){
            record = null;
        }else{
            record = new HashMap<>();
        }
    }
    public IScanCallback getScanCallback() {
        return scanCallback;
    }

    public SimpleBle getSimpleBle() {
        return simpleBle;
    }

    public void setSimpleBle(SimpleBle simpleBle) {
        this.simpleBle = simpleBle;
    }

    public void setScanCallback(IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (config !=null){
            //为什么要在应用层实现,系统不是提供了api吗?因为经过测试,部分android手机设置这个会直接报错
            //java.lang.SecurityException: Need BLUETOOTH_PRIVILEGED permission: Neither user 11317 nor current process has android.permission.BLUETOOTH_PRIVILEGED.
            //为了兼容考虑，故在应用层实现
            if (config.isRepeatCallback()!=null&&!config.isRepeatCallback()){
                if (record.get(result.getDevice().getAddress())!=null){
                    return;
                }
                record.put(result.getDevice().getAddress(), true);
            }
        }
        IScanCallback c1 = scanCallback;
        simpleBle.sendMessage(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                if (c1 != null) {
                    c1.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
                IBleCallback c2 = simpleBle;
                if (c2 == null) return;
                if (BleDebugConfig.isOpenScanLog){
                    BleLog.d(String.format("scan device mac=%s,rssi=%d,scanRecord=%s", result.getDevice().getAddress(), result.getRssi(), Utils.bytesToHexStr(result.getScanRecord().getBytes())));
                }
                GlobalBleCallback globalBleGattCallback = getSimpleBle().getGlobalBleGattCallback();
                if (globalBleGattCallback!=null){
                    globalBleGattCallback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
                List<IScanCallback> scanCallbacks = c2.getScanCallbacks();
                if (scanCallbacks != null) {
                    for (IScanCallback callback : scanCallbacks) {
                        callback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    }
                }
            }
        });
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);

    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        simpleBle.sendMessage(new StopScanMessage());
        simpleBle.sendMessage(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                GlobalBleCallback globalBleGattCallback = getSimpleBle().getGlobalBleGattCallback();
                if (globalBleGattCallback!=null){
                    globalBleGattCallback.onScanError(errorCode);
                }
                List<IScanErrorCallback> scanCallbacks = simpleBle.getScanErrorCallbacks();
                if (scanCallbacks != null) {
                    for (IScanErrorCallback callback : scanCallbacks) {
                        callback.onScanError(errorCode);
                    }
                }
            }
        });
    }

    public void onScanStop(){
        scanCallback = null;
    }

    public void onScanStart(){

    }

}