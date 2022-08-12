package com.zqs.ble.message.builder;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.QsBle;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.abs.IScanErrorCallback;
import com.zqs.ble.core.callback.abs.IScanStatusCallback;
import com.zqs.ble.core.callback.scan.WrapScanConfig;
import com.zqs.ble.message.pojo.Entry;

import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class StartScanChainBuilder extends BleChainBuilder<StartScanChainBuilder, StartScanChainBuilder.StartScanChain,HashMap<String, Entry<Integer,byte[]>>> {

    private StartScanChain chain = new StartScanChain();

    public StartScanChainBuilder(Queue<BleChainBuilder> chains) {
        super(chains);
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

    public StartScanChainBuilder isFindStopScan(boolean findStopScan) {
        chain.isFindStopScan = findStopScan;
        return this;
    }

    public StartScanChainBuilder filterServiceUuids(UUID serviceUuids){
        chain.filterServiceUuid = serviceUuids;
        return this;
    }

    public StartScanChainBuilder distinct(){
        chain.distinct = true;
        return this;
    }

    public StartScanChainBuilder filterName(String name){
        chain.filterName = name;
        return this;
    }

    @Override
    protected void verifyMac(String mac) {
        //null impl
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

    public static class StartScanChain extends BleChain<HashMap<String, Entry<Integer,byte[]>>> {

        private long scanTime = BleGlobalConfig.scanTime;
        private boolean isFindStopScan;

        private IScanCallback scanCallback;
        private IScanStatusCallback scanStatusCallback;
        private IScanErrorCallback scanErrorCallback;

        private IScanCallback callback1;
        private IScanStatusCallback callback2;
        private IScanErrorCallback callback3;
        private String targetMac;
        private boolean isRecordDevice = false;

        private Boolean distinct;
        private String filterName;
        private UUID filterServiceUuid;

        private HashMap<String, Entry<Integer,byte[]>> scanDevices;

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
                setTimeout(scanTime + 500);
            }
            if (isRecordDevice){
                scanDevices = new HashMap<>();
            }
        }

        @Override
        public void handle() {
            scanCallback = (device, rssi, scanRecord) -> {
                if (callback1 != null) {
                    callback1.onLeScan(device, rssi, scanRecord);
                }
                if (targetMac!=null&&!targetMac.isEmpty()){
                    if (isRecordDevice){
                        Entry<Integer, byte[]> entry = scanDevices.get(device.getAddress());
                        if (entry==null){
                            entry = new Entry<>();
                            scanDevices.put(device.getAddress(), entry);
                        }
                        entry.first = rssi;
                        entry.second = scanRecord;
                    }
                    if (targetMac.equals(device.getAddress())){
                        if (isFindStopScan){
                            QsBle.getInstance().stopScan();
                        }
                        onSuccess(scanDevices);
                    }
                }else{
                    if (isRecordDevice){
                        Entry<Integer, byte[]> entry = scanDevices.get(device.getAddress());
                        if (entry==null){
                            entry = new Entry<>();
                            scanDevices.put(device.getAddress(), entry);
                        }
                        entry.first = rssi;
                        entry.second = scanRecord;
                    }
                }
            };
            scanStatusCallback = isScanning -> {
                if (callback2 != null) {
                    callback2.onScanStatusChanged(isScanning);
                }
                if (targetMac!=null&&!targetMac.isEmpty()&&!isScanning) {
                    onFail(new IllegalStateException(String.format("scan stop, but device not found")));
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
            WrapScanConfig config = new WrapScanConfig();
            if (BleGlobalConfig.globalScanConfig!=null){
                BleGlobalConfig.globalScanConfig.toApplyConfig(config);
            }
            if (distinct!=null){
                config.setRepeatCallback(distinct);
            }
            if (filterName!=null){
                config.setDeviceName(filterName);
            }
            if (filterServiceUuid!=null){
                config.setServiceUuid(filterServiceUuid);
            }
            getBle().startScan(scanTime, null, config);
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
