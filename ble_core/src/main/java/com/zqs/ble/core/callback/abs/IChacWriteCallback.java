package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IChacWriteCallback extends IBleOptionCallback {

    void onCharacteristicWrite(BluetoothDevice device, BluetoothGattCharacteristic characteristic,byte[] value, int status);

}
