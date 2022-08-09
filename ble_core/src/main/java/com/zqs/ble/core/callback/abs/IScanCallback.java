package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;

/*
 *   @author zhangqisheng
 *   @date 2022-02-23
 *   @description
 */
@FunctionalInterface
public interface IScanCallback extends IBleOptionCallback {

    void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);

}
