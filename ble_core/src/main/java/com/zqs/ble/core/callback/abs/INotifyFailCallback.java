package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothGattCharacteristic;

/*
 *   @author zhangqisheng
 *   @date 2022-07-15
 *   @description
 */
@FunctionalInterface
public interface INotifyFailCallback {

    void onNotifyFail(BluetoothGattCharacteristic characteristic,int status);

}
