package com.zqs.ble.core.callback.scan;

import android.annotation.SuppressLint;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-29
 *   @description
 */
public class WrapScanConfig {

    //过滤的设备名称
    private String deviceName;
    //过滤的设备名称
    private String mac;
    //过滤的serviceUuid
    private UUID serviceUuid;

    private Boolean isRepeatCallback;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Boolean isRepeatCallback() {
        return isRepeatCallback;
    }

    public void setRepeatCallback(boolean isRepeatCallback) {
        this.isRepeatCallback = isRepeatCallback;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public UUID getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(UUID serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    @SuppressLint("NewApi")
    List<android.bluetooth.le.ScanFilter> toScanFilter(){
        List list = new ArrayList();
        android.bluetooth.le.ScanFilter.Builder builder = new android.bluetooth.le.ScanFilter.Builder();
        if (deviceName!=null){
            builder.setDeviceName(deviceName);
        }
        if (mac!=null){
            builder.setDeviceAddress(mac);
        }
        if (serviceUuid !=null){
            builder.setServiceUuid(new ParcelUuid(serviceUuid));
        }
        list.add(builder.build());
        return list;
    }

}
