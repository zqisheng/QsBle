package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.zqs.ble.core.BleConst;
import com.zqs.ble.core.callback.abs.INotifyFailCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;

import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-15
 *   @description
 */
public class SetNotificationMessage extends AbsBleMessage {

    private UUID serviceUuid;
    private UUID chacUuid;
    private boolean enable;

    public SetNotificationMessage(String mac,UUID serviceUuid, UUID chacUuid, boolean enable){
        super(mac);
        this.serviceUuid = serviceUuid;
        this.chacUuid = chacUuid;
        this.enable = enable;
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BluetoothGatt gatt = getGatt();
        if (gatt==null)return;
        BluetoothGattCharacteristic chac = getGattCharacteristic(serviceUuid, chacUuid);
        if (chac==null)return;
        boolean isSetSuccess = true;
        if (gatt.setCharacteristicNotification(chac, enable)){
            BluetoothGattDescriptor descriptor = getGattDescriptor(serviceUuid, chacUuid, BleConst.clientCharacteristicConfig);
            if (descriptor!=null){
                if (enable){
                    if ((chac.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0){
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (!gatt.writeDescriptor(descriptor)){
                            isSetSuccess = false;
                        }
                    }else if ((chac.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0){
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        if (!gatt.writeDescriptor(descriptor)){
                            isSetSuccess = false;
                        }
                    }
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    if (!gatt.writeDescriptor(descriptor)){
                        isSetSuccess = false;
                    }
                }
            }else{
                isSetSuccess = false;

            }
        }else{
            isSetSuccess = false;
        }
        if (!isSetSuccess){
            List<INotifyFailCallback> callbacks = getSimpleBle().getCallbackManage().getNotifyFailCallbacks(getMac());
            for (INotifyFailCallback callback : callbacks) {
                callback.onNotifyFail(chac, -1);
            }
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }

}
