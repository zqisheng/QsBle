package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IPhyReadCallback;

import java.util.Queue;

import androidx.annotation.RequiresApi;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ReadPhyChainBuilder extends BleChainBuilder<ReadPhyChainBuilder, ReadPhyChainBuilder.ReadPhyChain,int[]> {

    private ReadPhyChain chain = new ReadPhyChain(mac);
    public ReadPhyChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    public ReadPhyChainBuilder setPhyReadCallback(IPhyReadCallback callback){
        chain.callback = callback;
        return this;
    }

    @Override
    public ReadPhyChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class ReadPhyChain extends BleChain<int[]>{

        private IPhyReadCallback phyReadCallback;
        private IPhyReadCallback callback;

        private ReadPhyChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            phyReadCallback = (device, txPhy, rxPhy, status) -> {
                if (callback != null) {
                    callback.onPhyRead(device, txPhy, rxPhy, status);
                }
                if (status== BluetoothGatt.GATT_SUCCESS){
                    onSuccess(new int[]{txPhy,rxPhy});
                }else{
                    onFail(new IllegalStateException(String.format("%s read phy fail,status=%d", device.getAddress(), status)));
                }
            };
            getBle().addPhyReadCallback(getMac(),phyReadCallback);
            getBle().readPhy(getMac());
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmPhyReadCallback(getMac(),phyReadCallback);
        }
    }

}
