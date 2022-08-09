package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.core.deamon.AbsBleMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class DiscoverServiceMessage extends AbsBleMessage implements IOptionMessage  {

    private int retryCount;

    public DiscoverServiceMessage(String mac,int retryCount){
        super(mac);
        this.retryCount = retryCount;
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (!getSimpleBle().isConnect(getMac())) return;
        if (retryCount<0) return;
        BluetoothGatt gatt = getGatt();
        if (gatt==null)return;
        boolean result = gatt.discoverServices();
        if (!result&&retryCount>0){
            DiscoverServiceMessage message = new DiscoverServiceMessage(getMac(), retryCount - 1);
            getSimpleBle().sendMessage(message);
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }
}
