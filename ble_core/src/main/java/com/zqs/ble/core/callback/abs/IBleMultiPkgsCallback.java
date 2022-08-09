package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-07-16
 *   @description
 */
public interface IBleMultiPkgsCallback {

    void onResult(BluetoothDevice device, BluetoothGattCharacteristic characteristic, List<byte[]> result);

}
