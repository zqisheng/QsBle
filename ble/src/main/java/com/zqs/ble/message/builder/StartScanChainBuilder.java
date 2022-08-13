package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.abs.IScanErrorCallback;
import com.zqs.ble.core.callback.abs.IScanStatusCallback;
import com.zqs.ble.core.callback.scan.SimpleScanConfig;
import com.zqs.ble.message.pojo.Entry;

import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public final class StartScanChainBuilder extends BleChainBuilder<StartScanChainBuilder, StartScanChainBuilder.StartScanChain,Entry<Integer, byte[]>> {

    private StartScanChain chain = new StartScanChain();

    public StartScanChainBuilder(String mac,Queue<BleChainBuilder> chains) {
        super(mac,chains);
        chain.setMac(mac);
    }

    public StartScanChainBuilder scanTime(long scanTime) {
        chain.scanTime = scanTime;
        return this;
    }

    public StartScanChainBuilder setScanCallback(IScanCallback callback) {
        chain.callback1 = callback;
        return this;
    }

    public StartScanChainBuilder setScanStatusCallback(IScanStatusCallback callback) {
        chain.callback2 = callback;
        return this;
    }


    public StartScanChainBuilder setScanErrorCallback(IScanErrorCallback callback) {
        chain.callback3 = callback;
        return this;
    }

    public StartScanChainBuilder filterServiceUuids(UUID serviceUuids){
        chain.filterServiceUuid = serviceUuids;
        return this;
    }

    public StartScanChainBuilder noRepeatCallback(){
        chain.isRepeatCallback = false;
        return this;
    }

    public StartScanChainBuilder filterName(String name){
        chain.filterName = name;
        return this;
    }

    @Override
    public StartScanChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        if (chain.getTimeout()!=0&&chain.scanTime < chain.getTimeout() + 500) {
            throw new IllegalArgumentException("scanTime不能小于(timeout+500)");
        }
        return chain;
    }

    public static class StartScanChain extends BleChain<Entry<Integer,byte[]>> {

        private long scanTime = BleGlobalConfig.scanTime;

        private IScanCallback scanCallback;
        private IScanStatusCallback scanStatusCallback;
        private IScanErrorCallback scanErrorCallback;

        private IScanCallback callback1;
        private IScanStatusCallback callback2;
        private IScanErrorCallback callback3;
        private boolean isRecordDevice = false;

        private Boolean isRepeatCallback;
        private String filterName;
        private UUID filterServiceUuid;

        public boolean isRecordDevice() {
            return isRecordDevice;
        }

        public void setRecordDevice(boolean recordDevice) {
            isRecordDevice = recordDevice;
        }

        public StartScanChain() {
            super("");
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (getTimeout()==0){
                setTimeout(scanTime);
            }
        }

        @Override
        public void handle() {
            if (getBle().isScaning()){
                getBle().stopScan();
            }
            scanCallback = (device, rssi, scanRecord) -> {
                if (callback1 != null) {
                    callback1.onLeScan(device, rssi, scanRecord);
                }
                if (device.getAddress().equals(getMac())){
                    Entry entry = new Entry(rssi, scanRecord);
                    onSuccess(entry);
                }
            };
            scanErrorCallback = errorCode -> {
                if (callback3 != null) {
                    callback3.onScanError(errorCode);
                }
                onFail(new IllegalStateException(String.format("scan error errorCode:%d", errorCode)));
            };
            getBle().addScanCallback(scanCallback);
            getBle().addScanStatusCallback(scanStatusCallback);
            getBle().addScanErrorCallback(scanErrorCallback);
            SimpleScanConfig config = new SimpleScanConfig();
            if (BleGlobalConfig.globalScanConfig!=null){
                BleGlobalConfig.globalScanConfig.toApplyConfig(config);
            }
            if (isRepeatCallback !=null){
                config.setRepeatCallback(isRepeatCallback);
            }
            if (filterName!=null){
                config.setDeviceName(filterName);
            }
            if (filterServiceUuid!=null){
                config.setServiceUuid(filterServiceUuid);
            }
            setMessageOption(getBle().startScan(scanTime, null, config));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getBle().rmScanCallback(scanCallback);
            getBle().rmScanStatusCallback(scanStatusCallback);
            getBle().rmScanErrorCallback(scanErrorCallback);
        }
    }


}
