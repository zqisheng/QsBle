package com.zqs.ble.core.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/*
 *   @author zhangqisheng
 *   @date 2022-07-19
 *   @description
 */
public final class GlobalBleCallback extends BluetoothGattCallback {


    public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {

    }

    public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {

    }

    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {

    }

    public void onServicesDiscovered(BluetoothDevice device, int status) {

    }

    public void onCharacteristicRead(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int status) {

    }

    public void onCharacteristicWrite(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int status) {

    }

    public void onCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic) {

    }

    public void onDescriptorRead(BluetoothDevice device, BluetoothGattDescriptor descriptor, int status) {

    }

    public void onDescriptorWrite(BluetoothDevice device, BluetoothGattDescriptor descriptor,byte[] value, int status) {

    }

    public void onReliableWriteCompleted(BluetoothDevice device, int status) {

    }

    public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status) {

    }

    public void onMtuChanged(BluetoothDevice device, int mtu, int status) {

    }

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord, Object lollipopScanResult){

    }

    public void onScanError(int errorCode){

    }

    public void onScanStatusChanged(boolean isScanning){

    }

    public void onBluetoothStatusChanged(boolean isOpen){

    }


    @Override
    public final void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        onPhyUpdate(gatt.getDevice(), txPhy, rxPhy, status);
    }

    @Override
    public final void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        onPhyRead(gatt.getDevice(), txPhy, rxPhy, status);
    }

    @Override
    public final void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        onConnectionStateChange(gatt.getDevice(), status, newState);
    }

    @Override
    public final void onServicesDiscovered(BluetoothGatt gatt, int status) {
        onServicesDiscovered(gatt.getDevice(), status);
    }

    @Override
    public final void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        onCharacteristicRead(gatt.getDevice(), characteristic, status);
    }

    @Override
    public final void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        onCharacteristicWrite(gatt.getDevice(), characteristic, status);
    }

    @Override
    public final void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        onCharacteristicChanged(gatt.getDevice(), characteristic);
    }

    @Override
    public final void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        onDescriptorRead(gatt.getDevice(), descriptor, status);
    }

    @Override
    public final void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        onDescriptorWrite(gatt.getDevice(), descriptor,descriptor.getValue(), status);
    }

    @Override
    public final void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        onReliableWriteCompleted(gatt.getDevice(), status);
    }

    @Override
    public final void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        onReadRemoteRssi(gatt.getDevice(), rssi, status);
    }

    @Override
    public final void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        onMtuChanged(gatt.getDevice(), mtu, status);
    }
    
}
