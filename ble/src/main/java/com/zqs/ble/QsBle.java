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
import android.os.Looper;

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
import com.zqs.ble.core.callback.scan.SimpleScanConfig;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;
import com.zqs.ble.core.utils.fun.Function2;
import com.zqs.ble.core.utils.fun.Function3;
import com.zqs.ble.core.utils.fun.IMessageOption;
import com.zqs.ble.impl.DefaultBleCallbackManage;
import com.zqs.ble.impl.DefaultBleMessageSender;
import com.zqs.ble.impl.DefaultBleOption;
import com.zqs.ble.impl.HandleMessageLooper;
import com.zqs.ble.lifecycle.DestroyLifecycleObserver;
import com.zqs.ble.message.ota.IOtaUpdateCallback;
import com.zqs.ble.message.ota.WriteFileMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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

    private Handler mainHandler;

    private QsBle(){}

    public static QsBle getInstance(){
        return INSTANCE;
    }

    private SimpleBle ble;

    /**
     * 最好再Main线程和Application中初始化
     * 初始化不会获得任何用户的隐私信息,对应用商店的审核没有任何影响
     * @param context
     */
    public boolean init(@NonNull Context context){
        return init(context, null);
    }

    /**
     * @param context
     * @param handler 蓝牙操作和回调的线程,为null的话为框架内部实现的线程,默认为null
     * @return
     */
    public boolean init(@NonNull Context context, Handler handler) {
        if (ble!=null) throw new IllegalStateException("QsBle already init");
        if (BluetoothAdapter.getDefaultAdapter()==null) return false;
        this.context = context;
        ble = new SimpleBle();
        mainHandler = new Handler(Looper.getMainLooper());
        IBleMessageSender sender;
        if (handler==null){
            sender = new DefaultBleMessageSender();
        }else{
            sender = new HandleMessageLooper(handler);
        }
        ble.init(context,new DefaultBleOption(sender), new DefaultBleCallbackManage(),sender);
        return true;
    }

    /**
     * 打开debug日志
     * 如果需要关闭具体的日志,可以手动设置
     * {@link com.zqs.ble.core.BleDebugConfig}
     * @param isDebug
     */
    public void setDebug(boolean isDebug){
        BleDebugConfig.isDebug = isDebug;
        BleDebugConfig.isPrintFunStack = isDebug;
        BleDebugConfig.isOpenBleThreadLog = isDebug;
        BleDebugConfig.isOpenScanLog = isDebug;
        BleDebugConfig.isOpenWriteLog = isDebug;
        BleDebugConfig.isOpenGattCallbackLog = isDebug;
    }

    /**
     * 设置组包组装的对象
     * @param mac
     * @param parser
     */
    public void setMultiPackageAssembly(@NonNull String mac, @NonNull IMultiPackageAssembly parser){
        ble.setMultiPackageAssembly(mac, parser);
    }

    /**
     * 当前的mtu
     * @return
     */
    public int getCurrentMtu(){
        return ble.getCurrentMtu();
    }

    /**
     * 是否正在扫描中
     * @return
     */
    public boolean isScaning(){
        return ble.isScaning();
    }

    /**
     * 是否连接
     * @param mac
     * @return
     */
    public boolean isConnect(String mac) {
        return ble.isConnect(mac);
    }

    /**
     * 获得连接设备数
     * android手机最大连接的蓝牙设备是7个,是指整个系统,包括其它应用中连接的,所以这个连接数量需要控制
     * 蓝牙的协议也规定最多连7个外设
     * @return
     */
    public int getConnectCount(){
        return ble.getConnectCount();
    }

    /**
     * 系统蓝牙开关是否打开
     * @return
     */
    public boolean bluetoothEnable(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    /**
     * 打开系统蓝牙,不建议,在我使用的过程中发现部分手机在用户拒绝打开时会出现anr
     */
    public void openBluetooth(){
        BluetoothAdapter.getDefaultAdapter().enable();
    }

    /**
     * 打开系统蓝牙,建议
     * @param activity
     */
    public void openBluetooth(@NonNull Activity activity) {
        if (!bluetoothEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 1);
        }
    }

    /**
     * 关闭系统蓝牙
     */
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

    /**
     * 获得所有连接的设备
     * @return
     */
    @Nullable
    public List<BluetoothDevice> getConnectDevices(){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager!=null){
            return bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        }
        return null;
    }

    /**
     * 获得设备的通知类型,下面三种情况
     * disable:通知没有打开
     * notification:没有确认机制机制的的通知,这种接受的比较快
     * indication:这个由确认机制,但是可能回的比较慢,但是可以确保到达
     * notification和indication都是通知设置成功,这个状态的选择,app端无法主动进行设置,由设备那边设置
     * 框架内部自己判断了设置那一种通知,不需要代码特意选择设置
     * @param mac
     * @param serviceUuid
     * @param notifyUuid
     * @return
     */
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

    /**
     * 通知是否是打开的
     * @param mac
     * @param serviceUuid
     * @param notifyUuid
     * @return
     */
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

    /**
     * 设置非代码断开设备自动重连次数
     * 如果不是app端主动断开,会按照设置的值进行重连,默认是0,不进行重连
     * @param mac
     * @param autoReconnectCount 这个值会覆盖 BleGlobalConfig.autoReconnectCount 的默认设置
     */
    public void setAutoReconnectCount(String mac,int autoReconnectCount){
        sendMessage(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                if (BleDebugConfig.isDebug){
                    BleLog.d(String.format("setAutoReconnectCount mac=%s,autoReconnectCount=%d", mac, autoReconnectCount));
                }
                ble.setAutoReconnectCount(mac, autoReconnectCount);
            }
        });
    }

    /**
     * 清理该设备在操作和回调队列里所有将要执行的回调和操作
     * @param mac
     */
    public void clearAllOption(@NonNull String mac){
        ble.rmMessagesByMac(mac.toUpperCase());
    }

    /**
     * 创建一个链,mac地址必须传入,null或者不合格的mac地址会报错
     * @param mac
     * @return
     */
    public BleChainBuilder chain(@NonNull String mac){
        return new BleChainBuilder(mac.toUpperCase(), new LinkedList<>()) {
            @Override
            public BleChain getBleChain() {
                return null;
            }

            @Override
            public BleChain build() {
                throw new IllegalStateException("valid chain");
            }
        };
    }

    /**
     * 连接设备
     * @param mac
     * @param timeout 连接超时
     * @param reconnectCount 连接失败重连次数
     * @param connectFailCallback 连接失败回调
     */
    public IMessageOption connect(@NonNull String mac, long timeout, int reconnectCount, Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback) {
        if (BleDebugConfig.isDebug){
            BleLog.d(String.format("handle connect mac=%s,timeout=%d,reconnectCount=%d", mac, timeout, reconnectCount));
        }
        if (getConnectCount()>= BleGlobalConfig.maxConnectCount&&!isConnect(mac)){
            ble.handleLruDisconnect();
        }
        return ble.connect(mac, timeout, reconnectCount,connectFailCallback);
    }

    public IMessageOption connect(@NonNull String mac, long timeout, int reconnectCount) {
        return connect(mac, timeout, reconnectCount,null);
    }

    public IMessageOption connect(@NonNull String mac, long timeout) {
        return connect(mac, timeout,BleGlobalConfig.reconnectCount ,null);
    }

    public IMessageOption connect(@NonNull String mac) {
        return connect(mac, BleGlobalConfig.connectTimeout,BleGlobalConfig.reconnectCount ,null);
    }

    /**
     * 断开连接
     * @param mac
     */
    public IMessageOption disconnect(@NonNull String mac) {
        if (BleDebugConfig.isDebug){
            BleLog.d(String.format("hanlde disconnect mac=%s", mac));
        }
        return ble.disconnect(mac);
    }

    @NonNull
    public IMessageOption writeFile(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] fileBytes, @NonNull IOtaUpdateCallback otaUpdateCallback){
        return writeFile(mac, serviceUuid, chacUuid, fileBytes.length,BleGlobalConfig.otaSegmentSize, new ByteArrayInputStream(fileBytes), otaUpdateCallback);
    }

    /**
     * 写一个文件,传入一个File对象
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     * @param file 注意AndroidQ的存储沙箱机制
     * @param otaUpdateCallback
     */
    @Nullable
    public IMessageOption writeFile(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull File file, @NonNull IOtaUpdateCallback otaUpdateCallback){
        try {
            return writeFile(mac, serviceUuid, chacUuid,(int) file.length(),BleGlobalConfig.otaSegmentSize, new FileInputStream(file), otaUpdateCallback);
        } catch (FileNotFoundException e) {
            otaUpdateCallback.onError(e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 传入一个io流,会将io流中的所有字节按顺序依次发送给设备
     * 相较于writeFileNoRsp方式写,这种方式写一个mtu的速度比writeFileNoRsp的速度慢大概3-30倍
     * 我建议所有的写操作都使用NoRsp类型,除非是特别指定必须保证到达的数据
     * 有norsp和没有norsp的区别就像是Udp和Tcp协议的区别,udp速度肯定比tcp高,但是不能保证数据在传输过程的丢失重发
     * norsp-->udp
     * rsp-->tcp
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     * @param fileByteCount
     * @param segmentSize
     * @param datasource
     * @param otaUpdateCallback
     */
    @NonNull
    public IMessageOption writeFile(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, int fileByteCount, int segmentSize, @NonNull InputStream datasource, @NonNull IOtaUpdateCallback otaUpdateCallback){
        WriteFileMessage message = new WriteFileMessage(mac, serviceUuid, chacUuid, fileByteCount, segmentSize, datasource, otaUpdateCallback);
        ble.sendMessage(message);
        WeakReference<WriteFileMessage> weakReference = new WeakReference<>(message);
        return () -> {
            WriteFileMessage msg = weakReference.get();
            if (msg!=null){
                rmMessage(msg);
            }
        };
    }

    @NonNull
    public IMessageOption writeFileNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] fileBytes, @NonNull IOtaUpdateCallback otaUpdateCallback){
        return writeFileNoRsp(mac, serviceUuid, chacUuid, fileBytes.length,BleGlobalConfig.otaSegmentSize, new ByteArrayInputStream(fileBytes), otaUpdateCallback);
    }

    @Nullable
    public IMessageOption writeFileNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull File file, @NonNull IOtaUpdateCallback otaUpdateCallback){
        try {
            return writeFileNoRsp(mac, serviceUuid, chacUuid, (int) file.length(),BleGlobalConfig.otaSegmentSize, new FileInputStream(file), otaUpdateCallback);
        } catch (FileNotFoundException e) {
            otaUpdateCallback.onError(e);
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public IMessageOption writeFileNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, int fileByteCount, int segmentSize, @NonNull InputStream datasource, @NonNull IOtaUpdateCallback otaUpdateCallback){
        WriteFileMessage message = new WriteFileMessage(mac, serviceUuid, chacUuid, fileByteCount, segmentSize, datasource, otaUpdateCallback);
        message.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        ble.sendMessage(message);
        WeakReference<WriteFileMessage> weakReference = new WeakReference<>(message);
        return () -> {
            WriteFileMessage msg = weakReference.get();
            if (msg!=null){
                rmMessage(msg);
            }
        };
    }

    @NonNull
    public IMessageOption write(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid,@NonNull  byte[] value, int retryWriteCount) {
        return ble.write(mac, serviceUuid, chacUuid, value, retryWriteCount);
    }

    @NonNull
    public IMessageOption write(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        return write(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount);
    }

    @NonNull
    public IMessageOption write(@NonNull String mac, @NonNull byte[] value) {
        return write(mac, value, BleGlobalConfig.rewriteCount);
    }

    @NonNull
    public IMessageOption writeNoRsp(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        return writeNoRsp(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount);
    }

    @NonNull
    public IMessageOption writeNoRsp(@NonNull String mac, @NonNull byte[] value) {
        verifyDefaultWriteUuid();
        return writeNoRsp(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, BleGlobalConfig.rewriteCount);
    }

    /**
     * 向一个特征值写数据
     * 有norsp和没有norsp的区别就像是Udp和Tcp协议的区别,udp效率肯定比tcp高,但是速度肯定不上udp
     * norsp-->udp
     * rsp-->tcp
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     * @param value 一个mtu大小的值,可以小于,但是不能大于,小于的话框架会自动补0凑成一个mtu大小的字节数组
     * @param retryWriteCount 一个mtu包的失败重写次数
     */
    @NonNull
    public IMessageOption writeNoRsp(@NonNull String mac,@NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount) {
        return ble.writeNoRsp(mac, serviceUuid, chacUuid, value, retryWriteCount);
    }

    @NonNull
    public IMessageOption writeByLock(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount, Function2<Boolean, Integer> writeCallback) {
        return ble.writeByLock(mac, serviceUuid, chacUuid, value, retryWriteCount,writeCallback);
    }

    @NonNull
    public IMessageOption writeByLock(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        return writeByLock(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount,null);
    }

    @NonNull
    public IMessageOption writeByLock(@NonNull String mac, @NonNull byte[] value) {
        return writeByLock(mac, value,BleGlobalConfig.rewriteCount);
    }

    /**
     * 向一个特征值写数据
     * 有norsp和没有norsp的区别就像是Udp和Tcp协议的区别,udp效率肯定比tcp高,但是速度肯定不上udp
     * norsp-->udp
     * rsp-->tcp
     * @param mac
     * @param value
     * @param serviceUuid
     * @param chacUuid
     * @param value 这个value的大小不限制,内部所有发送的带lock的消息都是进入一个写队列中,一个一个排队发送,收到操作系统发送成功的回调
                    后会继续发送下一个数据包,我建议只要你向该特征值发送的包有大于一个mtu的,都使用带lock的方法,能够避免很多问题,比如撞包,除非你的
                    数据都是不超过一个mtu的,带lock的方法我建议使用带norsp的,发送速度会较快
     * @param retryWriteCount 一个mtu包失败重写次数
     * @param writeCallback 所有数据发送完成或者没有发送完成,都会回调该方法,这个方法主要是发送该数据包的状态回调,该方法能确保被回调
     */
    @NonNull
    public IMessageOption writeByLockNoRsp(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull byte[] value, int retryWriteCount, Function2<Boolean, Integer> writeCallback) {
        return ble.writeByLockNoRsp(mac, serviceUuid, chacUuid, value, retryWriteCount,writeCallback);
    }

    @NonNull
    public IMessageOption writeByLockNoRsp(@NonNull String mac, @NonNull byte[] value, int retryWriteCount) {
        verifyDefaultWriteUuid();
        return writeByLockNoRsp(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value, retryWriteCount,null);
    }

    @NonNull
    public IMessageOption writeByLockNoRsp(@NonNull String mac, @NonNull byte[] value) {
        return writeByLockNoRsp(mac, value, BleGlobalConfig.rewriteCount);
    }

    /**
     * 写描述,你得确定该描述是否属性可写
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     * @param descUuid
     * @param value
     */
    @NonNull
    public IMessageOption writeDesc(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull UUID descUuid, @NonNull byte[] value) {
        return ble.writeDesc(mac, serviceUuid, chacUuid, descUuid, value);
    }

    /**
     * 读特征值,你得确定该特征是否可读
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     */
    @NonNull
    public IMessageOption read(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid) {
        return ble.read(mac, serviceUuid, chacUuid);
    }

    /**
     * 读描述,你得确定该描述是否可读
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     * @param descUuid
     */
    @NonNull
    public IMessageOption readDesc(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid, @NonNull UUID descUuid) {
        return ble.readDesc(mac, serviceUuid, chacUuid, descUuid);
    }

    /**
     * 打开通知
     * 内部会自动判断是打开ENABLE_NOTIFICATION_VALUE还是ENABLE_INDICATION_VALUE类似
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     */
    @NonNull
    public IMessageOption openNotify(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid) {
        return ble.openNotify(mac, serviceUuid, chacUuid);
    }

    @NonNull
    public IMessageOption openNotify(@NonNull String mac) {
        verifyDefaultNotifyUuid();
        return ble.openNotify(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    /**
     * 关闭通知
     * @param mac
     * @param serviceUuid
     * @param chacUuid
     */
    @NonNull
    public IMessageOption cancelNotify(@NonNull String mac, @NonNull UUID serviceUuid, @NonNull UUID chacUuid) {
        return ble.cancelNotify(mac, serviceUuid, chacUuid);
    }

    @NonNull
    public IMessageOption cancelNotify(@NonNull String mac) {
        verifyDefaultNotifyUuid();
        return ble.cancelNotify(mac, BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    /**
     * 读rssi
     * @param mac
     */
    @NonNull
    public IMessageOption readRssi(@NonNull String mac) {
        return ble.readRssi(mac);
    }

    /**
     * 设置mtu,设置时需要注意,有点设置返回的虽然时成功,但是也有可能用这个mtu时无法通信的,这个不光需要
     * 手机软件和硬件的支持还需要设备端软件和硬件的支持
     * 我建议最好统一设置20byte长度,这也是系统默认的
     * @param mac
     * @param mtu
     */
    @NonNull
    public IMessageOption setMtu(@NonNull String mac, int mtu) {
        return ble.setMtu(mac, mtu);
    }

    /**
     * Android5.0开始从软件上支持Ble5.0,但是手机的硬件是否支持Ble5.0要看各个手机的配置,故对Ble5.0的很多操作在很多的手机上是无效的
     * 包括设置物理信道的方式
     * @param mac
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public IMessageOption readPhy(@NonNull String mac) {
        return ble.readPhy(mac);
    }

    /**
     *
     * @param mac
     * @param txPhy 发送的物理信道 {@link android.bluetooth.BluetoothDevice#PHY_LE_1M_MASK} 这条信道相对发的慢一点
     *             or {@link android.bluetooth.BluetoothDevice#PHY_LE_2M_MASK} 这条信道相对发的快一点
     *             or {@link android.bluetooth.BluetoothDevice#PHY_LE_CODED_MASK} 使用ble5.0信道编码的方式,这种要配合设置phyOptions参数,特点是发的远,收的远,Android5.0以下或者不支持ble5.0的硬件不支持设置
     * @param rxPhy 接收的物理信道 {@link android.bluetooth.BluetoothDevice#PHY_LE_1M_MASK} 这条信道相对发的慢一点
     *      *             or {@link android.bluetooth.BluetoothDevice#PHY_LE_2M_MASK} 这条信道相对发的快一点
     *      *             or {@link android.bluetooth.BluetoothDevice#PHY_LE_CODED_MASK} 信道编码方式,Android5.0以下或者不支持ble5.0的硬件不支持设置
     * @param phyOptions {@link android.bluetooth.BluetoothDevice#PHY_OPTION_NO_PREFERRED}  默认的编码方式,android5.0以下和不支持ble5.0的设备
     *                  or {@link android.bluetooth.BluetoothDevice#PHY_OPTION_S2} ble5.0的,收发距离相较于ble4.x,远2倍 功耗较高
                        or {@link android.bluetooth.BluetoothDevice#PHY_OPTION_S8} ble5.0的,收发距离相较于ble4.x,远4倍 功耗特别高
                    注:除了你手机支持ble5.0的,你连接的设备也需要支持ble5.0才能设置成功,所有这条设置对于不是特别了解ble的不需要关注,全部默认就行,因为各个手机各个设备
                        所支持的硬件也是不同的
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public IMessageOption setPreferredPhy(@NonNull String mac, int txPhy, int rxPhy, int phyOptions) {
        return ble.setPreferredPhy(mac, txPhy, rxPhy, phyOptions);
    }

    //参考值:interval=12 latency=0
    //发送和接受数据速度很快,耗电量高
    //{@link BluetoothGatt.CONNECTION_PRIORITY_HIGH}

    /**
     * 连接间隔和设备时延已经超时时间影响着数据的收发速度和手机的功耗
     * @param mac
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public IMessageOption requestConnectionToHigh(@NonNull String mac) {
        return ble.requestConnectionPriority(mac, BluetoothGatt.CONNECTION_PRIORITY_HIGH);
    }

    //参考值:interval=40 latency=0
    //发送和接受数据度一般,耗电量一般
    //{@link BluetoothGatt.CONNECTION_PRIORITY_BALANCED}
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public IMessageOption requestConnectionToBalanced(@NonNull String mac) {
        return ble.requestConnectionPriority(mac, BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
    }

    //参考值:interval=100 latency=2
    //发送和接受数据度较慢,耗电量低
    //{@link BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER}
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public IMessageOption requestConnectionToLowPower(@NonNull String mac) {
        return ble.requestConnectionPriority(mac, BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER);
    }

    @NonNull
    public IMessageOption startScan() {
        return startScan(BleGlobalConfig.scanTime);
    }

    @NonNull
    public IMessageOption startScan(long time, IScanCallback callback) {
        return ble.startScan(time, callback,null);
    }

    @NonNull
    public IMessageOption startScan(long time) {
        return ble.startScan(time, null,null);
    }

    /**
     * 开始扫描
     * @param time 扫描时间
     * @param callback 扫描回调
     * @param config 扫描过滤配置
     */
    @NonNull
    public IMessageOption startScan(long time, IScanCallback callback, SimpleScanConfig config) {
        return ble.startScan(time, callback, config == null ? BleGlobalConfig.globalScanConfig : config);
    }

    //android5.0以下版本无法生效
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public IMessageOption startScanOnlyLollipop(long time, List<ScanFilter> filters, ScanSettings settings, IScanCallback callback) {
        return ble.startScanOnlyLollipop(time, filters, settings != null ? settings : new ScanSettings.Builder().build(), callback);
    }

    @NonNull
    public IMessageOption stopScan() {
        return ble.stopScan();
    }

    /**
     * 组包组装成功回调
     * @param mac
     * @param chacUuid 监听的特征uuid
     * @param callback
     */
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


    /**
     * 通知失败回调
     * @param mac
     * @param callback
     */
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


    /**
     * 通知的开关状态发送改变会回调
     * @param mac
     * @param callback
     */
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

    /**
     * 系统蓝牙的开关状态回调
     * @param callback
     */
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

    /**
     * 扫描开关状态回调
     * @param callback
     */
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

    /**
     * 扫描回调
     * @param callback
     */
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


    /**
     * 扫描错误回调
     * 目前不支持一分扫描开关超过6次的错误回调
     * @param callback
     */
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

    /**
     * 特征值改变回调
     * @param mac
     * @param callback
     */
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

    /**
     * 读特征值成功回调
     * @param mac
     * @param callback
     */
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

    /**
     * 写特征值成功回调
     * @param mac
     * @param callback
     */
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

    /**
     * 连接参数更新回调,部分低版本的android手机不支持此功能
     * @param mac
     * @param callback
     */
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

    /**
     * 设备连接状态改变回调
     * @param mac
     * @param callback
     */
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

    /**
     * 读描述回调
     * @param mac
     * @param callback
     */
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

    /**
     * 写描述成功回调
     * @param mac
     * @param callback
     */
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

    /**
     * mtu改变回调
     * @param mac
     * @param callback
     */
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

    /**
     * 读rssi回调
     * @param mac
     * @param callback
     */
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

    /**
     * 发现服务回调
     * @param mac
     * @param callback
     */
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

    /**
     * 清空所有设置的蓝牙回调
     */
    public void clear() {
        safeRun(() -> ble.clear());
    }

    /**
     * 移除mac的所有监听回调
     * @param mac
     */
    public void removeBleCallback(String mac) {
        safeRun(() -> ble.removeBleCallback(mac));
    }

    /**
     * 物理信道参数更新回调
     * @param mac
     * @param callback
     */
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

    /**
     * 读物理信道成功回调
     * @param mac
     * @param callback
     */
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

    /**
     * 不支持外部使用
     * @param message
     */
    void sendMessage(AbsMessage message) {
        ble.sendMessage(message);
    }

    /**
     * 不支持外部使用
     * @param message
     */
    void rmMessage(AbsMessage message){
        ble.rmMessage(message);
    }

    /**
     * 不支持外部使用
     * @param message
     */
    void sendMessageByDelay(AbsMessage message,long delay){
        ble.sendMessageByDelay(message,delay);
    }


    /**
     * 不支持外部使用
     */
    void sendMessageToMain(Runnable callback){
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            callback.run();
        }else{
            mainHandler.post(callback);
        }

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

    /**
     * 该特征值是否支持write功能
     * @param characteristic
     * @return
     */
    public boolean isSupportWriteProperty(@NonNull BluetoothGattCharacteristic characteristic){
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    /**
     * 该特征值是否支持读功能
     * @param characteristic
     * @return
     */
    public boolean isSupportReadProperty(@NonNull BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties()&BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    /**
     * 该特征是否支持通知功能
     * @param characteristic
     * @return
     */
    public boolean isSupportNotifyProperty(@NonNull BluetoothGattCharacteristic characteristic){
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    /**
     * 该描述权限是否可写
     * @param descriptor
     * @return
     */
    public boolean isSupportWritePermission(@NonNull BluetoothGattDescriptor descriptor){
        return (descriptor.getPermissions() & BluetoothGattDescriptor.PERMISSION_READ) != 0;
    }

    /**
     * 该描述权限是否可读
     * @param descriptor
     * @return
     */
    public boolean isSupportReadPermission(@NonNull BluetoothGattDescriptor descriptor) {
        return (descriptor.getPermissions()&BluetoothGattDescriptor.PERMISSION_WRITE) != 0;
    }

}
