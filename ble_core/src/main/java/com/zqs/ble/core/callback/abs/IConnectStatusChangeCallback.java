package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IConnectStatusChangeCallback extends IBleOptionCallback {

    void onConnectStatusChanged(BluetoothDevice device, boolean isConnect, int status, int profileState);

}
