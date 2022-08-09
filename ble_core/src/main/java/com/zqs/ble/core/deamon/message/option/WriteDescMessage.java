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
public class WriteDescMessage extends AbsBleMessage implements IOptionMessage  {

    private UUID serviceUuid;
    private UUID chacUuid;
    private UUID descUuid;
    private byte[] value;

    public WriteDescMessage(String mac, UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value) {
        super(mac);
        this.serviceUuid = serviceUuid;
        this.chacUuid = chacUuid;
        this.descUuid = descUuid;
        this.value = value;
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BluetoothGatt gatt = getGatt();
        if (gatt==null)return;
        BluetoothGattDescriptor descriptor = getGattDescriptor(serviceUuid, chacUuid, descUuid);
        if (descriptor!=null){
            descriptor.setValue(value);
            gatt.writeDescriptor(descriptor);
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }
}
