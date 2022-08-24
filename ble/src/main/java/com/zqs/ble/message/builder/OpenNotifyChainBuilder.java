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
public final class OpenNotifyChainBuilder extends BleChainBuilder<OpenNotifyChainBuilder, OpenNotifyChainBuilder.OpenNotifyChain,String> {

    private OpenNotifyChain chain = new OpenNotifyChain(mac);

    public OpenNotifyChainBuilder(String mac, UUID serviceUuid, UUID notifyUuid, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid = serviceUuid;
        chain.notifyUuid = notifyUuid;
    }

    public OpenNotifyChainBuilder retry(int retry){
        getBaseChain().setRetry(retry);
        return this;
    }

    public OpenNotifyChainBuilder delay(long delay) {
        getBaseChain().setDelay(delay);
        return this;
    }

    public OpenNotifyChainBuilder timeout(long timeout) {
        getBaseChain().setTimeout(timeout);
        return this;
    }

    public OpenNotifyChainBuilder refresh(){
        chain.isRefresh = true;
        return this;
    }

    public OpenNotifyChainBuilder setNotifyStatusChangedCallback(INotifyStatusChangedCallback callback){
        chain.callback = callback;
        return this;
    }

    @Override
    public OpenNotifyChain getBaseChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    protected static class OpenNotifyChain extends BleChain<String>{

        private UUID serviceUuid;
        private UUID notifyUuid;
        private INotifyStatusChangedCallback notifyStatusChangedCallback;
        private INotifyStatusChangedCallback callback;
        private boolean isRefresh = false;

        private OpenNotifyChain(String mac) {
            super(mac);
        }

        @Override
        public void onCreate() {
            super.onCreate();
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
            }else if (!isRefresh&&Arrays.equals(desc.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    || Arrays.equals(desc.getValue(), BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)){
                onSuccess(Arrays.equals(desc.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ? "notification" : "indication");
            }else{
                notifyStatusChangedCallback = (device, descriptor, notifyEnable) -> {
                    if (!(Utils.uuidIsSame(descriptor.getCharacteristic().getUuid(), notifyUuid)&&Utils.uuidIsSame(descriptor.getCharacteristic().getService().getUuid(), serviceUuid))){
                        return;
                    }
                    if (callback!=null){
                        callback.onNotifyStatusChanged(device, descriptor, notifyEnable);
                    }
                    if (notifyEnable){
                        onSuccess(Arrays.equals(desc.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ? "notification" : "indication");
                    }else{
                        onFail(new IllegalStateException(String.format("%s open notify fail",device.getAddress())));
                    }
                };
                getBle().addNotifyStatusCallback(getMac(), notifyStatusChangedCallback);
                setMessageOption(getBle().openNotify(getMac(), serviceUuid, notifyUuid));
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmNotifyStatusCallback(getMac(), notifyStatusChangedCallback);
        }
    }
}
