package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
public interface IReadRssiCallback extends IBleOptionCallback {

    void onReadRssi(BluetoothDevice device, int rssi, int status);

}
