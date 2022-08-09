package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.Utils;

import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class WriteChacMessage extends AbsBleMessage implements IOptionMessage  {

    private UUID serviceUuid;
    private UUID chacUuid;
    private byte[] sendPkg;
    private int retryWriteCount = 0;
    private int writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;

    public WriteChacMessage(String mac, UUID serviceUuid, UUID chacUuid,byte[] value) {
        super(mac);
        this.serviceUuid = serviceUuid;
        this.chacUuid = chacUuid;
        sendPkg=Utils.expandBytes(value, (byte) 0, getSimpleBle().getCurrentMtu());
    }

    public int getWriteType() {
        return writeType;
    }

    public void setWriteType(int writeType) {
        this.writeType = writeType;
    }

    public int getRetryWriteCount() {
        return retryWriteCount;
    }

    public void setRetryWriteCount(int retryWriteCount) {
        this.retryWriteCount = retryWriteCount;
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (!getSimpleBle().isConnect(getMac())) return;
        BluetoothGatt gatt = getGatt();
        if (gatt==null)return;
        BluetoothGattCharacteristic characteristic = getGattCharacteristic(serviceUuid, chacUuid);
        if (characteristic!=null){
            characteristic.setValue(sendPkg);
            characteristic.setWriteType(writeType);
            boolean result = gatt.writeCharacteristic(characteristic);
            //方法返回层面的重写
            if (!result){
                while (retryWriteCount>0&&gatt.writeCharacteristic(characteristic)){
                    --retryWriteCount;
                }
            }
        }
    }

    @Override
    public boolean isLive() {
        return getSimpleBle().isConnect(getMac())&&super.isLive();
    }

}
