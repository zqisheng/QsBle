package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IChacReadCallback extends IBleOptionCallback {

    void onCharacteristicRead(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int status, byte[] value);

}
