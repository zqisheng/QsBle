package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IConnectStatusChangeCallback;

import java.util.Queue;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class DisconnectChainBuilder extends BleChainBuilder<DisconnectChainBuilder, DisconnectChainBuilder.DisconnectChain,Boolean> {

    private DisconnectChain chain = new DisconnectChain(mac);

    public DisconnectChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    public DisconnectChainBuilder setConnectStatusChangeCallback(IConnectStatusChangeCallback callback){
        chain.callback = callback;
        return this;
    }

    @Override
    public DisconnectChain getBaseChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    protected static class DisconnectChain extends BleChain<Boolean>{

        private IConnectStatusChangeCallback connectStatusChangeCallback;
        private IConnectStatusChangeCallback callback;

        private DisconnectChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onSuccess(false);
            }else{
                connectStatusChangeCallback = (device, isConnect, status, profileState) -> {
                    if (callback!=null){
                        callback.onConnectStatusChanged(device, isConnect, status, profileState);
                    }
                    if (!isConnect){
                        onSuccess(false);
                    }else{
                        onFail(new IllegalStateException(String.format("%s disconnect fail",device.getAddress())));
                    }
                };
                getBle().addConnectStatusChangeCallback(getMac(), connectStatusChangeCallback);
                setMessageOption(getBle().disconnect(getMac()));
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmConnectStatusChangeCallback(getMac(),connectStatusChangeCallback);
        }
    }
}
