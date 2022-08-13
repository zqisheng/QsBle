package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.QsBle;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.deamon.message.option.WriteChacLockMessage;
import com.zqs.ble.core.utils.fun.Function2;

import java.util.Queue;
import java.util.UUID;

import androidx.annotation.NonNull;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class WriteByLockChacChainBuilder extends BleChainBuilder<WriteByLockChacChainBuilder, WriteByLockChacChainBuilder.WriteByLockChacChain,Boolean> {

    private WriteByLockChacChain chain = new WriteByLockChacChain(mac);

    public WriteByLockChacChainBuilder(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, Queue<BleChainBuilder> chains) {
        super(mac, chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
        chain.value = value;
    }

    public WriteByLockChacChainBuilder reWriteCount(int retryWriteCount){
        chain.retryWriteCount = retryWriteCount;
        return this;
    }

    public WriteByLockChacChainBuilder setWriteLockStatusCallback(Function2<Boolean,Integer> callback) {
        chain.callback = callback;
        return this;
    }

    @Override
    public WriteByLockChacChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public class WriteByLockChacChain extends BleChain<Boolean> {
        private UUID serviceUuid;
        private UUID chacUuid;
        private byte[] value;
        private int retryWriteCount = BleGlobalConfig.rewriteCount;
        private Function2<Boolean,Integer> callback;
        private WriteChacLockMessage message;

        private WriteByLockChacChain(String mac) {
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
            writeByLock(getMac(),serviceUuid,chacUuid,value,retryWriteCount,(isSuccess,status)->{
                if (callback!=null){
                    callback.onCallback(isSuccess, status);
                }
                if (isSuccess){
                    onSuccess(true);
                }else{
                    onFail(new IllegalStateException(String.format("%s write chac %s,status=%d", getMac(), chacUuid.toString(), status)));
                }
            });
        }

        private void writeByLock(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount, Function2<Boolean, Integer> writeCallback){
            message = new WriteChacLockMessage(mac, serviceUuid, chacUuid, value);
            message.setRetryWriteCount(retryWriteCount);
            if (writeCallback!=null){
                message.setWriteCallback(writeCallback);
            }
            sendMessage(message);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            rmMessage(message);
        }

        @Override
        public boolean isAlreadHandleTimeoutOption() {
            return true;
        }
    }

}
