package com.zqs.ble.message.builder;

import com.zqs.ble.BaseChain;
import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.ChainMessage;
import com.zqs.ble.QsBle;

import java.util.Queue;

import androidx.annotation.Nullable;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class TogetherChainBuilder extends BleChainBuilder<TogetherChainBuilder, TogetherChainBuilder.TogetherChain,Boolean> {

    private TogetherChain chain = new TogetherChain(mac);

    public TogetherChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    @Override
    public BaseChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class TogetherChain extends BleChain<Boolean> {
        private Queue<BaseChain> chains;
        private ChainMessage message;

        public TogetherChain(String mac) {
            super(mac);
        }

        public void setChains(Queue<BaseChain> chains){
            this.chains = chains;
        }

        @Override
        public void handle() {
            message = new ChainMessage(chains);
            message.setHandleStatusCallback(new ChainMessage.ChainHandleStatusCallback() {
                @Override
                public void onReport(Boolean isSuccess, @Nullable Exception e) {
                    if (isSuccess) {
                        onSuccess(true);
                    } else {
                        onFail(e);
                    }
                }
            });
            sendMessage(message);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            message.cancel();
        }
    }


}
