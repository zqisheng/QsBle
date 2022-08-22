package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.zqs.ble.core.BleConst;
import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IDescWriteCallback;
import com.zqs.ble.core.callback.abs.INotifyFailCallback;
import com.zqs.ble.core.callback.abs.INotifyStatusChangedCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.Arrays;
import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnDescriptorWriteMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;

    private BluetoothGattDescriptor descriptor;

    private int status;
    private byte[] value;

    public OnDescriptorWriteMessage(BluetoothDevice device, BluetoothGattDescriptor descriptor,byte[] value, int status){
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
            BleLog.d(String.format("OnDescriptorWriteMessage:mac=%s,status=%d,chac=%s,desc=%s,value=%s",device.getAddress(),status,descriptor.getCharacteristic().getUuid().toString(),descriptor.getUuid().toString(),  Utils.bytesToHexStr(value)));
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onDescriptorWrite(device.getAddress(),descriptor,value,status);
        }
        List<IDescWriteCallback> callbacks = getSimpleBle().getCallbackManage().getDescWriteCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IDescWriteCallback callback:callbacks){
                callback.onDescriptorWrite(device,descriptor,value,status);
            }
        }
        if (Utils.uuidIsSame(BleConst.clientCharacteristicConfig,descriptor.getUuid())){
            if (status== BluetoothGatt.GATT_SUCCESS){
                List<INotifyStatusChangedCallback> notifyStatusCallbacks = getSimpleBle().getCallbackManage().getNotifyStatusChangedCallbacks(getMac());
                Boolean notifyEnable = null;
                if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        || Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)){
                    notifyEnable = true;
                }else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){
                    notifyEnable = false;
                }
                if (notifyEnable!=null&&notifyStatusCallbacks!=null&&!notifyStatusCallbacks.isEmpty()){
                    for (INotifyStatusChangedCallback callback:notifyStatusCallbacks){
                        callback.onNotifyStatusChanged(device,descriptor,notifyEnable);
                    }
                }
            }else{
                List<INotifyFailCallback> notifyFailStatusCallbacks = getSimpleBle().getCallbackManage().getNotifyFailCallbacks(getMac());
                for (INotifyFailCallback callback : notifyFailStatusCallbacks) {
                    callback.onNotifyFail(descriptor.getCharacteristic(), status);
                }
            }
        }
    }
}
