package com.zqs.ble;

import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.fun.Function;

import java.util.Queue;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class InterruptChainBuilder extends BleChainBuilder<InterruptChainBuilder, InterruptChainBuilder.InterruptChain,Boolean> {

    private InterruptChain chain = new InterruptChain(mac);

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

    public static class InterruptChain extends BleChain<Boolean>{

        public InterruptChain(String mac) {
            super(mac);
        }

        private Function<InterruptOption,Runnable> interrupt;
        private Runnable destroyCallback;
        //闭包
        private InterruptOption option = new InterruptOption() {
            @Override
            public void next() {
                QsBle.getInstance().sendMessage(new AbsMessage() {
                    @Override
                    public void onHandlerMessage() {
                        InterruptChain.this.onSuccess(true);
                    }
                });
            }

            @Override
            public void cancel() {
                QsBle.getInstance().sendMessage(new AbsMessage() {
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


    public interface InterruptOption {
        void next();

        void cancel();
    }

}
