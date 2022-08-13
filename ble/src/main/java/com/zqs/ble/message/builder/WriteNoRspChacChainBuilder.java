package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IChacWriteCallback;
import com.zqs.ble.core.deamon.message.option.WriteChacMessage;
import com.zqs.ble.core.utils.Utils;

import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class WriteNoRspChacChainBuilder extends BleChainBuilder<WriteNoRspChacChainBuilder, WriteNoRspChacChainBuilder.WriteNoRspChacChain,Boolean> {

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
    public WriteNoRspChacChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public class WriteNoRspChacChain extends BleChain<Boolean> {
        private UUID serviceUuid;
        private UUID chacUuid;
        private byte[] value;
        private int retryWriteCount = BleGlobalConfig.rewriteCount;
        private IChacWriteCallback chacWriteCallback;
        private IChacWriteCallback callback;
        private WriteChacMessage message;

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
            chacWriteCallback = (device, characteristic, value, status) -> {
                if (!Utils.uuidIsSame(characteristic, serviceUuid, chacUuid)) {
                    return;
                }
                if (callback!=null){
                    callback.onCharacteristicWrite(device, characteristic, value, status);
                }
                if (status== BluetoothGatt.GATT_SUCCESS){
                    if (Arrays.equals(value,WriteNoRspChacChain.this.value)){
                        onSuccess(true);
                    }
                }
            };
            getBle().addChacWriteCallback(getMac(), chacWriteCallback);
            write(getMac(),serviceUuid,chacUuid,value,retryWriteCount);
        }

        private void write(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount) {
            message = new WriteChacMessage(mac, serviceUuid, chacUuid, value);
            message.setRetryWriteCount(retryWriteCount);
            message.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            sendMessage(message);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            rmMessage(message);
            getBle().rmChacWriteCallback(getMac(), chacWriteCallback);
        }
    }
}
