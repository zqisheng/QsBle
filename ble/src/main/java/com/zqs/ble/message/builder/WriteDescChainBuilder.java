package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IDescWriteCallback;
import com.zqs.ble.core.utils.Utils;

import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class WriteDescChainBuilder extends BleChainBuilder<WriteDescChainBuilder> {

    private WriteDescChain chain = new WriteDescChain(mac);
    public WriteDescChainBuilder(String mac, UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
        chain.descUuid = descUuid;
        chain.value = value;
    }

    public WriteDescChainBuilder setDescWriteCallback(IDescWriteCallback callback) {
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

    public static class WriteDescChain extends BleChain<Object>{

        private UUID serviceUuid;
        private UUID chacUuid;
        private UUID descUuid;
        private byte[] value;
        private IDescWriteCallback descWriteCallback;
        private IDescWriteCallback callback;
        private WriteDescChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            descWriteCallback = (device, descriptor, value,status) -> {
                if (!(Utils.uuidIsSame(descriptor, serviceUuid, chacUuid, descUuid))) {
                    return;
                }
                if (callback!=null){
                    callback.onDescriptorWrite(device, descriptor, value, status);
                }
                if (status== BluetoothGatt.GATT_SUCCESS){
                    onSuccess(null);
                }else{
                    onFail(new IllegalStateException(String.format("%s write desc %s fail,status=%d", device.getAddress(), descriptor.getUuid(), status)));
                }
            };
            getBle().addDescWriteCallback(getMac(),descWriteCallback);
            getBle().writeDesc(getMac(), serviceUuid, chacUuid, descUuid, value);
        }

        @Override
        public void onDestroy() {
            getBle().rmDescWriteCallback(getMac(),descWriteCallback);
        }
    }

}
