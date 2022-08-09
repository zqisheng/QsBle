package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.core.deamon.AbsBleMessage;

import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class ReadChacMessage extends AbsBleMessage implements IOptionMessage  {

    private UUID serviceUuid;
    private UUID chacUuid;

    public ReadChacMessage(String mac,UUID serviceUuid, UUID chacUuid){
        super(mac);
        this.serviceUuid = serviceUuid;
        this.chacUuid = chacUuid;
    }

    public ReadChacMessage(String mac,String serviceUuid, String chacUuid){
        this(mac,UUID.fromString(serviceUuid), UUID.fromString(chacUuid));
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BluetoothGatt gatt = getGatt();
        if (gatt==null)return;
        BluetoothGattCharacteristic characteristic = getGattCharacteristic(serviceUuid, chacUuid);
        if (characteristic!=null){
            gatt.readCharacteristic(characteristic);
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }

}
