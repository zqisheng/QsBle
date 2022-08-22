package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IChacReadCallback;
import com.zqs.ble.core.utils.Utils;

import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class ReadChacChainBuilder extends BleChainBuilder<ReadChacChainBuilder, ReadChacChainBuilder.ReadChacChain,byte[]> {

    private ReadChacChain chain = new ReadChacChain(mac);
    public ReadChacChainBuilder(String mac,UUID serviceUuid,UUID chacUuid, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.serviceUuid = serviceUuid;
        chain.chacUuid = chacUuid;
    }

    public ReadChacChainBuilder setChacReadCallback(IChacReadCallback callback){
        chain.callback = callback;
        return this;
    }

    @Override
    public ReadChacChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class ReadChacChain extends BleChain<byte[]>{

        private UUID serviceUuid;
        private UUID chacUuid;
        private IChacReadCallback chacReadCallback;
        private IChacReadCallback callback;
        private ReadChacChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            BluetoothGattCharacteristic chac = getBle().getGattCharacteristic(getMac(), serviceUuid, chacUuid);
            if (chac==null){
                onFail(new IllegalStateException(String.format("%s chac %s not found",getMac(),chacUuid.toString())));
            }else{
                chacReadCallback = (device, characteristic, status, value) -> {
                    if (!(Utils.uuidIsSame(characteristic,serviceUuid,chacUuid))){
                        return;
                    }
                    if (callback!=null){
                        callback.onCharacteristicRead(device, characteristic, status, value);
                    }
                    if (status== BluetoothGatt.GATT_SUCCESS){
                        onSuccess(value);
                    }else{
                        onFail(new IllegalStateException(String.format("read %s chac %s fail,status=%d",device.getAddress(),chacUuid.toString(),status)));
                    }
                };
                getBle().addChacReadCallback(getMac(),chacReadCallback);
                setMessageOption(getBle().read(getMac(), serviceUuid, chacUuid));
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmChacReadCallback(getMac(),chacReadCallback);
        }
    }

}
