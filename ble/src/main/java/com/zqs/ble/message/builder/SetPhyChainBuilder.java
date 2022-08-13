package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IPhyUpdateCallback;

import java.util.Queue;

import androidx.annotation.RequiresApi;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class SetPhyChainBuilder extends BleChainBuilder<SetPhyChainBuilder, SetPhyChainBuilder.SetPhyChain,int[]> {

    private SetPhyChain chain = new SetPhyChain(mac);

    public SetPhyChainBuilder(String mac, int txPhy, int rxPhy, int phyOptions, Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.txPhy = txPhy;
        chain.rxPhy = rxPhy;
        chain.phyOptions = phyOptions;
    }

    public SetPhyChainBuilder setPhyUpdateCallback(IPhyUpdateCallback callback) {
        chain.callback = callback;
        return this;
    }


    @Override
    public SetPhyChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class SetPhyChain extends BleChain<int[]>{
        private int txPhy;
        private int rxPhy;
        private int phyOptions;
        private IPhyUpdateCallback phyUpdateCallback;
        private IPhyUpdateCallback callback;
        private SetPhyChain(String mac) {
            super(mac);
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            phyUpdateCallback = (device, txPhy, rxPhy, status) -> {
                if (callback!=null){
                    callback.onPhyUpdate(device, txPhy, rxPhy, status);
                }
                if (status==BluetoothGatt.GATT_SUCCESS){
                    onSuccess(new int[]{txPhy, rxPhy});
                }else{
                    onFail(new IllegalStateException(String.format("%s read phy fail,statu=%d", device.getAddress(), status)));
                }
            };
            getBle().addPhyUpdateCallback(getMac(),phyUpdateCallback);
            getBle().setPreferredPhy(getMac(),txPhy,rxPhy,phyOptions);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmPhyUpdateCallback(getMac(), phyUpdateCallback);
        }
    }

}
