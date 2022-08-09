package com.zqs.ble.impl;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import com.zqs.ble.core.api.IBleMessageSender;
import com.zqs.ble.core.api.IBleOption;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.scan.WrapScanConfig;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.deamon.message.option.ConnectMessage;
import com.zqs.ble.core.deamon.message.option.DisconnectMessage;
import com.zqs.ble.core.deamon.message.option.IOptionMessage;
import com.zqs.ble.core.deamon.message.option.ReadChacMessage;
import com.zqs.ble.core.deamon.message.option.ReadDescMessage;
import com.zqs.ble.core.deamon.message.option.ReadPhyMessage;
import com.zqs.ble.core.deamon.message.option.ReadRssiMessage;
import com.zqs.ble.core.deamon.message.option.RequestMtuMessage;
import com.zqs.ble.core.deamon.message.option.SetConnectionPriorityMessage;
import com.zqs.ble.core.deamon.message.option.SetNotificationMessage;
import com.zqs.ble.core.deamon.message.option.SetPhyMessage;
import com.zqs.ble.core.deamon.message.option.WriteChacLockMessage;
import com.zqs.ble.core.deamon.message.option.WriteChacMessage;
import com.zqs.ble.core.deamon.message.option.WriteDescMessage;
import com.zqs.ble.core.deamon.message.order.FrontMessage;
import com.zqs.ble.core.deamon.message.scan.StartScanMessage;
import com.zqs.ble.core.deamon.message.scan.StopScanMessage;
import com.zqs.ble.core.utils.fun.Function2;
import com.zqs.ble.core.utils.fun.Function3;

import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-16
 *   @description
 */
public class DefaultBleOption implements IBleOption {

    private IBleMessageSender sender;

    public DefaultBleOption(IBleMessageSender sender) {
        this.sender = sender;
    }

    private void sendMessage(AbsMessage message){
        sender.sendMessage(message);
    }

    @Override
    public void connect(String mac, long timeout, int reconnectCount, Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback) {
        ConnectMessage message = new ConnectMessage(mac, reconnectCount);
        message.setConnectTimeout(timeout);
        message.setConnectFailCallback(connectFailCallback);
        sendMessage(message);
    }

    @Override
    public void disconnect(String mac) {
        sendMessage(new FrontMessage(){
            @Override
            public void onHandlerMessage() {
                DisconnectMessage message = new DisconnectMessage(mac);
                sender.clearMessageIf((msg)-> msg instanceof IOptionMessage
                        && ((AbsBleMessage) msg).getMac().equalsIgnoreCase(message.getMac())
                        && this.getAddQueueTime() > msg.getAddQueueTime(),()->sendMessage(message));
            }
        });
    }

    @Override
    public void write(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount) {
        WriteChacMessage message = new WriteChacMessage(mac, serviceUuid, chacUuid, value);
        message.setRetryWriteCount(retryWriteCount);
        sendMessage(message);
    }

    @Override
    public void writeNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount) {
        WriteChacMessage message = new WriteChacMessage(mac, serviceUuid, chacUuid, value);
        message.setRetryWriteCount(retryWriteCount);
        message.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        sendMessage(message);
    }

    @Override
    public void writeByLock(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallback) {
        WriteChacLockMessage message = new WriteChacLockMessage(mac, serviceUuid, chacUuid, value);
        message.setRetryWriteCount(retryWriteCount);
        if (writeCallback!=null){
            message.setWriteCallback(writeCallback);
        }
        sendMessage(message);
    }

    @Override
    public void writeByLockNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallback) {
        WriteChacLockMessage message = new WriteChacLockMessage(mac, serviceUuid, chacUuid, value);
        message.setRetryWriteCount(retryWriteCount);
        message.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        if (writeCallback!=null){
            message.setWriteCallback(writeCallback);
        }
        sendMessage(message);
    }

    @Override
    public void writeDesc(String mac, UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value) {
        WriteDescMessage message = new WriteDescMessage(mac, serviceUuid, chacUuid, descUuid, value);
        sendMessage(message);
    }

    @Override
    public void read(String mac, UUID serviceUuid, UUID chacUuid) {
        ReadChacMessage message = new ReadChacMessage(mac, serviceUuid, chacUuid);
        sendMessage(message);
    }

    @Override
    public void readDesc(String mac, UUID serviceUuid, UUID chacUuid, UUID descUuid) {
        ReadDescMessage message = new ReadDescMessage(mac, serviceUuid, chacUuid, descUuid);
        sendMessage(message);
    }

    @Override
    public void openNotify(String mac, UUID serviceUuid, UUID chacUuid) {
        SetNotificationMessage message = new SetNotificationMessage(mac, serviceUuid, chacUuid, true);
        sendMessage(message);
    }

    @Override
    public void cancelNotify(String mac, UUID serviceUuid, UUID chacUuid) {
        SetNotificationMessage message = new SetNotificationMessage(mac, serviceUuid, chacUuid, false);
        sendMessage(message);
    }

    @Override
    public void setMtu(String mac,int mtu){
        RequestMtuMessage message = new RequestMtuMessage(mac, mtu);
        sendMessage(message);
    }

    @Override
    public void readRssi(String mac) {
        ReadRssiMessage message = new ReadRssiMessage(mac);
        sendMessage(message);
    }

    @Override
    public void readPhy(String mac) {
        sendMessage(new ReadPhyMessage(mac));
    }

    @Override
    public void requestConnectionPriority(String mac, int connectionPriority) {
        sendMessage(new SetConnectionPriorityMessage(mac, connectionPriority));
    }

    @Override
    public void setPreferredPhy(String mac, int txPhy, int rxPhy, int phyOptions) {
        sendMessage(new SetPhyMessage(mac, txPhy, rxPhy, phyOptions));
    }

    @Override
    public void startScan(long time, IScanCallback callback, WrapScanConfig filter) {
        StartScanMessage message = new StartScanMessage(callback, time);
        message.setWrapFilter(filter);
        sendMessage(message);
    }

    @Override
    public void startScanOnlyLollipop(long time, List<ScanFilter> filters, ScanSettings settings, IScanCallback callback) {
        StartScanMessage message = new StartScanMessage(callback, time);
        message.setFilters(filters);
        message.setSettings(settings);
        sendMessage(message);
    }

    @Override
    public void stopScan() {
        sender.clearMessageIf((msg)->msg instanceof StopScanMessage,()->sendMessage(new StopScanMessage()));
    }

}
