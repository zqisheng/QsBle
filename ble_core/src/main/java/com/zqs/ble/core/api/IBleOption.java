package com.zqs.ble.core.api;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.scan.SimpleScanConfig;
import com.zqs.ble.core.utils.fun.Function2;
import com.zqs.ble.core.utils.fun.Function3;
import com.zqs.ble.core.utils.fun.IMessageOption;

import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-16
 *   @description
 */
public interface IBleOption {

    IMessageOption connect(String mac, long timeout, int reconnectCount, Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback);

    IMessageOption disconnect(String mac);

    IMessageOption write(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount);

    IMessageOption writeNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount);

    IMessageOption writeByLock(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallbac);

    IMessageOption writeByLockNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount, Function2<Boolean,Integer> writeCallback);

    IMessageOption writeDesc(String mac,UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value);

    IMessageOption read(String mac,UUID serviceUuid, UUID chacUuid);

    IMessageOption readDesc(String mac,UUID serviceUuid, UUID chacUuid, UUID descUuid);

    IMessageOption openNotify(String mac,UUID serviceUuid, UUID chacUuid);

    IMessageOption cancelNotify(String mac,UUID serviceUuid, UUID chacUuid);

    IMessageOption setMtu(String mac, int mtu);

    IMessageOption readRssi(String mac);

    IMessageOption readPhy(String mac);

    IMessageOption requestConnectionPriority(String mac,int connectionPriority);

    IMessageOption setPreferredPhy(String mac, int txPhy, int rxPhy, int phyOptions);

    IMessageOption startScan(long time, IScanCallback callback, SimpleScanConfig config);

    IMessageOption startScanOnlyLollipop(long time, List<ScanFilter> filters, ScanSettings settings, IScanCallback callback);

    IMessageOption stopScan();


}
