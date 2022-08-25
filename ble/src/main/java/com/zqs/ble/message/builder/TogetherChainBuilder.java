package com.zqs.ble.message.builder;

import com.zqs.ble.BaseChain;
import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.ChainMessage;
import com.zqs.ble.QsBle;
import com.zqs.ble.core.utils.BleLog;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import androidx.annotation.Nullable;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class TogetherChainBuilder extends BleChainBuilder<TogetherChainBuilder, TogetherChainBuilder.TogetherChain,Boolean> {

    private TogetherChain chain = new TogetherChain(mac);

    public TogetherChainBuilder(String mac, Queue<BleChainBuilder> chains,BleChainBuilder builder) {
        super(mac,chains);
        LinkedList<BaseChain> chainQueue = new LinkedList<>();
        Queue<BleChainBuilder> builderQueue=builder.getChains();
        while (!builderQueue.isEmpty()) {
            chainQueue.add(builderQueue.poll().build());
        }
        chain.chains = chainQueue;
        //timeout default 60s
        timeout(60000);
    }

    @Override
    public BaseChain getBaseChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    protected static class TogetherChain extends BleChain<Boolean> {
        private ChainMessage message;
        private LinkedList<BaseChain> chains;
        public TogetherChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            LinkedList<BaseChain> tempBaseChain = new LinkedList<>(chains);
            message = new ChainMessage(tempBaseChain);
            message.setHandleStatusCallback((isSuccess, e) -> {
                if (isSuccess){
                    onSuccess(true);
                }else{
                    onFail(e);
                }
            });
            sendMessage(message);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (message!=null){
                message.cancel();
                message = null;
            }
        }
    }


}
