package com.zqs.ble.core.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.zqs.ble.core.api.IMessageSender;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.deamon.message.callback.OnCharacteristicChangedMessage;
import com.zqs.ble.core.deamon.message.callback.OnCharacteristicReadMessage;
import com.zqs.ble.core.deamon.message.callback.OnCharacteristicWriteMessage;
import com.zqs.ble.core.deamon.message.callback.OnConnectionStateMessage;
import com.zqs.ble.core.deamon.message.callback.OnConnectionUpdatedMessage;
import com.zqs.ble.core.deamon.message.callback.OnDescriptorReadMessage;
import com.zqs.ble.core.deamon.message.callback.OnDescriptorWriteMessage;
import com.zqs.ble.core.deamon.message.callback.OnMtuChangedMessage;
import com.zqs.ble.core.deamon.message.callback.OnPhyReadMessage;
import com.zqs.ble.core.deamon.message.callback.OnPhyUpdateMessage;
import com.zqs.ble.core.deamon.message.callback.OnReadRemoteRssiMessage;
import com.zqs.ble.core.deamon.message.callback.OnServicesDiscoveredMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-02-24
 *   @description 统一拦截所有的ble回调，包括全局的，单个设备的
 */
public class BleGattCallback extends BluetoothGattCallback {

    private static IMessageSender sender;

    public static void setSimpleBle(IMessageSender sender){
        BleGattCallback.sender = sender;
    }

    public BleGattCallback() {
        super();
    }

    //负责给外部调用覆盖的
    public void onWrapPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        sendMessage(new OnPhyUpdateMessage(gatt.getDevice(), txPhy,rxPhy,status));
    }

    public void onWrapPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        sendMessage(new OnPhyReadMessage(gatt.getDevice(), txPhy,rxPhy,status));
    }

    public void onWrapConnectionStateChange(BluetoothGatt gatt, int status, int profileState) {
        sendMessage(new OnConnectionStateMessage(gatt.getDevice(), status, profileState));
    }

    public void onWrapServicesDiscovered(BluetoothGatt gatt, int status) {
        sendMessage(new OnServicesDiscoveredMessage(gatt.getDevice(),gatt.getServices(), status));
    }

    public void onWrapCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        sendMessage(new OnCharacteristicReadMessage(gatt.getDevice(), characteristic, status, characteristic.getValue()));
    }

    public void onWrapCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        sendMessage(new OnCharacteristicWriteMessage(gatt.getDevice(), characteristic,characteristic.getValue(), status));
    }

    public void onWrapCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        sendMessage(new OnCharacteristicChangedMessage(gatt.getDevice(), characteristic,characteristic.getValue()));
    }

    public void onWrapDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        sendMessage(new OnDescriptorReadMessage(gatt.getDevice(), descriptor, status,descriptor.getValue()));
    }

    public void onWrapDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        sendMessage(new OnDescriptorWriteMessage(gatt.getDevice(), descriptor, descriptor.getValue(), status));
    }

    public void onWrapReliableWriteCompleted(BluetoothGatt gatt, int status) {

    }

    public void onWrapReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        sendMessage(new OnReadRemoteRssiMessage(gatt.getDevice(),rssi, status));
    }

    public void onWrapMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        sendMessage(new OnMtuChangedMessage(gatt.getDevice(), mtu, status));
    }

    /**
     * 部分高版本的Android会调用,尽量不要在业务中使用,该回调仅仅是用在设备端连接参数的展示
     *
     * @param gatt
     * @param interval 连接间隔
     * @param latency  连接时延
     * @param timeout  连接超时
     * @param status
     */
    public void onWrapConnectionUpdated(BluetoothGatt gatt, int interval, int latency, int timeout, int status) {
        sendMessage(new OnConnectionUpdatedMessage(gatt.getDevice(), interval, latency, timeout, status));
    }


    @Override
    public final void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        onWrapPhyUpdate(gatt, txPhy, rxPhy, status);
    }

    @Override
    public final void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyRead(gatt, txPhy, rxPhy, status);
        onWrapPhyRead(gatt, txPhy, rxPhy, status);
    }

    @Override
    public final void onConnectionStateChange(BluetoothGatt gatt, int status, int profileState) {
        super.onConnectionStateChange(gatt, status, profileState);
        onWrapConnectionStateChange(gatt, status, profileState);
    }

    @Override
    public final void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        onWrapServicesDiscovered(gatt, status);
    }

    @Override
    public final void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        onWrapCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public final void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        onWrapCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public final void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        onWrapCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public final void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        onWrapDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public final void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        onWrapDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public final void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        onWrapReliableWriteCompleted(gatt, status);
    }

    @Override
    public final void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        onWrapReadRemoteRssi(gatt, rssi, status);
    }

    @Override
    public final void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        onWrapMtuChanged(gatt, mtu, status);
    }

    /**
     * 部分高版本的Android会调用,尽量不要在业务中使用,该回调仅仅是用在设备端连接参数的展示
     *
     * @param gatt
     * @param interval 连接间隔
     * @param latency  连接时延
     * @param timeout  连接超时
     * @param status
     */
    public final void onConnectionUpdated(BluetoothGatt gatt, int interval, int latency, int timeout, int status) {
        onWrapConnectionUpdated(gatt, interval, latency, timeout, status);
    }

    private void sendMessage(AbsMessage message){
        sender.sendMessage(message);
    }


}
