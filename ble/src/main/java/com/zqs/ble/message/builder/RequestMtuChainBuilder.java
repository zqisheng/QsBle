package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IMtuChangeCallback;

import java.util.Queue;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class RequestMtuChainBuilder extends BleChainBuilder<RequestMtuChainBuilder, RequestMtuChainBuilder.RequestMtuChain,Integer> {

    private RequestMtuChain chain = new RequestMtuChain(mac);
    public RequestMtuChainBuilder(String mac,int mtu, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.mtu = mtu;
    }

    public RequestMtuChainBuilder refresh(){
        chain.isRefresh = true;
        return this;
    }

    public RequestMtuChainBuilder setMtuChangeCallback(IMtuChangeCallback callback) {
        chain.callback = callback;
        return this;
    }

    @Override
    public RequestMtuChain getBaseChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    protected static class RequestMtuChain extends BleChain<Integer>{
        private int mtu;
        private IMtuChangeCallback mtuChangeCallback;
        private IMtuChangeCallback callback;
        private boolean isRefresh = false;

        private RequestMtuChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            if (!isRefresh&&getBle().getCurrentMtu()==mtu){
                onSuccess(mtu);
            }else{
                mtuChangeCallback = (device, mtu, status) -> {
                    if (callback!=null){
                        callback.onMtuChanged(device,mtu,status);
                    }
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        onSuccess(mtu);
                    } else {
                        onFail(new IllegalStateException(String.format("%s request mtu fail,status=%d", device.getAddress(), status)));
                    }
                };
                getBle().addMtuChangeCallback(getMac(),mtuChangeCallback);
                setMessageOption(getBle().setMtu(getMac(), mtu));
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmMtuChangeCallback(getMac(),mtuChangeCallback);
        }
    }
}
