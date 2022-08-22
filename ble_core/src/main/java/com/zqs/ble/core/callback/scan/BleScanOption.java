package com.zqs.ble.core.callback.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import com.zqs.ble.core.SimpleBle;
import com.zqs.ble.core.callback.abs.IScanCallback;

import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
public class BleScanOption {

    private SimpleBle simpleBle;

    public void setSimpleBle(SimpleBle simpleBle){
        this.simpleBle = simpleBle;
    }

    public void startScan() {
        startScan(null,null);
    }

    public void startScan(IScanCallback callback, SimpleScanConfig config) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            c.setSimpleBle(simpleBle);
            if (callback!=null){
                c.setScanCallback(callback);
            }
            c.setConfig(config);
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(config != null ? config.toScanFilter() : null, new ScanSettings.Builder().build(), c);
        }else{
            BleJellyBeanScanCallback c = BleJellyBeanScanCallback.INSTANCE;
            c.setSimpleBle(simpleBle);
            if (callback!=null){
                c.setScanCallback(callback);
            }
            c.setConfig(config);
            if (config==null){
                BluetoothAdapter.getDefaultAdapter().startLeScan(c);
            }else{
                if (config.getServiceUuid()!=null){
                    BluetoothAdapter.getDefaultAdapter().startLeScan(new UUID[]{config.getServiceUuid()}, c);
                }
            }
        }
    }

    //只有在版本大于或者等于LOLLIPOP level=21 android5.0生效
    public void startScanOnlyLollipop(List<android.bluetooth.le.ScanFilter> filters, ScanSettings settings, IScanCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            if (callback!=null){
                c.setScanCallback(callback);
            }
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(filters,settings,c);
        }
    }

    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            c.setScanCallback(null);
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(c);
        }else{
            BleJellyBeanScanCallback c = BleJellyBeanScanCallback.INSTANCE;
            c.setSimpleBle(simpleBle);
            c.setScanCallback(null);
            BluetoothAdapter.getDefaultAdapter().startLeScan(c);
        }
    }

    public IScanCallback getScanCallback(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            return c.getScanCallback();
        }else{
            BleJellyBeanScanCallback c = BleJellyBeanScanCallback.INSTANCE;
            return c.getScanCallback();
        }
    }

    public void removeScanCallback(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            c.setScanCallback(null);
        }else{
            BleJellyBeanScanCallback c = BleJellyBeanScanCallback.INSTANCE;
            c.setScanCallback(null);
        }
    }

    public void onScanStop(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            c.onScanStop();
        }else{
            BleJellyBeanScanCallback c = BleJellyBeanScanCallback.INSTANCE;
            c.onScanStop();
        }
    }

    public void onScanStart(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleLollipopScanCallback c = BleLollipopScanCallback.INSTANCE;
            c.onScanStart();
        }else{
            BleJellyBeanScanCallback c = BleJellyBeanScanCallback.INSTANCE;
            c.onScanStart();
        }
    }


}
