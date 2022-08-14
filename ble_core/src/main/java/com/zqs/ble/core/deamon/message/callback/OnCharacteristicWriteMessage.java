package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IChacWriteCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnCharacteristicWriteMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;
    private BluetoothGattCharacteristic characteristic;
    private byte[] value;
    private int status;

    public OnCharacteristicWriteMessage(BluetoothDevice device, BluetoothGattCharacteristic characteristic,byte[] value, int status){
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
            BleLog.d(String.format("BleCallback OnCharacteristicWriteMessage:mac=%s,status=%d,chac=%s,value=%s", device.getAddress(), status, characteristic.getUuid().toString(), Utils.bytesToHexStr(value)));
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onCharacteristicWrite(device,characteristic,status);
        }
        IChacWriteCallback lockCallback = getSimpleBle().getCallbackManage().getLockWriteChacCallback(getMac());
        if (lockCallback!=null){
            lockCallback.onCharacteristicWrite(device, characteristic,value, status);
        }
        List<IChacWriteCallback> callbacks = getSimpleBle().getCallbackManage().getChacWriteCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IChacWriteCallback callback:callbacks){
                callback.onCharacteristicWrite(device,characteristic,value,status);
            }
        }
    }
}
