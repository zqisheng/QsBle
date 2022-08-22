package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
        boolean oldConnect = getSimpleBle().isConnect(getMac());
        BluetoothGatt gatt = getGatt();
        if (gatt!=null){
            gatt.disconnect();
        }
        if (oldConnect){
            getSimpleBle().updateConnectStatus(getMac(), false, 0, 0, "app");
        }
    }
}
