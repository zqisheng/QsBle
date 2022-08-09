package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.core.deamon.AbsBleMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class ReadRssiMessage extends AbsBleMessage implements IOptionMessage  {

    public ReadRssiMessage(String mac) {
        super(mac);
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BluetoothGatt gatt = getGatt();
        if (gatt!=null){
            gatt.readRemoteRssi();
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }
}
