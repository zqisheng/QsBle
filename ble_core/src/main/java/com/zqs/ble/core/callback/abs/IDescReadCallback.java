package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IDescReadCallback extends IBleOptionCallback {

    void onDescriptorRead(BluetoothDevice device, BluetoothGattDescriptor descriptor, int status, byte[] value);

}
