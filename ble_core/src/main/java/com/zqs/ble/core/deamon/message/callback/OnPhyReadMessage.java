package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;

import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IPhyReadCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description 暂时不实现
 */
public class OnPhyReadMessage extends AbsBleMessage implements ICallbackMessage {
    private BluetoothDevice device;
    private int txPhy;
    private int rxPhy;
    private int status;
    public OnPhyReadMessage(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        super(device.getAddress());
        this.device = device;
        this.txPhy=txPhy;
        this.rxPhy=rxPhy;
        this.status=status;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        BleLog.d(() -> String.format("OnPhyReadMessage:mac=%s,txPhy=%d,rxPhy=%d,status=%d", device.getAddress(), txPhy, rxPhy, status));
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onPhyRead(device,txPhy,rxPhy,status);
        }
        List<IPhyReadCallback> callbacks = getSimpleBle().getCallbackManage().getPhyReadCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IPhyReadCallback callback:callbacks){
                callback.onPhyRead(device,txPhy,rxPhy,status);
            }
        }
    }
}
