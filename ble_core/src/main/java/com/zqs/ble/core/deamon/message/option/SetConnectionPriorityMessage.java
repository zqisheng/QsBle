package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.core.deamon.AbsBleMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-07-29
 *   @description
 */
public class SetConnectionPriorityMessage extends AbsBleMessage {

    private Integer connectionPriority;

    public SetConnectionPriorityMessage(String mac, int connectionPriority) {
        super(mac);
        this.connectionPriority = connectionPriority;
    }

    @Override
    public void onHandlerMessage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothGatt gatt = getGatt();
            if (gatt==null)return;
            gatt.requestConnectionPriority(connectionPriority);
        }
    }
}
