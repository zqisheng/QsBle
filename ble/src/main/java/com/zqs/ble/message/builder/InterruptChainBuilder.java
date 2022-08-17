package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.QsBle;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.fun.Function;

import java.util.Queue;

import androidx.annotation.Nullable;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class InterruptChainBuilder<D> extends BleChainBuilder<InterruptChainBuilder, InterruptChainBuilder.InterruptChain,D> {

    private InterruptChain<D> chain = new InterruptChain<D>(mac);

    public InterruptChainBuilder(String mac, Queue<BleChainBuilder> chains, Function<InterruptOption,Runnable> interrupt) {
        super(mac,chains);
        chain.interrupt = interrupt;
    }

    @Override
    public InterruptChainBuilder async() {
        throw new IllegalStateException("InterruptChainBuilder no support to use async");
    }

    @Override
    public InterruptChainBuilder dump(boolean dump) {
        throw new IllegalStateException("InterruptChainBuilder no support to use dump");
    }

    @Override
    public InterruptChainBuilder retry(int retry) {
        throw new IllegalStateException("InterruptChainBuilder no support to use retry");
    }

    @Override
    public InterruptChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class InterruptChain<D> extends BleChain<D>{

        public InterruptChain(String mac) {
            super(mac);
        }

        private Function<InterruptOption,Runnable> interrupt;
        private Runnable destroyCallback;

        //闭包
        private InterruptOption<D> option = new InterruptOption<D>() {
            @Override
            public void next(D data) {
                sendMessage(new AbsMessage() {
                    @Override
                    public void onHandlerMessage() {
                        InterruptChain.this.onSuccess(data);
                    }
                });
            }

            @Override
            public void cancel() {
                sendMessage(new AbsMessage() {
                    @Override
                    public void onHandlerMessage() {
                    InterruptChain.this.onFail(new IllegalStateException());
                    }
                });
            }
        };

        @Override
        public void handle() {
            destroyCallback = interrupt.apply(option);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (destroyCallback!=null){
                destroyCallback.run();
            }
        }
    }


    public interface InterruptOption<D> {
        void next(@Nullable D data);

        void cancel();
    }

}
