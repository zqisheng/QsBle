package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IScanStatusCallback;

import java.util.Queue;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class StopScanChainBuilder extends BleChainBuilder<StopScanChainBuilder, StopScanChainBuilder.StopScanChain,Boolean> {

    private StopScanChain chain = new StopScanChain("");

    public StopScanChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    public StopScanChainBuilder setScanStatusCallback(IScanStatusCallback callback) {
        chain.callback = callback;
        return this;
    }

    @Override
    protected void verifyMac(String mac) {
        //null impl
    }

    @Override
    public StopScanChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class StopScanChain extends BleChain<Boolean> {

        private IScanStatusCallback scanStatusCallback;
        private IScanStatusCallback callback;

        private StopScanChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isScaning()){
                onSuccess(false);
            }else{
                scanStatusCallback = isScanning -> {
                    if (callback!=null){
                        callback.onScanStatusChanged(isScanning);
                    }
                    if (!isScanning){
                        onSuccess(false);
                    }else{
                        onFail(new IllegalStateException());
                    }
                };
                getBle().addScanStatusCallback(scanStatusCallback);
                getBle().stopScan();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmScanStatusCallback(scanStatusCallback);
        }
    }

}
