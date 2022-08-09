package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IChacWriteCallback;
import com.zqs.ble.core.utils.Utils;

import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class WriteChacChainBuilder extends BleChainBuilder<WriteChacChainBuilder> {

    private WriteChacChain chain = new WriteChacChain(mac);
    public WriteChacChainBuilder(String mac, UUID serviceUuid, UUID chacUuid,byte[] value, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
        chain.value = value;
    }

    public WriteChacChainBuilder setRetryWriteCount(int retryWriteCount){
        chain.retryWriteCount = retryWriteCount;
        return this;
    }

    public WriteChacChainBuilder setChacWriteCallback(IChacWriteCallback callback) {
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

    public class WriteChacChain extends BleChain<Object> {
        private UUID serviceUuid;
        private UUID chacUuid;
        private byte[] value;
        private int retryWriteCount = BleGlobalConfig.rewriteCount;
        private IChacWriteCallback chacWriteCallback;
        private IChacWriteCallback callback;

        private WriteChacChain(String mac) {
            super(mac);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (getTimeout()==0){
                setTimeout(200*(retryWriteCount==0?1:retryWriteCount));
            }
            value = Utils.expandBytes(value, (byte) 0, getBle().getCurrentMtu());
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            chacWriteCallback = (device, characteristic, value, status) -> {
                if (!Utils.uuidIsSame(characteristic, serviceUuid, chacUuid)) {
                    return;
                }
                if (callback!=null){
                    callback.onCharacteristicWrite(device, characteristic, value, status);
                }
                if (status== BluetoothGatt.GATT_SUCCESS){
                    if (Arrays.equals(value,WriteChacChain.this.value)){
                        onSuccess(null);
                    }
                }else{
                    onFail(new IllegalStateException(String.format("%s write chac %s,status=%d", getMac(), chacUuid.toString(), status)));
                }
            };
            getBle().addChacWriteCallback(getMac(), chacWriteCallback);
            getBle().write(getMac(),serviceUuid,chacUuid,value,retryWriteCount);
        }

        @Override
        public void onDestroy() {
            getBle().rmChacWriteCallback(getMac(), chacWriteCallback);
        }
    }

}
