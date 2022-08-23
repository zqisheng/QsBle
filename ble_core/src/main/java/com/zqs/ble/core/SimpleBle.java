package com.zqs.ble.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.zqs.ble.core.api.DefaultMultiPackageAssembly;
import com.zqs.ble.core.api.IBleCallback;
import com.zqs.ble.core.api.IBleMessageSender;
import com.zqs.ble.core.api.IBleOption;
import com.zqs.ble.core.api.IMultiPackageAssembly;
import com.zqs.ble.core.callback.BleGattCallback;
import com.zqs.ble.core.callback.GlobalBleCallback;
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
import com.zqs.ble.core.callback.scan.BleScanOption;
import com.zqs.ble.core.callback.scan.SimpleScanConfig;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.deamon.message.callback.OnBlueStatusChangedMessage;
import com.zqs.ble.core.deamon.message.order.FrontMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.fun.BooleanFunction;
import com.zqs.ble.core.utils.fun.Function2;
import com.zqs.ble.core.utils.fun.Function3;
import com.zqs.ble.core.utils.fun.IMessageOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-13
 *   @description
 */
public class SimpleBle implements IBleMessageSender, IBleOption,IBleCallback {

    private Context context;

    private IBleOption bleOption;

    private IBleMessageSender sender;

    private IBleCallback callbackManage;

    private GlobalBleCallback bleGlobalGattCallback;

    private BleScanOption bleScanOption;

    private boolean isScaning = false;

    private int currentMtu = 20;
    //是否启用严格模式
    private boolean isStrictMode = false;

    private static Map<String, IMultiPackageAssembly> chacAssembly = new HashMap<>();

    private Map<String, BluetoothGatt> gatts = new HashMap<>();
    private Map<String, BleDevice> bleDevices = new HashMap<>();

    private BroadcastReceiver blueStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (status == BluetoothAdapter.STATE_ON) {
                    BleLog.d("","bluetooth is open");
                    sendMessage(new FrontMessage(){
                        @Override
                        public void onHandlerMessage() {
                            sendMessage(new OnBlueStatusChangedMessage(true));
                        }
                    });
                }else if(status == BluetoothAdapter.STATE_OFF){
                    BleLog.d("","bluetooth is close");
                    sendMessage(new FrontMessage(){
                        @Override
                        public void onHandlerMessage() {
                            sendMessage(new OnBlueStatusChangedMessage(false));
                        }
                    });
                }
            }
        }
    };

    public void init(Context context, IBleOption bleOption, IBleCallback callbackManage, IBleMessageSender sender) {
        this.context = context;
        this.bleOption = bleOption;
        this.sender = sender;
        this.callbackManage = callbackManage;
        this.bleScanOption = new BleScanOption();
        AbsBleMessage.setSimpleBle(this);
        BleGattCallback.setSimpleBle(this);
        bleScanOption.setSimpleBle(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(blueStatusReceiver, filter);
        veriryParams();
    }

    public boolean isStrictMode() {
        return isStrictMode;
    }

    public void setStrictMode(boolean strictMode) {
        isStrictMode = strictMode;
    }

    public void setMultiPackageAssembly(String mac, IMultiPackageAssembly parser){
        sendMessage(new FrontMessage(){
            @Override
            public void onHandlerMessage() {
                chacAssembly.put(mac.toUpperCase(), parser);
            }
        });
    }

    public IMultiPackageAssembly getMultiPackageAssembly(String mac){
        assertCurrentIsSenderThread();
        IMultiPackageAssembly assembly = chacAssembly.get(mac.toUpperCase());
        if (assembly==null){
            assembly = new DefaultMultiPackageAssembly();
            chacAssembly.put(mac.toUpperCase(), assembly);
        }
        return assembly;
    }

    public List<BluetoothGattService> getGattService(String mac){
        BluetoothGatt gatt = gatts.get(mac);
        if (gatt!=null){
            return gatt.getServices();
        }
        return null;
    }

    public int getCurrentMtu() {
        return currentMtu;
    }

    public void setCurrentMtu(int currentMtu) {
        assertCurrentIsSenderThread();
        this.currentMtu = currentMtu;
    }

    public boolean isScaning() {
        return isScaning;
    }

    public void setScanState(boolean scaning) {
        isScaning = scaning;
    }

    public void setGlobalGattCallback(GlobalBleCallback bleGlobalGattCallback) {
        this.bleGlobalGattCallback = bleGlobalGattCallback;
    }

    public GlobalBleCallback getGlobalBleGattCallback() {
        return bleGlobalGattCallback;
    }

    public BleScanOption getBleScanOption() {
        return bleScanOption;
    }

    private void veriryParams(){
        if (context==null) throw new IllegalArgumentException("context require nonnull");
        if (bleOption==null) throw new IllegalArgumentException("bleOption require nonnull");
        if (sender==null) throw new IllegalArgumentException("sender require nonnull");
        if (callbackManage==null) throw new IllegalArgumentException("callbackManage require nonnull");
    }

    public Context getContext() {
        return context;
    }

    public void setGatt(String mac, BluetoothGatt gatt){
        assertCurrentIsSenderThread();
        gatts.put(mac, gatt);
    }

    public BluetoothGatt getGatt(String mac){
        return gatts.get(mac);
    }

    public void assertCurrentIsSenderThread(){
        if (!sender.currentIsSenderThread()){
            throw new IllegalStateException("Can only be run in the specified thread");
        }
    }

    public boolean blueIsOpen(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public void clearConnectStatus(){
        assertCurrentIsSenderThread();
        bleDevices.clear();
        if (!gatts.isEmpty()) {
            for (String mac : gatts.keySet()) {
                BluetoothGatt gatt = gatts.get(mac);
                gatt.disconnect();
                gatt.close();
                setGatt(mac,null);
            }
            gatts.clear();
        }
    }

    public void setAutoReconnectCount(String mac,int autoReconnectCount){
        assertCurrentIsSenderThread();
        mac = mac.toUpperCase();
        BleDevice bleDevice = bleDevices.get(mac);
        if (bleDevice==null){
            bleDevice = new BleDevice(mac, false);
        }
        bleDevice.setAutoConnectCount(autoReconnectCount);
    }

    public void updateConnectStatus(String mac, boolean isConnect, int status, int profileState,String disconnecter){
        assertCurrentIsSenderThread();
        if (!isConnect){
            currentMtu = 20;
        }
        BleDevice bleDevice = bleDevices.get(mac);
        if (bleDevice==null){
            bleDevice = new BleDevice(mac, isConnect);
            bleDevices.put(mac, bleDevice);
        }
        bleDevice.setLastGattCode(new int[]{status, profileState});
        bleDevice.setConnect(isConnect);
        bleDevice.setConnectStatusUpdateTime(System.currentTimeMillis());
        bleDevice.setDisconnecter(disconnecter);
        if (bleDevice.getAutoConnectCount() > 0 && disconnecter != null && disconnecter.equals("device")) {
            connect(mac, BleGlobalConfig.connectTimeout, bleDevice.getAutoConnectCount(), null);
        }
    }

    public List<BluetoothDevice> getConnectDevices(){
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getConnectedDevices(BluetoothProfile.GATT);
    }

    public int[] getConnectCode(String mac){
        BleDevice bleDevice = bleDevices.get(mac.toUpperCase());
        if (bleDevice==null) return null;
        return bleDevice.getLastGattCode();
    }

    public Boolean isConnect(String mac){
        BleDevice bleDevice = bleDevices.get(mac.toUpperCase());
        if (bleDevice==null) return false;
        return bleDevice.isConnect();
    }

    public int getConnectCount(){
        return getConnectDevices().size();
    }

    public void handleLruDisconnect(){
        sendMessage(new FrontMessage() {
            @Override
            public void onHandlerMessage() {
                String lruMac = null;
                long time = 0;
                for (BleDevice bleDevice:bleDevices.values()){
                    if (bleDevice.isConnect()&&bleDevice.getConnectStatusUpdateTime()<time){
                        time = bleDevice.getConnectStatusUpdateTime();
                        lruMac = bleDevice.getMac();
                    }
                }
                if (lruMac!=null){
                    disconnect(lruMac);
                }
            }
        });
    }

    public Long getConnectStatusUpdateTime(String mac){
        BleDevice bleDevice = bleDevices.get(mac.toUpperCase());
        if (bleDevice==null) return 0L;
        return bleDevice.getConnectStatusUpdateTime();
    }

    public IBleCallback getCallbackManage() {
        return callbackManage;
    }

    @Override
    public void sendMessage(AbsMessage message) {
        sender.sendMessage(message);
    }

    @Override
    public void rmMessage(AbsMessage message) {
        sender.rmMessage(message);
    }

    @Override
    public void rmMessages(String token) {
        sender.rmMessages(token);
    }

    @Override
    public void sendMessageByDelay(AbsMessage message, long delay) {
        sender.sendMessageByDelay(message, delay);
    }

    @Override
    public boolean currentIsSenderThread() {
        return sender.currentIsSenderThread();
    }

    @Override
    public IMessageOption connect(String mac, long timeout, int reconnectCount, Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback) {
        return bleOption.connect(mac, timeout, reconnectCount,connectFailCallback);
    }

    @Override
    public IMessageOption disconnect(String mac) {
        return bleOption.disconnect(mac);
    }

    @Override
    public IMessageOption write(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount) {
        return bleOption.write(mac, serviceUuid, chacUuid, value, retryWriteCount);
    }

    @Override
    public IMessageOption writeNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount) {
        return bleOption.writeNoRsp(mac, serviceUuid, chacUuid, value, retryWriteCount);
    }

    @Override
    public IMessageOption writeByLock(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallback) {
        return bleOption.writeByLock(mac, serviceUuid, chacUuid, value, retryWriteCount,writeCallback);
    }

    @Override
    public IMessageOption writeByLockNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallback) {
        return bleOption.writeByLockNoRsp(mac, serviceUuid, chacUuid, value, retryWriteCount,writeCallback);
    }

    @Override
    public IMessageOption writeDesc(String mac, UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value) {
        return bleOption.writeDesc(mac, serviceUuid, chacUuid, descUuid, value);
    }

    @Override
    public IMessageOption read(String mac, UUID serviceUuid, UUID chacUuid) {
        return bleOption.read(mac, serviceUuid, chacUuid);
    }

    @Override
    public IMessageOption readDesc(String mac, UUID serviceUuid, UUID chacUuid, UUID descUuid) {
        return bleOption.readDesc(mac, serviceUuid, chacUuid, descUuid);
    }

    @Override
    public IMessageOption openNotify(String mac, UUID serviceUuid, UUID chacUuid) {
        return bleOption.openNotify(mac, serviceUuid, chacUuid);
    }

    @Override
    public IMessageOption cancelNotify(String mac, UUID serviceUuid, UUID chacUuid) {
        return bleOption.cancelNotify(mac, serviceUuid, chacUuid);
    }

    @Override
    public IMessageOption setMtu(String mac, int mtu) {
        return bleOption.setMtu(mac, mtu);
    }

    @Override
    public IMessageOption readRssi(String mac) {
        return bleOption.readRssi(mac);
    }

    @Override
    public IMessageOption readPhy(String mac) {
        return bleOption.readPhy(mac);
    }

    @Override
    public IMessageOption requestConnectionPriority(String mac, int connectionPriority) {
        return bleOption.requestConnectionPriority(mac, connectionPriority);
    }

    @Override
    public IMessageOption setPreferredPhy(String mac, int txPhy, int rxPhy, int phyOptions) {
        return  bleOption.setPreferredPhy(mac, txPhy, rxPhy, phyOptions);
    }

    @Override
    public IMessageOption startScan(long time, IScanCallback callback, SimpleScanConfig config) {
        return bleOption.startScan(time, callback,config);
    }

    @Override
    public IMessageOption startScanOnlyLollipop(long time, List<ScanFilter> filters, ScanSettings settings, IScanCallback callback) {
        return bleOption.startScanOnlyLollipop(time, filters, settings, callback);
    }

    @Override
    public IMessageOption stopScan() {
        return bleOption.stopScan();
    }

    @Override
    public List<IPhyReadCallback> getPhyReadCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getPhyReadCallbacks(mac);
    }

    @Override
    public List<IPhyUpdateCallback> getPhyUpdateCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getPhyUpdateCallbacks(mac);
    }

    @Override
    public void addPhyUpdateCallback(String mac, IPhyUpdateCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addPhyUpdateCallback(mac, callback);
    }

    @Override
    public void rmPhyUpdateCallback(String mac, IPhyUpdateCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmPhyUpdateCallback(mac, callback);
    }

    @Override
    public void addPhyReadCallback(String mac, IPhyReadCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addPhyReadCallback(mac, callback);
    }

    @Override
    public void rmPhyReadCallback(String mac, IPhyReadCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmPhyReadCallback(mac, callback);
    }

    @Override
    public List<INotifyStatusChangedCallback> getNotifyStatusChangedCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getNotifyStatusChangedCallbacks(mac);
    }

    @Override
    public List<INotifyFailCallback> getNotifyFailCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getNotifyFailCallbacks(mac);
    }

    @Override
    public List<IBleMultiPkgsCallback> getBleMultiPkgsCallbacks(String mac, UUID chacUuid) {
        assertCurrentIsSenderThread();
        return callbackManage.getBleMultiPkgsCallbacks(mac,chacUuid);
    }

    @Override
    public void addBleMultiPkgsCallback(String mac, UUID chacUuid, IBleMultiPkgsCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addBleMultiPkgsCallback(mac, chacUuid, callback);
    }

    @Override
    public void rmBleMultiPkgsCallback(String mac, UUID chacUuid, IBleMultiPkgsCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmBleMultiPkgsCallback(mac, chacUuid, callback);
    }

    @Override
    public void addNotifyFailCallback(String mac, INotifyFailCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addNotifyFailCallback(mac, callback);
    }

    @Override
    public void rmNotifyFailCallback(String mac, INotifyFailCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmNotifyFailCallback(mac, callback);
    }

    @Override
    public void addNotifyStatusCallback(String mac, INotifyStatusChangedCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addNotifyStatusCallback(mac,callback);
    }

    @Override
    public void rmNotifyStatusCallback(String mac, INotifyStatusChangedCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmNotifyStatusCallback(mac, callback);
    }

    @Override
    public void rmLockWriteChacCallback(String mac) {
        assertCurrentIsSenderThread();
        callbackManage.rmLockWriteChacCallback(mac);
    }

    @Override
    public void setLockWriteChacCallback(String mac, IChacWriteCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.setLockWriteChacCallback(mac, callback);
    }

    @Override
    public IChacWriteCallback getLockWriteChacCallback(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getLockWriteChacCallback(mac);
    }

    @Override
    public void addBleStatusCallback(IBlueStatusCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addBleStatusCallback(callback);
    }

    @Override
    public void rmBleStatusCallback(IBlueStatusCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmBleStatusCallback(callback);
    }

    @Override
    public void addScanStatusCallback(IScanStatusCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addScanStatusCallback(callback);
    }

    @Override
    public void rmScanStatusCallback(IScanStatusCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmScanStatusCallback(callback);
    }

    @Override
    public void addScanCallback(IScanCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addScanCallback(callback);
    }

    @Override
    public void rmScanCallback(IScanCallback callback) {
        assertCurrentIsSenderThread();
        if (bleScanOption.getScanCallback()==callback){
            bleScanOption.removeScanCallback();
        }
        callbackManage.rmScanCallback(callback);
    }

    @Override
    public void addScanErrorCallback(IScanErrorCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addScanErrorCallback(callback);
    }

    @Override
    public void rmScanErrorCallback(IScanErrorCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmScanErrorCallback(callback);
    }

    @Override
    public void addChacChangeCallback(String mac, IChacChangeCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addChacChangeCallback(mac,callback);
    }

    @Override
    public void rmChacChangeCallback(String mac, IChacChangeCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmChacChangeCallback(mac,callback);
    }

    @Override
    public void addChacReadCallback(String mac, IChacReadCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addChacReadCallback(mac,callback);
    }

    @Override
    public void rmChacReadCallback(String mac, IChacReadCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmChacReadCallback(mac,callback);
    }

    @Override
    public void addChacWriteCallback(String mac, IChacWriteCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addChacWriteCallback(mac,callback);
    }

    @Override
    public void rmChacWriteCallback(String mac, IChacWriteCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmChacWriteCallback(mac,callback);
    }

    @Override
    public void addConnectionUpdatedCallback(String mac, IConnectionUpdatedCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addConnectionUpdatedCallback(mac,callback);
    }

    @Override
    public void rmConnectionUpdatedCallback(String mac, IConnectionUpdatedCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmConnectionUpdatedCallback(mac,callback);
    }

    @Override
    public void addConnectStatusChangeCallback(String mac, IConnectStatusChangeCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addConnectStatusChangeCallback(mac,callback);
    }

    @Override
    public void rmConnectStatusChangeCallback(String mac, IConnectStatusChangeCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmConnectStatusChangeCallback(mac,callback);
    }

    @Override
    public void addDescReadCallback(String mac, IDescReadCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addDescReadCallback(mac,callback);
    }

    @Override
    public void rmDescReadCallback(String mac, IDescReadCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmDescReadCallback(mac,callback);
    }

    @Override
    public void addDescWriteCallback(String mac, IDescWriteCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addDescWriteCallback(mac,callback);
    }

    @Override
    public void rmDescWriteCallback(String mac, IDescWriteCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmDescWriteCallback(mac,callback);
    }

    @Override
    public void addMtuChangeCallback(String mac, IMtuChangeCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addMtuChangeCallback(mac,callback);
    }

    @Override
    public void rmMtuChangeCallback(String mac, IMtuChangeCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmMtuChangeCallback(mac,callback);
    }

    @Override
    public void addReadRssiCallback(String mac, IReadRssiCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addReadRssiCallback(mac,callback);
    }

    @Override
    public void rmReadRssiCallback(String mac, IReadRssiCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmReadRssiCallback(mac,callback);
    }

    @Override
    public void addServicesDiscoveredCallback(String mac, IServicesDiscoveredCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.addServicesDiscoveredCallback(mac,callback);
    }

    @Override
    public void rmServicesDiscoveredCallback(String mac, IServicesDiscoveredCallback callback) {
        assertCurrentIsSenderThread();
        callbackManage.rmServicesDiscoveredCallback(mac,callback);
    }

    @Override
    public void clear() {
        assertCurrentIsSenderThread();
        callbackManage.clear();
    }

    @Override
    public void removeBleCallback(String mac) {
        assertCurrentIsSenderThread();
        callbackManage.getLockWriteChacCallback(mac);
    }


    @Override
    public List<IBlueStatusCallback> getBlueStatusCallbacks() {
        assertCurrentIsSenderThread();
        return callbackManage.getBlueStatusCallbacks();
    }

    @Override
    public List<IScanStatusCallback> getScanStatusCallbacks() {
        assertCurrentIsSenderThread();
        return callbackManage.getScanStatusCallbacks();
    }

    @Override
    public List<IChacChangeCallback> getChacChangeCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getChacChangeCallbacks(mac);
    }

    @Override
    public List<IChacReadCallback> getChacReadCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getChacReadCallbacks(mac);
    }

    @Override
    public List<IChacWriteCallback> getChacWriteCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getChacWriteCallbacks(mac);
    }

    @Override
    public List<IConnectionUpdatedCallback> getConnectionUpdatedCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getConnectionUpdatedCallbacks(mac);
    }

    @Override
    public List<IConnectStatusChangeCallback> getConnectStatusChangeCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getConnectStatusChangeCallbacks(mac);
    }

    @Override
    public List<IDescReadCallback> getDescReadCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getDescReadCallbacks(mac);
    }

    @Override
    public List<IDescWriteCallback> getDescWriteCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getDescWriteCallbacks(mac);
    }

    @Override
    public List<IMtuChangeCallback> getMtuChangeCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getMtuChangeCallbacks(mac);
    }

    @Override
    public List<IReadRssiCallback> getReadRssiCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getReadRssiCallbacks(mac);
    }

    @Override
    public List<IScanCallback> getScanCallbacks() {
        assertCurrentIsSenderThread();
        return callbackManage.getScanCallbacks();
    }

    @Override
    public List<IScanErrorCallback> getScanErrorCallbacks() {
        assertCurrentIsSenderThread();
        return callbackManage.getScanErrorCallbacks();
    }

    @Override
    public List<IServicesDiscoveredCallback> getServicesDiscoveredCallbacks(String mac) {
        assertCurrentIsSenderThread();
        return callbackManage.getServicesDiscoveredCallbacks(mac);
    }

    @Override
    public void rmMessagesByMac(String mac) {
        sender.rmMessagesByMac(mac);
    }

    @Override
    public void clearMessageIf(BooleanFunction<AbsMessage> condition,Runnable clearFinishCallback) {
        sender.clearMessageIf(condition, clearFinishCallback);
    }

}
