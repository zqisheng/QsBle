package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.api.IMultiPackageAssembly;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IBleMultiPkgsCallback;
import com.zqs.ble.core.callback.abs.IChacChangeCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnCharacteristicChangedMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;
    private BluetoothGattCharacteristic characteristic;
    private byte[] value;
    public OnCharacteristicChangedMessage(BluetoothDevice device, BluetoothGattCharacteristic characteristic,byte[] value){
        super(device.getAddress());
        this.device=device;
        this.characteristic=characteristic;
        this.value = value;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("OnCharacteristicChangedMessage:mac=%s,chac=%s,value=%s", device.getAddress(), characteristic.getUuid().toString(), Utils.bytesToHexStr(value)));
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onCharacteristicChanged(device.getAddress(), characteristic);
        }
        IMultiPackageAssembly parser = getSimpleBle().getMultiPackageAssembly(getMac());
        if (parser != null) {
            parser.onChanged(characteristic, value);
            if (!parser.hasNext(value)){
                List<byte[]> result = parser.getResult();
                List<IBleMultiPkgsCallback> multiPkgCallbacks = getSimpleBle().getCallbackManage().getBleMultiPkgsCallbacks(getMac(), characteristic.getUuid());
                if (multiPkgCallbacks!=null&&!multiPkgCallbacks.isEmpty()){
                    for (IBleMultiPkgsCallback callback:multiPkgCallbacks){
                        callback.onResult(device,characteristic,result);
                    }
                }
            }
        }
        List<IChacChangeCallback> callbacks = getSimpleBle().getCallbackManage().getChacChangeCallbacks(getMac());
        if (callbacks!=null){
            for (IChacChangeCallback callback:callbacks){
                callback.onCharacteristicChanged(device,characteristic,value);
            }
        }
    }

}
