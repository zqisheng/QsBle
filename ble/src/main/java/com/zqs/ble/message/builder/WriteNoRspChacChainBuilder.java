package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IChacWriteCallback;

import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class WriteNoRspChacChainBuilder extends BleChainBuilder<WriteNoRspChacChainBuilder> {

    private WriteNoRspChacChain chain = new WriteNoRspChacChain(mac);

    public WriteNoRspChacChainBuilder(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, Queue<BleChainBuilder> chains) {
        super(mac, chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
        chain.value = value;
    }

    public WriteNoRspChacChainBuilder setRetryWriteCount(int retryWriteCount){
        chain.retryWriteCount = retryWriteCount;
        return this;
    }

    public WriteNoRspChacChainBuilder setChacWriteCallback(IChacWriteCallback callback) {
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

    public class WriteNoRspChacChain extends BleChain<Object> {
        private UUID serviceUuid;
        private UUID chacUuid;
        private byte[] value;
        private int retryWriteCount = BleGlobalConfig.rewriteCount;
        private IChacWriteCallback chacWriteCallback;
        private IChacWriteCallback callback;

        private WriteNoRspChacChain(String mac) {
            super(mac);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (getTimeout()==0){
                setTimeout(200*(retryWriteCount==0?1:retryWriteCount));
            }
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            getBle().addChacWriteCallback(getMac(), chacWriteCallback);
            getBle().write(getMac(),serviceUuid,chacUuid,value,retryWriteCount);
        }

        @Override
        public void onDestroy() {
            getBle().rmChacWriteCallback(getMac(), chacWriteCallback);
        }
    }
}
