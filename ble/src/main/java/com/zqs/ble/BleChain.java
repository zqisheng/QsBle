package com.zqs.ble;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public abstract class BleChain<T> extends BaseChain<T> {

    private String mac;

    public BleChain(String mac){
        this.mac = mac;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}
