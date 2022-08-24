package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IReadRssiCallback;

import java.util.Queue;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class ReadRssiChainBuilder extends BleChainBuilder<ReadRssiChainBuilder, ReadRssiChainBuilder.ReadRssiChain,Integer> {

    private ReadRssiChain chain = new ReadRssiChain(mac);
    public ReadRssiChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    public ReadRssiChainBuilder setReadRssiCallback(IReadRssiCallback callback) {
        chain.callback = callback;
        return this;
    }

    @Override
    public ReadRssiChain getBaseChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    protected static class ReadRssiChain extends BleChain<Integer>{

        private IReadRssiCallback readRssiCallback;
        private IReadRssiCallback callback;

        private ReadRssiChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            readRssiCallback = (device, rssi, status) -> {
                if (callback!=null){
                    callback.onReadRssi(device, rssi, status);
                }
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    onSuccess(rssi);
                } else {
                    onFail(new IllegalStateException(String.format("%s read rssi fail,status=%d", device.getAddress(), status)));
                }
            };
            getBle().addReadRssiCallback(getMac(),readRssiCallback);
            setMessageOption(getBle().readRssi(getMac()));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmReadRssiCallback(getMac(),readRssiCallback);
        }
    }
}
