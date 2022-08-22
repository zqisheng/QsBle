package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IReadRssiCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnReadRemoteRssiMessage extends AbsBleMessage implements ICallbackMessage {

    private int rssi;
    private int status;
    private BluetoothDevice device;

    public OnReadRemoteRssiMessage(BluetoothDevice device,int rssi, int status){
        super(device.getAddress());
        this.rssi=rssi;
        this.status=status;
        this.device = device;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("OnReadRemoteRssiMessage:mac=%s,status=%d,rssi=%d", device.getAddress(), status, rssi));
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onReadRemoteRssi(device.getAddress(),rssi,status);
        }
        List<IReadRssiCallback> callbacks = getSimpleBle().getCallbackManage().getReadRssiCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IReadRssiCallback callback:callbacks){
                callback.onReadRssi(device,rssi,status);
            }
        }
    }
}
