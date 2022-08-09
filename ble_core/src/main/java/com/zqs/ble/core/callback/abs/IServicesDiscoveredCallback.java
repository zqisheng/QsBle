package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IServicesDiscoveredCallback extends IBleOptionCallback {

    void onServicesDiscovered(BluetoothDevice device, List<BluetoothGattService> services, int status);


}
