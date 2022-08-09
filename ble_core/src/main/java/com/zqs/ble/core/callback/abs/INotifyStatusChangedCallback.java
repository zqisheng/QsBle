package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;

/*
 *   @author zhangqisheng
 *   @date 2022-07-15
 *   @description
 */
@FunctionalInterface
public interface INotifyStatusChangedCallback {

    void onNotifyStatusChanged(BluetoothDevice device,BluetoothGattDescriptor descriptor, boolean notifyEnable);

}
