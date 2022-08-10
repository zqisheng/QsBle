package com.zqs.ble.core.deamon.message.callback;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IBlueStatusCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnBlueStatusChangedMessage extends AbsBleMessage implements ICallbackMessage {

    private boolean isOpen;

    public OnBlueStatusChangedMessage(boolean isOpen){
        super("");
        this.isOpen = isOpen;
    }

    @Override
    public void verifyMessage() {

    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("OnBlueStatusChangedMessage:isOpen=%s", isOpen));
        }
        if (!isOpen){
            getSimpleBle().clearMessageIf((msg)->true, null);
            getSimpleBle().clearConnectStatus();
            getSimpleBle().setScanState(false);
        }
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onBluetoothStatusChanged(isOpen);
        }
        List<IBlueStatusCallback> callbacks = getSimpleBle().getCallbackManage().getBlueStatusCallbacks();
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IBlueStatusCallback callback:callbacks){
                callback.onBluetoothStatusChanged(isOpen);
            }
        }
    }
}
