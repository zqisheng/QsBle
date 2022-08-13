package com.zqs.ble.core;

/*
 *   @author zhangqisheng
 *   @date 2022-08-13
 *   @description
 */
public class BleDevice {

    private String mac;
    private boolean isConnect;
    private int[] lastGattCode;
    private long connectStatusUpdateTime= 0;
    //app,device,null
    private String disconnecter = null;

    private int autoConnectCount = BleGlobalConfig.autoReconnectCount;

    public int getAutoConnectCount() {
        return autoConnectCount;
    }

    public void setAutoConnectCount(int autoConnectCount) {
        this.autoConnectCount = autoConnectCount;
    }

    public BleDevice(String mac, boolean isConnect){
        this.mac = mac;
        this.isConnect = isConnect;
    }

    public String getDisconnecter() {
        return disconnecter;
    }

    public void setDisconnecter(String disconnecter) {
        this.disconnecter = disconnecter;
    }

    public long getConnectStatusUpdateTime() {
        return connectStatusUpdateTime;
    }

    public void setConnectStatusUpdateTime(long connectStatusUpdateTime) {
        this.connectStatusUpdateTime = connectStatusUpdateTime;
    }

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
    }

    public int[] getLastGattCode() {
        return lastGattCode;
    }

    public void setLastGattCode(int[] lastGattCode) {
        this.lastGattCode = lastGattCode;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }
}
