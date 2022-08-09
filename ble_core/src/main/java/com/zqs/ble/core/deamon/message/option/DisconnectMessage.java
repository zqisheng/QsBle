package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.deamon.message.order.IFrontMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class DisconnectMessage extends AbsBleMessage implements IFrontMessage {

    public DisconnectMessage(String mac) {
        super(mac);
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BluetoothGatt gatt = getGatt();
        if (gatt!=null){
            gatt.disconnect();
        }
        getSimpleBle().updateConnectStatus(getMac(), false, 0, 0);
    }
}
