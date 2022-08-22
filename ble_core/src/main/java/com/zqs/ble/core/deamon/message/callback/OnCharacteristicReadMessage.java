package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IChacReadCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnCharacteristicReadMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;

    private BluetoothGattCharacteristic characteristic;

    private int status;
    private byte[] value;

    public OnCharacteristicReadMessage(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int status, byte[] value){
        super(device.getAddress());
        this.device=device;
        this.characteristic=characteristic;
        this.status=status;
        this.value = value;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("BleCallback OnCharacteristicReadMessage:mac=%s,chac=%s,value=%s", device.getAddress(), characteristic.getUuid().toString(), Utils.bytesToHexStr(value)));
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onCharacteristicRead(device.getAddress(), characteristic, status);
        }
        List<IChacReadCallback> callbacks = getSimpleBle().getCallbackManage().getChacReadCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IChacReadCallback callback:callbacks){
                callback.onCharacteristicRead(device,characteristic,status,value);
            }
        }
    }
}
