package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.core.deamon.AbsBleMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description 仅仅在sdk level 大于等于 26有效
 */
public class SetPhyMessage extends AbsBleMessage implements IOptionMessage {

    private int txPhy;
    private int rxPhy;
    private int phyOptions;


    public SetPhyMessage(String mac, int txPhy, int rxPhy, int phyOptions) {
        super(mac);
        this.txPhy = txPhy;
        this.rxPhy = rxPhy;
        this.phyOptions = phyOptions;
    }

    @Override
    public void onHandlerMessage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertCurrentIsSenderThread();
            BluetoothGatt gatt = getGatt();
            if (gatt == null) return;
            gatt.setPreferredPhy(txPhy, rxPhy, phyOptions);
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac()) && super.isLive();
    }

}
