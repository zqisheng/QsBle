package com.zqs.ble.core.api;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.scan.WrapScanConfig;
import com.zqs.ble.core.utils.fun.Function2;
import com.zqs.ble.core.utils.fun.Function3;

import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-16
 *   @description
 */
public interface IBleOption {

    void connect(String mac, long timeout, int reconnectCount, Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback);

    void disconnect(String mac);

    void write(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount);

    void writeNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount);

    void writeByLock(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallbac);

    void writeByLockNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount, Function2<Boolean,Integer> writeCallback);

    void writeDesc(String mac,UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value);

    void read(String mac,UUID serviceUuid, UUID chacUuid);

    void readDesc(String mac,UUID serviceUuid, UUID chacUuid, UUID descUuid);

    void openNotify(String mac,UUID serviceUuid, UUID chacUuid);

    void cancelNotify(String mac,UUID serviceUuid, UUID chacUuid);

    void setMtu(String mac, int mtu);

    void readRssi(String mac);

    void readPhy(String mac);

    void requestConnectionPriority(String mac,int connectionPriority);

    void setPreferredPhy(String mac, int txPhy, int rxPhy, int phyOptions);

    void startScan(long time, IScanCallback callback, WrapScanConfig config);

    void startScanOnlyLollipop(long time, List<ScanFilter> filters, ScanSettings settings, IScanCallback callback);

    void stopScan();


}
