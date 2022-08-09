package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IMtuChangeCallback extends IBleOptionCallback {
    void onMtuChanged(BluetoothDevice device, int mtu, int status);
}
