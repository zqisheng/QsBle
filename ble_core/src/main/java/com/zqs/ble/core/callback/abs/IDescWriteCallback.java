package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IDescWriteCallback extends IBleOptionCallback {

    void onDescriptorWrite(BluetoothDevice device, BluetoothGattDescriptor descriptor,byte[] value, int status);

}
