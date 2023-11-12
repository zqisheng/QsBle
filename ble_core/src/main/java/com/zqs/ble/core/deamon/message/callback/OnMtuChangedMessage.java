package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IMtuChangeCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.deamon.message.option.WriteChacLockMessage;
import com.zqs.ble.core.deamon.message.option.WriteChacMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnMtuChangedMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;

    private int mtu;

    private int status;

    public OnMtuChangedMessage(BluetoothDevice device, int mtu, int status) {
        super(device.getAddress());
        this.device = device;
        this.mtu = mtu;
        this.status = status;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("BleCallback OnMtuChangedMessage:mac=%s,status=%d,mtu=%d", device.getAddress(), status, mtu));
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (getSimpleBle().getCurrentMtu(getMac()) != mtu) {
                clearBeforeMessageIf(msg ->
                        (msg instanceof AbsBleMessage) && ((AbsBleMessage) msg).getMac().equals(getMac())
                         && ((msg instanceof WriteChacLockMessage) || msg instanceof WriteChacMessage));
            }
            getSimpleBle().setCurrentMtu(mtu);
            getSimpleBle().setCurrentMtu(getMac(),mtu);
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback != null) {
            globalBleCallback.onMtuChanged(device.getAddress(), mtu, status);
        }
        List<IMtuChangeCallback> callbacks = getSimpleBle().getCallbackManage().getMtuChangeCallbacks(getMac());
        if (callbacks != null && !callbacks.isEmpty()) {
            for (IMtuChangeCallback callback : callbacks) {
                callback.onMtuChanged(device, mtu, status);
            }
        }
    }
}
