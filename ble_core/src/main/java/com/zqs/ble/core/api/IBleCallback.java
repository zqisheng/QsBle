package com.zqs.ble.core.api;

import com.zqs.ble.core.callback.abs.IBleMultiPkgsCallback;
import com.zqs.ble.core.callback.abs.IBlueStatusCallback;
import com.zqs.ble.core.callback.abs.IChacChangeCallback;
import com.zqs.ble.core.callback.abs.IChacReadCallback;
import com.zqs.ble.core.callback.abs.IChacWriteCallback;
import com.zqs.ble.core.callback.abs.IConnectStatusChangeCallback;
import com.zqs.ble.core.callback.abs.IConnectionUpdatedCallback;
import com.zqs.ble.core.callback.abs.IDescReadCallback;
import com.zqs.ble.core.callback.abs.IDescWriteCallback;
import com.zqs.ble.core.callback.abs.IMtuChangeCallback;
import com.zqs.ble.core.callback.abs.INotifyFailCallback;
import com.zqs.ble.core.callback.abs.INotifyStatusChangedCallback;
import com.zqs.ble.core.callback.abs.IPhyReadCallback;
import com.zqs.ble.core.callback.abs.IPhyUpdateCallback;
import com.zqs.ble.core.callback.abs.IReadRssiCallback;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.callback.abs.IScanErrorCallback;
import com.zqs.ble.core.callback.abs.IScanStatusCallback;
import com.zqs.ble.core.callback.abs.IServicesDiscoveredCallback;

import java.util.List;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-16
 *   @description
 */
public interface IBleCallback {


    List<IPhyReadCallback> getPhyReadCallbacks(String mac);

    List<IPhyUpdateCallback> getPhyUpdateCallbacks(String mac);

    void addPhyUpdateCallback(String mac, IPhyUpdateCallback callback);

    void rmPhyUpdateCallback(String mac, IPhyUpdateCallback callback);

    void addPhyReadCallback(String mac, IPhyReadCallback callback);

    void rmPhyReadCallback(String mac, IPhyReadCallback callback);

    List<INotifyStatusChangedCallback> getNotifyStatusChangedCallbacks(String mac);

    List<INotifyFailCallback> getNotifyFailCallbacks(String mac);

    List<IBleMultiPkgsCallback> getBleMultiPkgsCallbacks(String mac, UUID chacUuid);

    void addBleMultiPkgsCallback(String mac, UUID chacUuid, IBleMultiPkgsCallback callback);

    void rmBleMultiPkgsCallback(String mac,UUID chacUuid,IBleMultiPkgsCallback callback);

    void addNotifyFailCallback(String mac,INotifyFailCallback callback);

    void rmNotifyFailCallback(String mac,INotifyFailCallback callback);

    void addNotifyStatusCallback(String mac,INotifyStatusChangedCallback callback);

    void rmNotifyStatusCallback(String mac,INotifyStatusChangedCallback callback);

    void rmLockWriteChacCallback(String mac);

    void setLockWriteChacCallback(String mac, IChacWriteCallback callback);

    IChacWriteCallback getLockWriteChacCallback(String mac);

    void addBleStatusCallback(IBlueStatusCallback callback);

    void rmBleStatusCallback(IBlueStatusCallback callback);

    void addScanStatusCallback(IScanStatusCallback callback);

    void rmScanStatusCallback(IScanStatusCallback callback);

    void addScanCallback(IScanCallback callback);

    void rmScanCallback(IScanCallback callback);

    void addScanErrorCallback(IScanErrorCallback callback);
        
    void rmScanErrorCallback(IScanErrorCallback callback);

    void addChacChangeCallback(String mac,IChacChangeCallback callback);

    void rmChacChangeCallback(String mac,IChacChangeCallback callback);

    void addChacReadCallback(String mac, IChacReadCallback callback);

    void rmChacReadCallback(String mac, IChacReadCallback callback);

    void addChacWriteCallback(String mac, IChacWriteCallback callback);

    void rmChacWriteCallback(String mac, IChacWriteCallback callback);

    void addConnectionUpdatedCallback(String mac,IConnectionUpdatedCallback callback);

    void rmConnectionUpdatedCallback(String mac,IConnectionUpdatedCallback callback);

    void addConnectStatusChangeCallback(String mac,IConnectStatusChangeCallback callback);

    void rmConnectStatusChangeCallback(String mac,IConnectStatusChangeCallback callback);

    void addDescReadCallback(String mac,IDescReadCallback callback);

    void rmDescReadCallback(String mac,IDescReadCallback callback);

    void addDescWriteCallback(String mac,IDescWriteCallback callback);

    void rmDescWriteCallback(String mac,IDescWriteCallback callback);

    void addMtuChangeCallback(String mac,IMtuChangeCallback callback);

    void rmMtuChangeCallback(String mac,IMtuChangeCallback callback);

    void addReadRssiCallback(String mac, IReadRssiCallback callback);

    void rmReadRssiCallback(String mac, IReadRssiCallback callback);

    void addServicesDiscoveredCallback(String mac, IServicesDiscoveredCallback callback);

    void rmServicesDiscoveredCallback(String mac, IServicesDiscoveredCallback callback);

    void clear();

    void removeBleCallback(String mac);

    List<IBlueStatusCallback> getBlueStatusCallbacks();

    List<IScanStatusCallback> getScanStatusCallbacks(); 

    List<IChacChangeCallback> getChacChangeCallbacks(String mac);

    List<IChacReadCallback> getChacReadCallbacks(String mac);

    List<IChacWriteCallback> getChacWriteCallbacks(String mac);

    List<IConnectionUpdatedCallback> getConnectionUpdatedCallbacks(String mac);

    List<IConnectStatusChangeCallback> getConnectStatusChangeCallbacks(String mac);

    List<IDescReadCallback> getDescReadCallbacks(String mac);

    List<IDescWriteCallback> getDescWriteCallbacks(String mac);

    List<IMtuChangeCallback> getMtuChangeCallbacks(String mac);

    List<IReadRssiCallback> getReadRssiCallbacks(String mac);

    List<IScanCallback> getScanCallbacks();

    List<IScanErrorCallback> getScanErrorCallbacks();

    List<IServicesDiscoveredCallback> getServicesDiscoveredCallbacks(String mac);


}
