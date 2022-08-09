package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.zqs.ble.core.deamon.AbsBleMessage;

import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class ReadDescMessage extends AbsBleMessage implements IOptionMessage  {

    private UUID serviceUuid;
    private UUID chacUuid;
    private UUID descUuid;

    public ReadDescMessage(String mac,UUID serviceUuid, UUID chacUuid, UUID descUuid){
        super(mac);
        this.serviceUuid = serviceUuid;
        this.chacUuid = chacUuid;
        this.descUuid = descUuid;
    }

    public ReadDescMessage(String mac,String serviceUuid, String chacUuid, String descUuid){
        this(mac,UUID.fromString(serviceUuid), UUID.fromString(chacUuid), UUID.fromString(descUuid));
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BluetoothGatt gatt = getGatt();
        if (gatt==null)return;
        BluetoothGattDescriptor descriptor = getGattDescriptor(serviceUuid, chacUuid, descUuid);
        if (descriptor!=null){
            gatt.readDescriptor(descriptor);
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }
}
