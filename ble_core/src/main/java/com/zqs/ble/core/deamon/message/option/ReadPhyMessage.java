package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.core.deamon.AbsBleMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description 仅仅在sdk level 大于等于 26有效
 */
public class ReadPhyMessage extends AbsBleMessage implements IOptionMessage  {

    public ReadPhyMessage(String mac){
        super(mac);
    }

    @Override
    public void onHandlerMessage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertCurrentIsSenderThread();
            BluetoothGatt gatt = getGatt();
            if (gatt==null)return;
            gatt.readPhy();
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }

}
