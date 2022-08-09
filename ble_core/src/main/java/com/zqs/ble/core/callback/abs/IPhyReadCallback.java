package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IPhyReadCallback extends IBleOptionCallback {

    void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status);

}
