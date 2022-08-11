package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IDescReadCallback;
import com.zqs.ble.core.utils.Utils;

import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class ReadDescChainBuilder extends BleChainBuilder<ReadDescChainBuilder, ReadDescChainBuilder.ReadDescChain,byte[]> {

    private ReadDescChain chain = new ReadDescChain(mac);
    public ReadDescChainBuilder(String mac, UUID serviceUuid, UUID chacUuid,UUID descUuid, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
        chain.descUuid = descUuid;
    }

    public ReadDescChainBuilder setDescReadCallback(IDescReadCallback callback){
        chain.callback = callback;
        return this;
    }

    @Override
    public ReadDescChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class ReadDescChain extends BleChain<byte[]>{

        private UUID serviceUuid;
        private UUID chacUuid;
        private UUID descUuid;
        private IDescReadCallback descReadCallback;
        private IDescReadCallback callback;
        private ReadDescChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            if (getBle().getGattDescriptor(getMac(), serviceUuid, chacUuid, descUuid)==null) {
                onFail(new IllegalStateException(String.format("%s desc %s no found",getMac(),chacUuid.toString())));
            }else{
                descReadCallback = (device, descriptor, status, value) -> {
                    if (!(Utils.uuidIsSame(descriptor, serviceUuid, chacUuid, descUuid))) {
                        return;
                    }
                    if (callback!=null){
                        callback.onDescriptorRead(device, descriptor, status, value);
                    }
                    if (status== BluetoothGatt.GATT_SUCCESS){
                        onSuccess(value);
                    }else{
                        onFail(new IllegalStateException(String.format("read %s desc %s fail,status=%d",device.getAddress(),descriptor.getUuid().toString(),status)));
                    }
                };
                getBle().addDescReadCallback(getMac(),descReadCallback);
                getBle().readDesc(getMac(),serviceUuid,chacUuid,descUuid);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmDescReadCallback(getMac(),descReadCallback);
        }
    }

}
