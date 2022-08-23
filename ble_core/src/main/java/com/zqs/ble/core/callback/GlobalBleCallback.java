package com.zqs.ble.core.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-07-19
 *   @description
 */
public class GlobalBleCallback extends BluetoothGattCallback {


    public void onPhyUpdate(String mac, int txPhy, int rxPhy, int status) {

    }

    public void onPhyRead(String mac, int txPhy, int rxPhy, int status) {

    }

    public void onConnectionStateChange(String mac, int status, int newState) {

    }

    public void onServicesDiscovered(String mac, List<BluetoothGattService> services, int status) {

    }

    public void onCharacteristicRead(String mac, BluetoothGattCharacteristic characteristic, int status) {

    }

    public void onCharacteristicWrite(String mac, BluetoothGattCharacteristic characteristic, int status) {

    }

    public void onCharacteristicChanged(String mac, BluetoothGattCharacteristic characteristic) {

    }

    public void onDescriptorRead(String mac, BluetoothGattDescriptor descriptor, int status) {

    }

    public void onDescriptorWrite(String mac, BluetoothGattDescriptor descriptor,byte[] value, int status) {

    }

    public void onReliableWriteCompleted(String mac, int status) {

    }

    public void onReadRemoteRssi(String mac, int rssi, int status) {

    }

    public void onMtuChanged(String mac, int mtu, int status) {

    }

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){

    }

    public void onScanError(int errorCode){

    }

    public void onScanStatusChanged(boolean isScanning){

    }

    public void onBluetoothStatusChanged(boolean isOpen){

    }
    
}
