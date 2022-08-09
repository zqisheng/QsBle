package com.zqs.ble.impl;

import com.zqs.ble.core.api.IBleCallback;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/*
 *   @author zhangqisheng
 *   @date 2022-02-23
 *   @description 回调的统一转发管理
 */
public class DefaultBleCallbackManage implements IBleCallback {

    private List<IBlueStatusCallback> blueStatusCallbacks = new LinkedList<>();
    private List<IScanStatusCallback> scanStatusCallbacks = new LinkedList();
    private List<IScanCallback> scanCallbacks = new LinkedList();
    private List<IScanErrorCallback> scanErrorCodeCallbacks =  new LinkedList();
    private Map<String, List<IChacChangeCallback>> chacChangeCallbacks = new HashMap<>();
    private Map<String, List<IChacReadCallback>> chacReadCallbacks = new HashMap<>();
    private Map<String, List<IChacWriteCallback>> chacWriteCallbacks = new HashMap<>();
    private Map<String, List<IConnectionUpdatedCallback>> connectionUpdatedCallbacks = new HashMap<>();
    private Map<String, List<IConnectStatusChangeCallback>> connectStatusChangeCallbacks = new HashMap<>();
    private Map<String, List<IDescReadCallback>> descReadCallbacks = new HashMap<>();
    private Map<String, List<IDescWriteCallback>> descWriteCallbacks = new HashMap<>();
    private Map<String, List<IMtuChangeCallback>> mtuChangeCallbacks = new HashMap<>();
    private Map<String, List<IReadRssiCallback>> readRssiCallbacks = new HashMap<>();
    private Map<String, List<IServicesDiscoveredCallback>> servicesDiscoveredCallbacks = new HashMap<>();
    private Map<String, IChacWriteCallback> lockWriteChacCallbacks = new HashMap<>();
    private Map<String, List<INotifyStatusChangedCallback>> notifyStatusCallbacks = new HashMap<>();
    private Map<String, List<INotifyFailCallback>> notifyFailCallbacks = new HashMap<>();
    private Map<String, List<IBleMultiPkgsCallback>> multiPkgsCallbacks = new HashMap<>();
    private Map<String, List<IPhyReadCallback>> phyReadCallbacks = new HashMap<>();
    private Map<String, List<IPhyUpdateCallback>> phyUpdateCallbacks = new HashMap<>();

    @Override
    public List<IPhyReadCallback> getPhyReadCallbacks(String mac) {
        return phyReadCallbacks.get(mac);
    }

    @Override
    public List<IPhyUpdateCallback> getPhyUpdateCallbacks(String mac) {
        return phyUpdateCallbacks.get(mac);
    }

    @Override
    public void addPhyUpdateCallback(String mac, IPhyUpdateCallback callback){
        mac = mac.toUpperCase();
        List<IPhyUpdateCallback> callbacks= phyUpdateCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            phyUpdateCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmPhyUpdateCallback(String mac,IPhyUpdateCallback callback){
        List<IPhyUpdateCallback> callbacks= phyUpdateCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addPhyReadCallback(String mac, IPhyReadCallback callback){
        mac = mac.toUpperCase();
        List<IPhyReadCallback> callbacks= phyReadCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            phyReadCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmPhyReadCallback(String mac,IPhyReadCallback callback){
        List<IPhyReadCallback> callbacks= phyReadCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public List<INotifyStatusChangedCallback> getNotifyStatusChangedCallbacks(String mac) {
        return notifyStatusCallbacks.get(mac);
    }

    @Override
    public List<INotifyFailCallback> getNotifyFailCallbacks(String mac) {
        return notifyFailCallbacks.get(mac);
    }

    @Override
    public List<IBleMultiPkgsCallback> getBleMultiPkgsCallbacks(String mac,UUID chacUuid) {
        return multiPkgsCallbacks.get(mac.toUpperCase()+chacUuid.toString());
    }

    @Override
    public void addBleMultiPkgsCallback(String mac, UUID chacUuid, IBleMultiPkgsCallback callback){
        mac = mac.toUpperCase();
        String key = mac + chacUuid.toString();
        List<IBleMultiPkgsCallback> callbacks=multiPkgsCallbacks.get(key);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            multiPkgsCallbacks.put(key, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmBleMultiPkgsCallback(String mac,UUID chacUuid,IBleMultiPkgsCallback callback){
        List<IBleMultiPkgsCallback> callbacks=multiPkgsCallbacks.get(mac.toUpperCase()+chacUuid.toString());
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addNotifyFailCallback(String mac,INotifyFailCallback callback){
        mac = mac.toUpperCase();
        List<INotifyFailCallback> callbacks=notifyFailCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            notifyFailCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmNotifyFailCallback(String mac,INotifyFailCallback callback){
        List<INotifyFailCallback> callbacks=notifyFailCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addNotifyStatusCallback(String mac,INotifyStatusChangedCallback callback){
        mac = mac.toUpperCase();
        List<INotifyStatusChangedCallback> callbacks=notifyStatusCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            notifyStatusCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmNotifyStatusCallback(String mac,INotifyStatusChangedCallback callback){
        List<INotifyStatusChangedCallback> callbacks=notifyStatusCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void rmLockWriteChacCallback(String mac){
        lockWriteChacCallbacks.remove(mac);
    }

    @Override
    public void setLockWriteChacCallback(String mac, IChacWriteCallback callback){
        lockWriteChacCallbacks.put(mac, callback);
    }

    @Override
    public IChacWriteCallback getLockWriteChacCallback(String mac){
        return lockWriteChacCallbacks.get(mac);
    }

    @Override
    public void addBleStatusCallback(IBlueStatusCallback callback){
        blueStatusCallbacks.add(callback);
    }

    @Override
    public void rmBleStatusCallback(IBlueStatusCallback callback){
        blueStatusCallbacks.remove(callback);
    }

    @Override
    public void addScanStatusCallback(IScanStatusCallback callback){
        scanStatusCallbacks.add(callback);
    }

    @Override
    public void rmScanStatusCallback(IScanStatusCallback callback){
        scanStatusCallbacks.remove(callback);
    }

    @Override
    public void addScanCallback(IScanCallback callback){
        scanCallbacks.add(callback);
    }

    @Override
    public void rmScanCallback(IScanCallback callback){
        scanCallbacks.remove(callback);
    }

    @Override
    public void addScanErrorCallback(IScanErrorCallback callback){
        scanErrorCodeCallbacks.add(callback);
    }

    @Override
    public void rmScanErrorCallback(IScanErrorCallback callback){
        scanErrorCodeCallbacks.remove(callback);
    }

    @Override
    public void addChacChangeCallback(String mac,IChacChangeCallback callback){
        mac = mac.toUpperCase();
        List<IChacChangeCallback> callbacks=chacChangeCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            chacChangeCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmChacChangeCallback(String mac,IChacChangeCallback callback){
        List<IChacChangeCallback> callbacks=chacChangeCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addChacReadCallback(String mac, IChacReadCallback callback){
        mac = mac.toUpperCase();
        List<IChacReadCallback> callbacks=chacReadCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            chacReadCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmChacReadCallback(String mac, IChacReadCallback callback){
        List<IChacReadCallback> callbacks=chacReadCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addChacWriteCallback(String mac, IChacWriteCallback callback){
        mac = mac.toUpperCase();
        List<IChacWriteCallback> callbacks=chacWriteCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            chacWriteCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmChacWriteCallback(String mac, IChacWriteCallback callback){
        List<IChacWriteCallback> callbacks=chacWriteCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addConnectionUpdatedCallback(String mac,IConnectionUpdatedCallback callback){
        mac = mac.toUpperCase();
        List<IConnectionUpdatedCallback> callbacks=connectionUpdatedCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            connectionUpdatedCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmConnectionUpdatedCallback(String mac,IConnectionUpdatedCallback callback){
        List<IConnectionUpdatedCallback> callbacks=connectionUpdatedCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addConnectStatusChangeCallback(String mac,IConnectStatusChangeCallback callback){
        mac = mac.toUpperCase();
        List<IConnectStatusChangeCallback> callbacks=connectStatusChangeCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            connectStatusChangeCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmConnectStatusChangeCallback(String mac,IConnectStatusChangeCallback callback){
        List<IConnectStatusChangeCallback> callbacks=connectStatusChangeCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addDescReadCallback(String mac,IDescReadCallback callback){
        mac = mac.toUpperCase();
        List<IDescReadCallback> callbacks=descReadCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            descReadCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmDescReadCallback(String mac,IDescReadCallback callback){
        List<IDescReadCallback> callbacks=descReadCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addDescWriteCallback(String mac,IDescWriteCallback callback){
        mac = mac.toUpperCase();
        List<IDescWriteCallback> callbacks=descWriteCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            descWriteCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmDescWriteCallback(String mac,IDescWriteCallback callback){
        List<IDescWriteCallback> callbacks=descWriteCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addMtuChangeCallback(String mac,IMtuChangeCallback callback){
        mac = mac.toUpperCase();
        List<IMtuChangeCallback> callbacks=mtuChangeCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            mtuChangeCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmMtuChangeCallback(String mac,IMtuChangeCallback callback){
        List<IMtuChangeCallback> callbacks=mtuChangeCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addReadRssiCallback(String mac, IReadRssiCallback callback){
        mac = mac.toUpperCase();
        List<IReadRssiCallback> callbacks=readRssiCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            readRssiCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmReadRssiCallback(String mac, IReadRssiCallback callback){
        List<IReadRssiCallback> callbacks=readRssiCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void addServicesDiscoveredCallback(String mac,IServicesDiscoveredCallback callback){
        mac = mac.toUpperCase();
        List<IServicesDiscoveredCallback> callbacks=servicesDiscoveredCallbacks.get(mac);
        if (callbacks==null){
            callbacks = new ArrayList<>();
            callbacks.add(callback);
            servicesDiscoveredCallbacks.put(mac, callbacks);
        }else{
            callbacks.add(callback);
        }
    }

    @Override
    public void rmServicesDiscoveredCallback(String mac,IServicesDiscoveredCallback callback){
        List<IServicesDiscoveredCallback> callbacks=servicesDiscoveredCallbacks.get(mac);
        if (callbacks!=null){
            callbacks.remove(callback);
        }
    }

    @Override
    public void clear(){
        blueStatusCallbacks.clear();
        scanStatusCallbacks.clear();
        scanCallbacks.clear();
        scanErrorCodeCallbacks.clear();
        chacChangeCallbacks.clear();
        chacReadCallbacks.clear();
        chacWriteCallbacks.clear();
        connectionUpdatedCallbacks.clear();
        connectStatusChangeCallbacks.clear();
        descReadCallbacks.clear();
        descWriteCallbacks.clear();
        mtuChangeCallbacks.clear();
        readRssiCallbacks.clear();
        servicesDiscoveredCallbacks.clear();
        lockWriteChacCallbacks.clear();
        notifyStatusCallbacks.clear();
        notifyFailCallbacks.clear();
        multiPkgsCallbacks.clear();
        phyReadCallbacks.clear();
        phyUpdateCallbacks.clear();
    }

    @Override
    public void removeBleCallback(String mac){
        chacChangeCallbacks.remove(mac);
        chacReadCallbacks.remove(mac);
        chacWriteCallbacks.remove(mac);
        connectionUpdatedCallbacks.remove(mac);
        connectStatusChangeCallbacks.remove(mac);
        descReadCallbacks.remove(mac);
        descWriteCallbacks.remove(mac);
        mtuChangeCallbacks.remove(mac);
        readRssiCallbacks.remove(mac);
        servicesDiscoveredCallbacks.remove(mac);
        lockWriteChacCallbacks.remove(mac);
        notifyStatusCallbacks.remove(mac);
        notifyFailCallbacks.remove(mac);
        multiPkgsCallbacks.remove(mac);
        phyReadCallbacks.remove(mac);
        phyUpdateCallbacks.remove(mac);
    }

    @Override
    public List<IBlueStatusCallback> getBlueStatusCallbacks() {
        return blueStatusCallbacks;
    }

    @Override
    public List<IScanStatusCallback> getScanStatusCallbacks() {
        return scanStatusCallbacks;
    }

    @Override
    public List<IChacChangeCallback> getChacChangeCallbacks(String mac) {
        return chacChangeCallbacks.get(mac);
    }

    @Override
    public List<IChacReadCallback> getChacReadCallbacks(String mac) {
        return chacReadCallbacks.get(mac);
    }

    @Override
    public List<IChacWriteCallback> getChacWriteCallbacks(String mac) {
        return chacWriteCallbacks.get(mac);
    }

    @Override
    public List<IConnectionUpdatedCallback> getConnectionUpdatedCallbacks(String mac) {
        return connectionUpdatedCallbacks.get(mac);
    }

    @Override
    public List<IConnectStatusChangeCallback> getConnectStatusChangeCallbacks(String mac) {
        return connectStatusChangeCallbacks.get(mac);
    }

    @Override
    public List<IDescReadCallback> getDescReadCallbacks(String mac) {
        return descReadCallbacks.get(mac);
    }

    @Override
    public List<IDescWriteCallback> getDescWriteCallbacks(String mac) {
        return descWriteCallbacks.get(mac);
    }

    @Override
    public List<IMtuChangeCallback> getMtuChangeCallbacks(String mac) {
        return mtuChangeCallbacks.get(mac);
    }

    @Override
    public List<IReadRssiCallback> getReadRssiCallbacks(String mac) {
        return readRssiCallbacks.get(mac);
    }

    @Override
    public List<IScanCallback> getScanCallbacks() {
        return scanCallbacks;
    }

    @Override
    public List<IScanErrorCallback> getScanErrorCallbacks() {
        return scanErrorCodeCallbacks;
    }

    @Override
    public List<IServicesDiscoveredCallback> getServicesDiscoveredCallbacks(String mac) {
        return servicesDiscoveredCallbacks.get(mac);
    }

}
