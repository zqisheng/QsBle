package com.zqs.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import com.zqs.ble.core.BleConst;
import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.SimpleBle;
import com.zqs.ble.core.api.IBleMessageSender;
import com.zqs.ble.core.api.IMultiPackageAssembly;
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
import com.zqs.ble.core.callback.scan.WrapScanConfig;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.deamon.message.order.IFrontMessage;
import com.zqs.ble.core.utils.Utils;
import com.zqs.ble.core.utils.fun.Function2;
import com.zqs.ble.core.utils.fun.Function3;
import com.zqs.ble.impl.DefaultBleCallbackManage;
import com.zqs.ble.impl.DefaultBleMessageSender;
import com.zqs.ble.impl.DefaultBleOption;
import com.zqs.ble.impl.HandleMessageLooper;
import com.zqs.ble.lifecycle.DestroyLifecycleObserver;
import com.zqs.ble.message.builder.StartScanChainBuilder;
import com.zqs.ble.message.ota.IOtaUpdateCallback;
import com.zqs.ble.message.ota.WriteFileMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;

/*
 *   @author zhangqisheng
 *   @date 2022-07-18
 *   @description
 */
public final class QsBle {

    private static QsBle INSTANCE;

    static {
        INSTANCE = new QsBle();
    }

    private Context context;

    private QsBle(){}

    public static QsBle getInstance(){
        return INSTANCE;
    }

    private static Map<String, IMultiPackageAssembly> chacParser = new HashMap<>();

    public static void assertBluetoothAddress(@NonNull String mac){
        if (!BluetoothAdapter.checkBluetoothAddress(mac)){
            throw new IllegalArgumentException("mac地址格式不正确");
        }
    }

    private SimpleBle ble;

    /**
     * Main线程初始化
     * @param context
     */
    public boolean init(@NonNull Context context){
        return init(context, null);
    }

    /**
     * @param context
     * @param handler
     * @return
     */
    public boolean init(@NonNull Context context, Handler handler) {
        if (ble!=null) throw new IllegalStateException("QsBle already init");
        if (BluetoothAdapter.getDefaultAdapter()==null) return false;
        this.context = context;
        ble = new SimpleBle();
        IBleMessageSender sender;
        if (handler==null){
            sender = new DefaultBleMessageSender();
        }else{
            sender = new HandleMessageLooper(handler);
        }
        ble.init(context,new DefaultBleOption(sender), new DefaultBleCallbackManage(),sender);
        return true;
    }

    public void setDebug(boolean isDebug){
        BleDebugConfig.isDebug = isDebug;
        BleDebugConfig.isPrintFunStack = isDebug;
        BleDebugConfig.isOpenBleThreadLog = isDebug;
        BleDebugConfig.isOpenScanLog = isDebug;
        BleDebugConfig.isOpenBleLooperLog = isDebug;
        BleDebugConfig.isOpenWriteLog = isDebug;
        BleDebugConfig.isOpenGattCallbackLog = isDebug;
    }

    public void setMultiPackageAssembly(@NonNull String mac, @NonNull IMultiPackageAssembly parser){
        ble.setMultiPackageAssembly(mac, parser);
    }

    public int getCurrentMtu(){
        return ble.getCurrentMtu();
    }

    public boolean isScaning(){
        return ble.isScaning();
    }

    public boolean isConnect(String mac) {
        return ble.isConnect(mac);
    }

    public int getConnectCount(){
        return ble.getConnectCount();
    }

    public boolean bluetoothEnable(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public void openBluetooth(){
        BluetoothAdapter.getDefaultAdapter().enable();
    }

    public void openBluetooth(@NonNull Activity activity) {
        if (!bluetoothEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 1);
        }
    }

    public void closeBluetooth(){
        if (bluetoothEnable()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    /**
     * 清理蓝牙缓存
     * 注意:大部分新的android版本,此方法都是无法反射的
     */
    public boolean refreshDeviceCache(@NonNull String mac) {
        BluetoothGatt gatt = ble.getGatt(mac);
        if (gatt != null) {
            try {
                Method method = gatt.getClass().getMethod("refresh", new Class[0]);
                return (boolean)method.invoke(gatt, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Nullable
    public List<BluetoothDevice> getConnectDevices(){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager!=null){
            return bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        }
        return null;
    }
    
    public String getNotifyType(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID notifyUuid){
        BluetoothGattDescriptor descriptor = getGattDescriptor(mac, serviceUuid, notifyUuid, BleConst.clientCharacteristicConfig);
        if (descriptor==null){
            return "disable";
        }
        if (Arrays.equals(descriptor.getValue(),BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){
            return "disable";
        }
        if (Arrays.equals(descriptor.getValue(),BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){
            return "notification";
        }
        if (Arrays.equals(descriptor.getValue(),BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)){
            return "indication";
        }
        return "disable";
    }

    public boolean notifyIsOpen(@NonNull String mac,@NonNull UUID serviceUuid,@NonNull UUID notifyUuid){
        return getNotifyType(mac, serviceUuid, notifyUuid).equals("disable");
    }

    @Nullable
    public List<BluetoothGattService> getGattService(@NonNull String mac){
        return ble.getGattService(mac);
    }

    @Nullable
    public BluetoothGattCharacteristic getGattCharacteristic(@NonNull String mac,UUID serviceUuid,UUID chacUuid){
        List<BluetoothGattService> services = getGattService(mac);
        if (services!=null){
            for (BluetoothGattService service:services){
                if (Utils.uuidIsSame(service.getUuid(),serviceUuid)){
                    return service.getCharacteristic(chacUuid);
                }
            }
        }
        return null;
    }

    @Nullable
    public BluetoothGattDescriptor getGattDescriptor(@NonNull String mac,UUID serviceUuid,UUID chacUuid,UUID descUuid){
        BluetoothGattCharacteristic characteristic = getGattCharacteristic(mac, serviceUuid, chacUuid);
        if (characteristic!=null){
            return characteristic.getDescriptor(descUuid);
        }
        return null;
    }

    public void clearAllOption(@NonNull String mac){
        ble.rmMessagesByMac(mac.toUpperCase());
    }

    public BleChainBuilder chain(@NonNull String mac){
        return new BleChainBuilder(mac.toUpperCase(), new LinkedList<>()) {
            @Override
            public BleChain getBleChain() {
                return null;
            }
        };
    }

    public BleChainBuilder chain(){
        return new StartScanChainBuilder(new LinkedList<>());
    }

    public void connect(@NonNull String mac, long timeout, int reconnectCount, Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback) {
        if (getConnectCount()>= BleGlobalConfig.maxConnectCount&&!isConnect(mac)){
            ble.handleLruDisconnect();
        }
        ble.connect(mac, timeout, reconnectCount,connectFailCallback);
    }

    public void connect(@NonNull String mac, long timeout, int reconnectCount) {
        connect(mac, timeout, reconnectCount,null);
    }

    public void connect(@NonNull String mac, long timeout) {
        connect(mac, timeout,BleGlobalConfig.reconnectCount ,null);
    }

    public void connect(@NonNull String mac) {
        connect(mac, BleGlobalConfig.connectTimeout,BleGlobalConfig.reconnectCount ,null);
    }

    public void disconnect(@NonNull String mac) {
        ble.disconnect(mac);
    }

    public void writeFile(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] fileBytes, @NonNull IOtaUpdateCallback otaUpdateCallback){
        writeFile(mac, serviceUuid, chacUuid, fileBytes.length,BleGlobalConfig.otaSegmentSize, new ByteArrayInputStream(fileBytes), otaUpdateCallback);
    }

    public void writeFile(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull File file, @NonNull IOtaUpdateCallback otaUpdateCallback){
        try {
            writeFile(mac, serviceUuid, chacUuid,(int) file.length(),BleGlobalConfig.otaSegmentSize, new FileInputStream(file), otaUpdateCallback);
        } catch (FileNotFoundException e) {
            otaUpdateCallback.onError(e);
            e.printStackTrace();
        }
    }

    public void writeFile(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, int fileByteCount, int segmentSize, @NonNull InputStream datasource, @NonNull IOtaUpdateCallback otaUpdateCallback){
        WriteFileMessage message = new WriteFileMessage(mac, serviceUuid, chacUuid, fileByteCount, segmentSize, datasource, otaUpdateCallback);
        ble.sendMessage(message);
    }

    public void writeFileNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] fileBytes, @NonNull IOtaUpdateCallback otaUpdateCallback){
        writeFileNoRsp(mac, serviceUuid, chacUuid, fileBytes.length,BleGlobalConfig.otaSegmentSize, new ByteArrayInputStream(fileBytes), otaUpdateCallback);
    }

    public void writeFileNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull File file, @NonNull IOtaUpdateCallback otaUpdateCallback){
        try {
            writeFileNoRsp(mac, serviceUuid, chacUuid, (int) file.length(),BleGlobalConfig.otaSegmentSize, new FileInputStream(file), otaUpdateCallback);
        } catch (FileNotFoundException e) {
            otaUpdateCallback.onError(e);
            e.printStackTrace();
        }
    }

    public void writeFileNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, int fileByteCount, int segmentSize, @NonNull InputStream datasource, @NonNull IOtaUpdateCallback otaUpdateCallback){
        WriteFileMessage message = new WriteFileMessage(mac, serviceUuid, chacUuid, fileByteCount, segmentSize, datasource, otaUpdateCallback);
        message.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        ble.sendMessage(message);
    }

    public void write(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid,@NonNull  byte[] value, int retryWriteCount) {
        ble.write(mac, serviceUuid, chacUuid, value, retryWriteCount);
    }

    public void write(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        write(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount);
    }

    public void write(@NonNull String mac, @NonNull byte[] value) {
        write(mac, value, BleGlobalConfig.rewriteCount);
    }

    public void writeNoRsp(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        writeNoRsp(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount);
    }

    public void writeNoRsp(@NonNull String mac, @NonNull byte[] value) {
        verifyDefaultWriteUuid();
        writeNoRsp(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, BleGlobalConfig.rewriteCount);
    }

    public void writeNoRsp(@NonNull String mac,@NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount) {
        ble.writeNoRsp(mac, serviceUuid, chacUuid, value, retryWriteCount);
    }

    public void writeByLock(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount, Function2<Boolean, Integer> writeCallback) {
        ble.writeByLock(mac, serviceUuid, chacUuid, value, retryWriteCount,writeCallback);
    }

    public void writeByLock(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        writeByLock(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount,null);
    }

    public void writeByLock(@NonNull String mac, @NonNull byte[] value) {
        writeByLock(mac, value,BleGlobalConfig.rewriteCount);
    }

    public void writeByLockNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount, Function2<Boolean, Integer> writeCallback) {
        ble.writeByLockNoRsp(mac, serviceUuid, chacUuid, value, retryWriteCount,writeCallback);
    }

    public void writeByLockNoRsp(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        writeByLockNoRsp(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount,null);
    }

    public void writeByLockNoRsp(@NonNull String mac, @NonNull byte[] value) {
        writeByLockNoRsp(mac, value, BleGlobalConfig.rewriteCount);
    }

    public void writeDesc(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull UUID descUuid, @NonNull byte[] value) {
        ble.writeDesc(mac, serviceUuid, chacUuid, descUuid, value);
    }

    public void read(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid) {
        ble.read(mac, serviceUuid, chacUuid);
    }

    public void readDesc(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull UUID descUuid) {
        ble.readDesc(mac, serviceUuid, chacUuid, descUuid);
    }

    public void openNotify(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid) {
        ble.openNotify(mac, serviceUuid, chacUuid);
    }

    public void openNotify(@NonNull String mac) {
        verifyDefaultNotifyUuid();
        ble.openNotify(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    public void cancelNotify(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid) {
        ble.cancelNotify(mac, serviceUuid, chacUuid);
    }

    public void cancelNotify(@NonNull String mac) {
        verifyDefaultNotifyUuid();
        ble.cancelNotify(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    public void readRssi(@NonNull String mac) {
        ble.readRssi(mac);
    }

    public void setMtu(@NonNull String mac, int mtu) {
        ble.setMtu(mac, mtu);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void readPhy(@NonNull String mac) {
        ble.readPhy(mac);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void setPreferredPhy(@NonNull String mac, int txPhy, int rxPhy, int phyOptions) {
        ble.setPreferredPhy(mac, txPhy, rxPhy, phyOptions);
    }

    //interval=12 latency=0
    //发送和接受数据速度很快,耗电量高
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestConnectionToHigh(@NonNull String mac) {
        ble.requestConnectionPriority(mac, BluetoothGatt.CONNECTION_PRIORITY_HIGH);
    }

    //interval=40 latency=0
    //发送和接受数据度一般,耗电量一般
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestConnectionToBalanced(@NonNull String mac) {
        ble.requestConnectionPriority(mac, BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
    }

    //interval=100 latency=2
    //发送和接受数据度较慢,耗电量低
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestConnectionToLowPower(@NonNull String mac) {
        ble.requestConnectionPriority(mac, BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER);
    }

    public void startScan() {
        startScan(BleGlobalConfig.scanTime);
    }

    public void startScan(long time, IScanCallback callback) {
        ble.startScan(time, callback,null);
    }

    public void startScan(long time) {
        ble.startScan(time, null,null);
    }

    public void startScan(long time, IScanCallback callback, WrapScanConfig config) {
        ble.startScan(time, callback,config);
    }

    //android5.0以下版本无法生效
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScanOnlyLollipop(long time, List<ScanFilter> filters, ScanSettings settings, IScanCallback callback) {
        ble.startScanOnlyLollipop(time, filters, settings != null ? settings : new ScanSettings.Builder().build(), callback);
    }

    public void stopScan() {
        ble.stopScan();
    }

    public void addBleMultiPkgsCallback(String mac, UUID chacUuid, IBleMultiPkgsCallback callback) {
        safeRun(()->ble.addBleMultiPkgsCallback(mac, chacUuid, callback));
    }

    public void addBleMultiPkgsCallback(String mac, UUID chacUuid, Lifecycle lifecycle, IBleMultiPkgsCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmBleMultiPkgsCallback(mac, chacUuid, callback));
        safeRun(()->ble.addBleMultiPkgsCallback(mac, chacUuid, callback));
    }

    public void rmBleMultiPkgsCallback(String mac, UUID chacUuid, IBleMultiPkgsCallback callback) {
        safeRun(()->ble.rmBleMultiPkgsCallback(mac, chacUuid, callback));
    }


    public void addNotifyFailCallback(String mac, INotifyFailCallback callback) {
        safeRun(()->ble.addNotifyFailCallback(mac, callback));
    }

    public void addNotifyFailCallback(String mac,Lifecycle lifecycle, INotifyFailCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmNotifyFailCallback(mac, callback));
        safeRun(()->ble.addNotifyFailCallback(mac, callback));
    }


    public void rmNotifyFailCallback(String mac, INotifyFailCallback callback) {
        safeRun(() -> ble.rmNotifyFailCallback(mac, callback));
    }


    public void addNotifyStatusCallback(String mac, INotifyStatusChangedCallback callback) {
        safeRun(() -> ble.addNotifyStatusCallback(mac, callback));
    }

    public void addNotifyStatusCallback(String mac,Lifecycle lifecycle, INotifyStatusChangedCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmNotifyStatusCallback(mac, callback));
        safeRun(() -> ble.addNotifyStatusCallback(mac, callback));
    }


    public void rmNotifyStatusCallback(String mac, INotifyStatusChangedCallback callback) {
        safeRun(() -> ble.rmNotifyStatusCallback(mac, callback));
    }

    public void addBleStatusCallback(IBlueStatusCallback callback) {
        safeRun(() -> ble.addBleStatusCallback(callback));
    }

    public void addBleStatusCallback(Lifecycle lifecycle,IBlueStatusCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmBleStatusCallback(callback));
        safeRun(() -> ble.addBleStatusCallback(callback));
    }

    public void rmBleStatusCallback(IBlueStatusCallback callback) {
        safeRun(() -> ble.rmBleStatusCallback(callback));
    }

    public void addScanStatusCallback(IScanStatusCallback callback) {
        safeRun(() -> ble.addScanStatusCallback(callback));
    }

    public void addScanStatusCallback(Lifecycle lifecycle,IScanStatusCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmScanStatusCallback(callback));
        safeRun(() -> ble.addScanStatusCallback(callback));
    }


    public void rmScanStatusCallback(IScanStatusCallback callback) {
        safeRun(() -> ble.rmScanStatusCallback(callback));
    }

    public void addScanCallback(IScanCallback callback) {
        safeRun(() -> ble.addScanCallback(callback));
    }

    public void addScanCallback(Lifecycle lifecycle,IScanCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmScanCallback(callback));
        safeRun(() -> ble.addScanCallback(callback));
    }


    public void rmScanCallback(IScanCallback callback) {
        safeRun(() -> ble.rmScanCallback(callback));
    }


    public void addScanErrorCallback(IScanErrorCallback callback) {
        safeRun(() -> ble.addScanErrorCallback(callback));
    }

    public void addScanErrorCallback(Lifecycle lifecycle,IScanErrorCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmScanErrorCallback(callback));
        safeRun(() -> ble.addScanErrorCallback(callback));
    }

    public void rmScanErrorCallback(IScanErrorCallback callback) {
        safeRun(() -> ble.rmScanErrorCallback(callback));
    }

    public void addChacChangeCallback(String mac, IChacChangeCallback callback) {
        safeRun(() -> ble.addChacChangeCallback(mac, callback));
    }

    public void addChacChangeCallback(String mac,Lifecycle lifecycle, IChacChangeCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmChacChangeCallback(mac,callback));
        safeRun(() -> ble.addChacChangeCallback(mac, callback));
    }


    public void rmChacChangeCallback(String mac, IChacChangeCallback callback) {
        safeRun(() -> ble.rmChacChangeCallback(mac, callback));
    }


    public void addChacReadCallback(String mac, IChacReadCallback callback) {
        safeRun(() -> ble.addChacReadCallback(mac, callback));
    }

    public void addChacReadCallback(String mac,Lifecycle lifecycle, IChacReadCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmChacReadCallback(mac,callback));
        safeRun(() -> ble.addChacReadCallback(mac, callback));
    }

    public void rmChacReadCallback(String mac, IChacReadCallback callback) {
        safeRun(() -> ble.rmChacReadCallback(mac, callback));
    }

    public void addChacWriteCallback(String mac, IChacWriteCallback callback) {
        safeRun(() -> ble.addChacWriteCallback(mac, callback));
    }

    public void addChacWriteCallback(String mac,Lifecycle lifecycle, IChacWriteCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmChacWriteCallback(mac,callback));
        safeRun(() -> ble.addChacWriteCallback(mac, callback));
    }


    public void rmChacWriteCallback(String mac, IChacWriteCallback callback) {
        safeRun(() -> ble.rmChacWriteCallback(mac, callback));
    }

    public void addConnectionUpdatedCallback(String mac, IConnectionUpdatedCallback callback) {
        safeRun(() -> ble.addConnectionUpdatedCallback(mac, callback));
    }

    public void addConnectionUpdatedCallback(String mac,Lifecycle lifecycle, IConnectionUpdatedCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmConnectionUpdatedCallback(mac,callback));
        safeRun(() -> ble.addConnectionUpdatedCallback(mac, callback));
    }


    public void rmConnectionUpdatedCallback(String mac, IConnectionUpdatedCallback callback) {
        safeRun(() -> ble.rmConnectionUpdatedCallback(mac, callback));
    }

    public void addConnectStatusChangeCallback(String mac, IConnectStatusChangeCallback callback) {
        safeRun(() -> ble.addConnectStatusChangeCallback(mac, callback));
    }

    public void addConnectStatusChangeCallback(String mac,Lifecycle lifecycle, IConnectStatusChangeCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmConnectStatusChangeCallback(mac,callback));
        safeRun(() -> ble.addConnectStatusChangeCallback(mac, callback));
    }


    public void rmConnectStatusChangeCallback(String mac, IConnectStatusChangeCallback callback) {
        safeRun(() -> ble.rmConnectStatusChangeCallback(mac, callback));
    }


    public void addDescReadCallback(String mac, IDescReadCallback callback) {
        safeRun(() -> ble.addDescReadCallback(mac, callback));
    }

    public void addDescReadCallback(String mac,Lifecycle lifecycle, IDescReadCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmDescReadCallback(mac,callback));
        safeRun(() -> ble.addDescReadCallback(mac, callback));
    }


    public void rmDescReadCallback(String mac, IDescReadCallback callback) {
        safeRun(() -> ble.rmDescReadCallback(mac, callback));
    }


    public void addDescWriteCallback(String mac, IDescWriteCallback callback) {
        safeRun(() -> ble.addDescWriteCallback(mac, callback));
    }

    public void addDescWriteCallback(String mac,Lifecycle lifecycle, IDescWriteCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmDescWriteCallback(mac,callback));
        safeRun(() -> ble.addDescWriteCallback(mac, callback));
    }


    public void rmDescWriteCallback(String mac, IDescWriteCallback callback) {
        safeRun(() -> ble.rmDescWriteCallback(mac, callback));
    }


    public void addMtuChangeCallback(String mac, IMtuChangeCallback callback) {
        safeRun(() -> ble.addMtuChangeCallback(mac, callback));
    }

    public void addMtuChangeCallback(String mac,Lifecycle lifecycle, IMtuChangeCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmMtuChangeCallback(mac,callback));
        safeRun(() -> ble.addMtuChangeCallback(mac, callback));
    }


    public void rmMtuChangeCallback(String mac, IMtuChangeCallback callback) {
        safeRun(() -> ble.rmMtuChangeCallback(mac, callback));
    }


    public void addReadRssiCallback(String mac, IReadRssiCallback callback) {
        safeRun(() -> ble.addReadRssiCallback(mac, callback));
    }

    public void addReadRssiCallback(String mac,Lifecycle lifecycle, IReadRssiCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmReadRssiCallback(mac,callback));
        safeRun(() -> ble.addReadRssiCallback(mac, callback));
    }

    public void rmReadRssiCallback(String mac, IReadRssiCallback callback) {
        safeRun(() -> ble.rmReadRssiCallback(mac, callback));
    }

    public void addServicesDiscoveredCallback(String mac, IServicesDiscoveredCallback callback) {
        safeRun(() -> ble.addServicesDiscoveredCallback(mac, callback));
    }

    public void addServicesDiscoveredCallback(String mac,Lifecycle lifecycle, IServicesDiscoveredCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmServicesDiscoveredCallback(mac,callback));
        safeRun(() -> ble.addServicesDiscoveredCallback(mac, callback));
    }


    public void rmServicesDiscoveredCallback(String mac, IServicesDiscoveredCallback callback) {
        safeRun(() -> ble.rmServicesDiscoveredCallback(mac, callback));
    }

    public void clear() {
        safeRun(() -> ble.clear());
    }

    public void removeBleCallback(String mac) {
        safeRun(() -> ble.removeBleCallback(mac));
    }

    public void addPhyUpdateCallback(String mac, IPhyUpdateCallback callback) {
        safeRun(()->ble.addPhyUpdateCallback(mac, callback));
    }

    public void addPhyUpdateCallback(String mac,Lifecycle lifecycle, IPhyUpdateCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmPhyUpdateCallback(mac,callback));
        safeRun(()->ble.addPhyUpdateCallback(mac, callback));
    }

    public void rmPhyUpdateCallback(String mac, IPhyUpdateCallback callback) {
        safeRun(()->ble.rmPhyUpdateCallback(mac, callback));
    }

    public void addPhyReadCallback(String mac, IPhyReadCallback callback) {
        safeRun(()->ble.addPhyReadCallback(mac, callback));
    }

    public void addPhyReadCallback(String mac,Lifecycle lifecycle, IPhyReadCallback callback) {
        lifecycle.addObserver((DestroyLifecycleObserver) () ->rmPhyReadCallback(mac,callback));
        safeRun(()->ble.addPhyReadCallback(mac, callback));
    }

    public void rmPhyReadCallback(String mac, IPhyReadCallback callback) {
        safeRun(()->ble.rmPhyReadCallback(mac, callback));
    }

    void sendMessage(AbsMessage message) {
        if (message instanceof IFrontMessage) throw new IllegalArgumentException("不能发送IOrderMessage的实现");
        ble.sendMessage(message);
    }

    void sendMessageByDelay(AbsMessage message,long delay){
        if (message instanceof IFrontMessage) throw new IllegalArgumentException("不能发送IOrderMessage的实现");
        ble.sendMessageByDelay(message,delay);
    }

    private void safeRun(Runnable runnable){
        sendMessage(new AbsMessage() {
            public void onHandlerMessage() {
                runnable.run();
            }
        });
    }

    void verifyDefaultWriteUuid(){
        if (BleGlobalConfig.serviceUUID==null||BleGlobalConfig.writeUUID==null){
            throw new IllegalStateException("default serviceUuid or writeUuid not set");
        }
    }

    void verifyDefaultNotifyUuid(){
        if (BleGlobalConfig.serviceUUID==null||BleGlobalConfig.notifyUUID==null){
            throw new IllegalStateException("default serviceUuid or notifyUuid not set");
        }
    }

    public boolean isSupportWriteProperty(@NonNull BluetoothGattCharacteristic characteristic){
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    public boolean isSupportReadProperty(@NonNull BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties()&BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    public boolean isSupportNotifyProperty(@NonNull BluetoothGattCharacteristic characteristic){
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    public boolean isSupportWritePermission(@NonNull BluetoothGattDescriptor descriptor){
        return (descriptor.getPermissions() & BluetoothGattDescriptor.PERMISSION_READ) != 0;
    }

    public boolean isSupportReadPermission(@NonNull BluetoothGattDescriptor descriptor) {
        return (descriptor.getPermissions()&BluetoothGattDescriptor.PERMISSION_WRITE) != 0;
    }

}
