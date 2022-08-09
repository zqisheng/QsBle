package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.core.deamon.AbsBleMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description Android LOLLIPOP版本以下的无效
 */
public class RequestMtuMessage extends AbsBleMessage implements IOptionMessage  {

    private int mtu;

    public RequestMtuMessage(String mac,int mtu) {
        super(mac);
        this.mtu = mtu;
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothGatt gatt = getGatt();
            if (gatt!=null){
                gatt.requestMtu(mtu);
            }
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }
}
