package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IDescReadCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnDescriptorReadMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;

    private BluetoothGattDescriptor descriptor;

    private int status;

    private byte[] value;

    public OnDescriptorReadMessage(BluetoothDevice device, BluetoothGattDescriptor descriptor, int status,byte[] value){
        super(device.getAddress());
        this.device=device;
        this.descriptor=descriptor;
        this.status=status;
        this.value = value;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("OnDescriptorReadMessage:mac=%s,status=%d,chac=%s,desc=%s,value=%s",device.getAddress(),status,descriptor.getCharacteristic().getUuid().toString(),descriptor.getUuid().toString(),  Utils.bytesToHexStr(value)));
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onDescriptorRead(device,descriptor,status);
        }
        List<IDescReadCallback> callbacks = getSimpleBle().getCallbackManage().getDescReadCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IDescReadCallback callback:callbacks){
                callback.onDescriptorRead(device,descriptor,status,value);
            }
        }
    }
}
