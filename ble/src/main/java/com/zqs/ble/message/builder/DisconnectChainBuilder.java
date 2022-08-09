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
public class DisconnectChainBuilder extends BleChainBuilder<DisconnectChainBuilder> {

    private DisconnectChain chain = new DisconnectChain(mac);

    public DisconnectChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    public DisconnectChainBuilder setConnectStatusChangeCallback(IConnectStatusChangeCallback callback){
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

    public static class DisconnectChain extends BleChain<Object>{

        private IConnectStatusChangeCallback connectStatusChangeCallback;
        private IConnectStatusChangeCallback callback;

        private DisconnectChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onSuccess(null);
            }else{
                connectStatusChangeCallback = (device, isConnect, status, profileState) -> {
                    if (callback!=null){
                        callback.onConnectStatusChanged(device, isConnect, status, profileState);
                    }
                    if (!isConnect){
                        onSuccess(null);
                    }else{
                        onFail(new IllegalStateException(String.format("%s disconnect fail",device.getAddress())));
                    }
                };
                getBle().addConnectStatusChangeCallback(getMac(), connectStatusChangeCallback);
                getBle().disconnect(getMac());
            }
        }

        @Override
        public void onDestroy() {
            getBle().rmConnectStatusChangeCallback(getMac(),connectStatusChangeCallback);
        }
    }
}
