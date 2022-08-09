package com.zqs.ble.core.api;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-07-15
 *   @description
 */
public interface IMultiPackageAssembly {

    void onChanged(BluetoothGattCharacteristic chac, byte[] value);

    boolean hasNext(byte[] value);

    List<byte[]> getResult();

}
