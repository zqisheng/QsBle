package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;

import com.zqs.ble.core.callback.abs.IConnectionUpdatedCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnConnectionUpdatedMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;
    private int interval;
    private int latency;
    private int timeout;
    private int status;

    public OnConnectionUpdatedMessage(BluetoothDevice device, int interval, int latency, int timeout, int status){
        super(device.getAddress());
        this.device=device;
        this.interval=interval;
        this.latency=latency;
        this.timeout=timeout;
        this.status=status;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BleLog.d(() -> String.format("OnConnectionUpdatedMessage:mac=%s,interval=%d,latency=%d,timeout=%d,status=%d", device.getAddress(), interval, latency, timeout, status));
        List<IConnectionUpdatedCallback> callbacks = getSimpleBle().getCallbackManage().getConnectionUpdatedCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IConnectionUpdatedCallback callback:callbacks){
                callback.onConnectionUpdated(device,interval,latency,timeout,status);
            }
        }
    }
}
