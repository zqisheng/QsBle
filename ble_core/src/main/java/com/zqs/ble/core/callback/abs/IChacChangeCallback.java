package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IChacChangeCallback extends IBleOptionCallback {

    void onCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic,byte[] value);

}
