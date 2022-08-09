package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IServicesDiscoveredCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnServicesDiscoveredMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;
    private List<BluetoothGattService> services;
    private int status;

    public OnServicesDiscoveredMessage(BluetoothDevice device, List<BluetoothGattService> services, int status){
        super(device.getAddress());
        this.device=device;
        this.services = services;
        this.status=status;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BleLog.d(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (BluetoothGattService service:services){
                sb.append(service.getUuid().toString() + ",");
            }
            sb.append("]");
            return String.format("OnServicesDiscoveredMessage:mac=%s,status=%d,services=%s", device.getAddress(), status, sb.toString());
        });
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onServicesDiscovered(device,status);
        }
        List<IServicesDiscoveredCallback> callbacks = getSimpleBle().getCallbackManage().getServicesDiscoveredCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IServicesDiscoveredCallback callback:callbacks){
                callback.onServicesDiscovered(device,services,status);
            }
        }
    }
}
