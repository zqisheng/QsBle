package com.zqs.ble.core.deamon.message.scan;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.abs.IScanStatusCallback;
import com.zqs.ble.core.callback.scan.WrapScanConfig;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class StartScanMessage extends AbsBleMessage implements IBleScanMessage {

    private List<ScanFilter> filters;
    private WrapScanConfig wrapFilter;
    private ScanSettings settings;
    private IScanCallback callback;
    private long scanTime = BleGlobalConfig.scanTime;

    public StartScanMessage(IScanCallback callback, long scanTime) {
        super("");
        this.callback = callback;
        this.scanTime = scanTime;
    }

    public void setWrapFilter(WrapScanConfig wrapFilter) {
        this.wrapFilter = wrapFilter;
    }

    public void setFilters(List<ScanFilter> filters) {
        this.filters = filters;
    }

    public void setSettings(ScanSettings settings) {
        this.settings = settings;
    }

    @Override
    public void verifyMessage() {
        //空实现
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (getSimpleBle().isScaning()){
            BleLog.e("Scanning, please stop scanning first");
            return;
        }
        if (wrapFilter!=null){
            getSimpleBle().getBleScanOption().startScan(callback, wrapFilter);
        }else if (filters!=null&&settings!=null){
            getSimpleBle().getBleScanOption().startScanOnlyLollipop(filters, settings, callback);
        }else{
            getSimpleBle().getBleScanOption().startScan(callback,null);
        }
        getSimpleBle().getBleScanOption().onScanStart();
        getSimpleBle().setScanState(true);
        List<IScanStatusCallback> callbacks = getSimpleBle().getCallbackManage().getScanStatusCallbacks();
        for (IScanStatusCallback callback : callbacks) {
            callback.onScanStatusChanged(true);
        }
        StopScanMessage message = new StopScanMessage();
        getSimpleBle().sendMessageByDelay(message, scanTime);
    }
}
