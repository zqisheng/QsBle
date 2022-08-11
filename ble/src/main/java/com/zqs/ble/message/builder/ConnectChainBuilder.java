package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IConnectStatusChangeCallback;
import com.zqs.ble.core.callback.abs.IServicesDiscoveredCallback;
import com.zqs.ble.core.utils.fun.Function3;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class ConnectChainBuilder extends BleChainBuilder<ConnectChainBuilder, ConnectChainBuilder.ConnectChain,Boolean> {

    private ConnectChain chain = new ConnectChain(mac);

    public ConnectChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        super(mac,chains);
    }

    public ConnectChainBuilder connectTimeout(long connectTimeout){
        chain.connectTimeout = connectTimeout;
        return this;
    }

    public ConnectChainBuilder mustDiscoverService(UUID ... serviceUuids){
        if (chain.uuids==null){
            chain.uuids = new HashMap<>();
        }else{
            chain.uuids.clear();
        }
        for (UUID service : serviceUuids) {
            chain.uuids.put(service.toString(), false);
        }
        return this;
    }

    public ConnectChainBuilder reConnectCount(int reconnectCount){
        chain.reconnectCount = reconnectCount;
        return this;
    }

    public ConnectChainBuilder setConnectFailCallback(Function3<Boolean /*isTimeout*/, Integer /*status*/, Integer/*profileState*/> connectFailCallback){
        chain.connectFailCallback = connectFailCallback;
        return this;
    }

    public ConnectChainBuilder setConnectStatusChangeCallback(IConnectStatusChangeCallback callback){
        chain.callback1 = callback;
        return this;
    }

    public ConnectChainBuilder setServicesDiscoveredCallback(IServicesDiscoveredCallback callback) {
        chain.callback2 = callback;
        return this;
    }

    @Override
    public ConnectChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class ConnectChain extends BleChain<Boolean>{
        private long connectTimeout = BleGlobalConfig.connectTimeout;
        private int reconnectCount=0;
        private Map<String, Boolean> uuids;
        private IConnectStatusChangeCallback connectStatusChangeCallback;
        private IServicesDiscoveredCallback servicesDiscoveredCallback;
        private IConnectStatusChangeCallback callback1;
        private IServicesDiscoveredCallback callback2;
        private Function3<Boolean /*isTimeout*/, Integer /*status*/, Integer/*profileState*/> connectFailCallback;
        private boolean isRefresh = false;

        private ConnectChain(String mac) {
            super(mac);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (getTimeout()==0){
                setTimeout((reconnectCount == 0 ? 1 : reconnectCount) * (connectTimeout + 3000));
            }
        }

        @Override
        public void handle() {
            if (getBle().isConnect(getMac())){
                onSuccess(true);
            }else{
                connectStatusChangeCallback = (device, isConnect, status, profileState) -> {
                    if (callback1!=null){
                        callback1.onConnectStatusChanged(device, isConnect, status, profileState);
                    }
                    if (!isConnect){
                        onFail(new IllegalStateException("device connect fail"));
                    }
                };
                getBle().addConnectStatusChangeCallback(getMac(), connectStatusChangeCallback);
                servicesDiscoveredCallback = (device, services, status) -> {
                    if (status== BluetoothGatt.GATT_SUCCESS){
                        if (uuids!=null&&!uuids.isEmpty()){
                            for (BluetoothGattService service:services){
                                if (uuids.get(service.getUuid().toString())!=null){
                                    uuids.put(service.getUuid().toString(), true);
                                }
                            }
                            boolean isFind = true;
                            for (String key : uuids.keySet()) {
                                if (!uuids.get(key)){
                                    isFind=false;
                                    break;
                                }
                            }
                            if (isFind){
                                onSuccess(true);
                            }
                        }else{
                            onSuccess(true);
                        }
                    }else{
                        onFail(new IllegalStateException());
                    }
                };
                getBle().addServicesDiscoveredCallback(getMac(),servicesDiscoveredCallback);
                if (callback2!=null){
                    getBle().addServicesDiscoveredCallback(getMac(), callback2);
                }
                getBle().connect(getMac(),connectTimeout,reconnectCount,connectFailCallback);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (callback2 != null) {
                getBle().rmServicesDiscoveredCallback(getMac(), callback2);
            }
            getBle().rmConnectStatusChangeCallback(getMac(),connectStatusChangeCallback);
        }
    }

}
