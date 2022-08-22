package com.zqs.ble;

import com.zqs.ble.core.utils.fun.IMessageOption;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public abstract class BleChain<T> extends BaseChain<T> {

    private String mac;
    private IMessageOption messageOption;

    public BleChain(String mac){
        this.mac = mac;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    protected void setMessageOption(IMessageOption messageOption){
        this.messageOption = messageOption;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messageOption!=null){
            messageOption.cancel();
        }
    }
}
