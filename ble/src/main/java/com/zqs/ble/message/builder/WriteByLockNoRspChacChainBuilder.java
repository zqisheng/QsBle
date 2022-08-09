package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.utils.fun.Function2;

import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class WriteByLockNoRspChacChainBuilder extends BleChainBuilder<WriteByLockNoRspChacChainBuilder> {

    private WriteByLockNoRspChacChain chain = new WriteByLockNoRspChacChain(mac);

    public WriteByLockNoRspChacChainBuilder(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
        chain.value = value;
    }

    public WriteByLockNoRspChacChainBuilder reWriteCount(int retryWriteCount){
        chain.retryWriteCount = retryWriteCount;
        return this;
    }

    public WriteByLockNoRspChacChainBuilder setRetryWriteCount(int retryWriteCount){
        chain.retryWriteCount = retryWriteCount;
        return this;
    }

    public WriteByLockNoRspChacChainBuilder setWriteLockStatusCallback(Function2<Boolean,Integer> callback) {
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

    public class WriteByLockNoRspChacChain extends BleChain<Object> {
        private UUID serviceUuid;
        private UUID chacUuid;
        private byte[] value;
        private int retryWriteCount = BleGlobalConfig.rewriteCount;
        private Function2<Boolean,Integer> callback;

        private WriteByLockNoRspChacChain(String mac) {
            super(mac);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (getTimeout()==0){
                setTimeout((value.length / 20) * (retryWriteCount==0?1:retryWriteCount) * 200);
            }
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            getBle().writeByLockNoRsp(getMac(),serviceUuid,chacUuid,value,retryWriteCount,(isSuccess,status)->{
                if (callback!=null){
                    callback.onCallback(isSuccess, status);
                }
                if (isSuccess){
                    onSuccess(null);
                }else{
                    onFail(new IllegalStateException(String.format("%s write chac %s,status=%d", getMac(), chacUuid.toString(), status)));
                }
            });
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public boolean isAlreadHandleTimeoutOption() {
            return true;
        }
    }
}
