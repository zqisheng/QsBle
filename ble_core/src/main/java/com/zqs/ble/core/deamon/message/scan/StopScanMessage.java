package com.zqs.ble.core.deamon.message.scan;

import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IScanStatusCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.deamon.message.order.IFrontMessage;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class StopScanMessage extends AbsBleMessage implements IFrontMessage, IBleScanMessage {

    public StopScanMessage() {
        super("");
    }

    @Override
    public void verifyMessage() {
        //空实现
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        getSimpleBle().getBleScanOption().stopScan();
        getSimpleBle().getBleScanOption().onScanStop();
        getSimpleBle().setScanState(false);
        GlobalBleCallback globalBleGattCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleGattCallback!=null){
            globalBleGattCallback.onScanStatusChanged(false);
        }
        List<IScanStatusCallback> callbacks = getSimpleBle().getCallbackManage().getScanStatusCallbacks();
        for (IScanStatusCallback callback : callbacks) {
            callback.onScanStatusChanged(false);
        }
    }
}
