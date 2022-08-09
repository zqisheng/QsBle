package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGattDescriptor;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleConst;
import com.zqs.ble.core.callback.abs.INotifyStatusChangedCallback;
import com.zqs.ble.core.utils.Utils;

import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class CancelNotifyChainBuilder extends BleChainBuilder<CancelNotifyChainBuilder> {

    private CancelNotifyChain chain = new CancelNotifyChain(mac);

    public CancelNotifyChainBuilder(String mac,UUID serviceUuid,UUID notifyUuid, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid=serviceUuid;
        chain.notifyUuid=notifyUuid;
    }

    public CancelNotifyChainBuilder refresh(){
        chain.isRefresh = true;
        return this;
    }

    public CancelNotifyChainBuilder setNotifyStatusChangedCallback(INotifyStatusChangedCallback callback){
        chain.callback = callback;
        return this;
    }

    @Override
    public BleChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public class CancelNotifyChain extends BleChain<String> {
        private UUID serviceUuid;
        private UUID notifyUuid;
        private INotifyStatusChangedCallback notifyStatusChangedCallback;
        private INotifyStatusChangedCallback callback;
        private boolean isRefresh = false;

        private CancelNotifyChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            BluetoothGattDescriptor desc = getBle().getGattDescriptor(getMac(), serviceUuid, notifyUuid, BleConst.clientCharacteristicConfig);
            if (desc==null){
                onFail(new IllegalStateException(String.format("%s notify desc not found",getMac())));
            }else if (!isRefresh&&Arrays.equals(desc.getValue(), BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){
                onSuccess("disable");
            }else{
                notifyStatusChangedCallback = (device, descriptor, notifyEnable) -> {
                    if (!(Utils.uuidIsSame(descriptor.getCharacteristic().getUuid(), notifyUuid)&&Utils.uuidIsSame(descriptor.getCharacteristic().getService().getUuid(), serviceUuid))){
                        return;
                    }
                    if (callback!=null){
                        callback.onNotifyStatusChanged(device, descriptor, notifyEnable);
                    }
                    if (!notifyEnable){
                        onSuccess("disable");
                    }else{
                        onFail(new IllegalStateException(String.format("%s cancel notify fail",device.getAddress())));
                    }
                };
                getBle().addNotifyStatusCallback(mac,notifyStatusChangedCallback);
                getBle().cancelNotify(mac,serviceUuid,notifyUuid);
            }
        }

        @Override
        public void onDestroy() {
            getBle().rmNotifyStatusCallback(mac,notifyStatusChangedCallback);
        }
    }

}
