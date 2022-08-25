package com.zqs.ble;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IBleMultiPkgsCallback;
import com.zqs.ble.core.callback.abs.IChacChangeCallback;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;
import com.zqs.ble.core.utils.fun.ReturnFunction;
import com.zqs.ble.core.utils.fun.VoidFunction;
import com.zqs.ble.fun.Function;
import com.zqs.ble.lifecycle.DestroyLifecycleObserver;
import com.zqs.ble.message.builder.CancelNotifyChainBuilder;
import com.zqs.ble.message.builder.ConnectChainBuilder;
import com.zqs.ble.message.builder.DisconnectChainBuilder;
import com.zqs.ble.message.builder.InterruptChainBuilder;
import com.zqs.ble.message.builder.OpenNotifyChainBuilder;
import com.zqs.ble.message.builder.ReadChacChainBuilder;
import com.zqs.ble.message.builder.ReadDescChainBuilder;
import com.zqs.ble.message.builder.ReadPhyChainBuilder;
import com.zqs.ble.message.builder.ReadRssiChainBuilder;
import com.zqs.ble.message.builder.RequestMtuChainBuilder;
import com.zqs.ble.message.builder.SetConnectionPriorityChainBuilder;
import com.zqs.ble.message.builder.SetPhyChainBuilder;
import com.zqs.ble.message.builder.StartScanChainBuilder;
import com.zqs.ble.message.builder.StopScanChainBuilder;
import com.zqs.ble.message.builder.TogetherChainBuilder;
import com.zqs.ble.message.builder.WriteByLockChacChainBuilder;
import com.zqs.ble.message.builder.WriteByLockNoRspChacChainBuilder;
import com.zqs.ble.message.builder.WriteChacChainBuilder;
import com.zqs.ble.message.builder.WriteDescChainBuilder;
import com.zqs.ble.message.builder.WriteNoRspChacChainBuilder;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 QsBle.requestPermission().startScan(mac:String).connect().openNotify(nUuid:UUID).write(cUuid:UUID).await()
 */
public abstract class BleChainBuilder<T extends BleChainBuilder, C extends BleChain, D> {

    protected Queue<BleChainBuilder> chains;

    protected String mac;

    public BleChainBuilder(String mac, Queue<BleChainBuilder> chains) {
        verifyMac(mac);
        this.mac = mac;
        this.chains = chains;
    }

    protected void verifyMac(String mac) {
        if (!BluetoothAdapter.checkBluetoothAddress(mac)) {
            throw new IllegalArgumentException("please input correct mac");
        }
    }

    protected void setMac(String mac) {
        verifyMac(mac);
        this.mac = mac;
    }

    public abstract BaseChain<D> getBaseChain();

    /**
     * 切换下游及当前链操作的设备mac地址
     *
     * @param mac
     * @return
     */
    public T withMac(String mac) {
        setMac(mac);
        return (T) this;
    }

    /**
     * 当前链延迟多少ms执行
     *
     * @param delay
     * @return
     */
    public T delay(long delay) {
        getBaseChain().setDelay(delay);
        return (T) this;
    }

    /**
     * 当前链失败重写执行的次数
     *
     * @param retry
     * @return
     */
    public T retry(int retry) {
        getBaseChain().setRetry(retry);
        return (T) this;
    }

    /**
     * 当前链一个重试周期内最大的执行时间,超过时间,这条链直接判断执行失败
     *
     * @param timeout
     * @return
     */
    public T timeout(long timeout) {
        getBaseChain().setTimeout(timeout);
        return (T) this;
    }

    /**
     * 当前链将异步执行,立即返回成功状态
     *
     * @return
     */
    public T async() {
        getBaseChain().setAsync(true);
        return (T) this;
    }

    /**
     * 当前链执行不成功是否中断整条链的执行
     *
     * @param dump
     * @return
     */
    public T dump(boolean dump) {
        getBaseChain().setDump(dump);
        return (T) this;
    }

    /**
     * 这条链执行之前回调
     *
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param before
     * @return
     */
    public T before(boolean isRunOnMain, @NonNull Runnable before) {
        getBaseChain().beforeIsRunMain = isRunOnMain;
        getBaseChain().setBeforeCallback(before);
        return (T) this;
    }

    public T before(@NonNull Runnable before) {
        return before(false, before);
    }

    /**
     * 这条链在执行下一条链之前会执行
     *
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param after
     * @return
     */
    public T after(boolean isRunOnMain, @NonNull Runnable after) {
        getBaseChain().afterIsRunMain = isRunOnMain;
        getBaseChain().setAfterCallback(after);
        return (T) this;
    }

    public T after(@NonNull Runnable after) {
        return after(false, after);
    }

    /**
     * 这条链执行成功,并且获得了结果,该值不会为null
     *
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param acceptData
     * @return
     */
    public T data(boolean isRunOnMain, @NonNull VoidFunction<D> acceptData) {
        getBaseChain().acceptDataIsRunMain = isRunOnMain;
        getBaseChain().setAcceptDataCallback(acceptData);
        return (T) this;
    }

    public T data(@NonNull VoidFunction<D> acceptData) {
        return data(false, acceptData);
    }

    /**
     * 当前链执行错误会回调
     *
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param error
     * @return
     */
    public T error(boolean isRunOnMain, @NonNull VoidFunction<Exception> error) {
        getBaseChain().errorIsRunMain = isRunOnMain;
        getBaseChain().errorCallback = error;
        return (T) this;
    }

    public T error(@NonNull VoidFunction<Exception> error) {
        return error(false, error);
    }

    /**
     * 开始扫描
     * 必须要传入mac,从上游传入或者显示指定mac地址
     * 当扫到指定的设备时,会判定成功,执行下一条链,没调用一次,当发现正在扫描时,会先停止扫描,再开始扫描,所以注意超过一分钟最大开始停止扫描调用次数
     *
     * @return
     */
    public StartScanChainBuilder startScan() {
        return startScan(mac);
    }

    public StartScanChainBuilder startScan(String mac) {
        StartScanChainBuilder builder = new StartScanChainBuilder(mac, chains);
        builder.setMac(mac);
        chains.add(builder);
        return builder;
    }

    /**
     * 停止扫描
     *
     * @return
     */
    public StopScanChainBuilder stopScan() {
        StopScanChainBuilder builder = new StopScanChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public ConnectChainBuilder connect() {
        return connect(mac);
    }

    /**
     * 连接设备,如果设备之前已经连接,会直接执行下一条链
     * @param mac
     * @return
     */
    public ConnectChainBuilder connect(String mac) {
        ConnectChainBuilder builder = new ConnectChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public DisconnectChainBuilder disconnect() {
        return disconnect(mac);
    }

    public DisconnectChainBuilder disconnect(String mac) {
        DisconnectChainBuilder builder = new DisconnectChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    /**
     * 打开通知,如果本地缓存的通知状态是打开的会直接返回成功,除非调用了refresh操作才会从设备那边更新状态
     * @return
     */
    public OpenNotifyChainBuilder openNotify() {
        QsBle.getInstance().verifyDefaultNotifyUuid();
        return openNotify(BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    /**
     * 关闭通知,如果本地缓存的通知状态是关闭的会直接返回成功,除非调用了refresh操作才会从设备那边更新状态
     * @return
     */
    public CancelNotifyChainBuilder cancelNotify() {
        QsBle.getInstance().verifyDefaultNotifyUuid();
        return cancelNotify(BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    public OpenNotifyChainBuilder openNotify(UUID serviceUuid, UUID notifyUuid) {
        OpenNotifyChainBuilder builder = new OpenNotifyChainBuilder(mac, serviceUuid, notifyUuid, chains);
        chains.add(builder);
        return builder;
    }

    public CancelNotifyChainBuilder cancelNotify(UUID serviceUuid, UUID notifyUuid) {
        CancelNotifyChainBuilder builder = new CancelNotifyChainBuilder(mac, serviceUuid, notifyUuid, chains);
        chains.add(builder);
        return builder;
    }

    public WriteChacChainBuilder write(UUID serviceUuid, UUID chacUuid, byte[] value) {
        WriteChacChainBuilder builder = new WriteChacChainBuilder(mac, serviceUuid, chacUuid, value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteByLockChacChainBuilder writeByLock(UUID serviceUuid, UUID chacUuid, byte[] value) {
        WriteByLockChacChainBuilder builder = new WriteByLockChacChainBuilder(mac, serviceUuid, chacUuid, value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteNoRspChacChainBuilder writeNoRsp(UUID serviceUuid, UUID chacUuid, byte[] value) {
        WriteNoRspChacChainBuilder builder = new WriteNoRspChacChainBuilder(mac, serviceUuid, chacUuid, value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteByLockNoRspChacChainBuilder writeByLockNoRsp(UUID serviceUuid, UUID chacUuid, byte[] value) {
        WriteByLockNoRspChacChainBuilder builder = new WriteByLockNoRspChacChainBuilder(mac, serviceUuid, chacUuid, value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteChacChainBuilder write(byte[] value) {
        QsBle.getInstance().verifyDefaultWriteUuid();
        return write(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value);
    }

    public WriteByLockChacChainBuilder writeByLock(byte[] value) {
        QsBle.getInstance().verifyDefaultWriteUuid();
        return writeByLock(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value);
    }

    public WriteNoRspChacChainBuilder writeNoRsp(byte[] value) {
        QsBle.getInstance().verifyDefaultWriteUuid();
        return writeNoRsp(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value);
    }

    public WriteByLockNoRspChacChainBuilder writeByLockNoRsp(byte[] value) {
        QsBle.getInstance().verifyDefaultWriteUuid();
        return writeByLockNoRsp(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value);
    }

    public ReadChacChainBuilder read(UUID serviceUuid, UUID chacUuid) {
        ReadChacChainBuilder builder = new ReadChacChainBuilder(mac, serviceUuid, chacUuid, chains);
        chains.add(builder);
        return builder;
    }

    public ReadDescChainBuilder readDesc(UUID serviceUuid, UUID chacUuid, UUID descUuid) {
        ReadDescChainBuilder builder = new ReadDescChainBuilder(mac, serviceUuid, chacUuid, descUuid, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ReadPhyChainBuilder readPhy() {
        ReadPhyChainBuilder builder = new ReadPhyChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public ReadRssiChainBuilder readRssi() {
        ReadRssiChainBuilder builder = new ReadRssiChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public RequestMtuChainBuilder requestMtu(int mtu) {
        RequestMtuChainBuilder builder = new RequestMtuChainBuilder(mac, mtu, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetConnectionPriorityChainBuilder requestConnectionToHigh() {
        SetConnectionPriorityChainBuilder builder = new SetConnectionPriorityChainBuilder(mac, 0, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetConnectionPriorityChainBuilder requestConnectionToBalanced() {
        SetConnectionPriorityChainBuilder builder = new SetConnectionPriorityChainBuilder(mac, 1, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetConnectionPriorityChainBuilder requestConnectionToLowPower() {
        SetConnectionPriorityChainBuilder builder = new SetConnectionPriorityChainBuilder(mac, 2, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetPhyChainBuilder setPreferredPhy(int txPhy, int rxPhy, int phyOptions) {
        SetPhyChainBuilder builder = new SetPhyChainBuilder(mac, txPhy, rxPhy, phyOptions, chains);
        chains.add(builder);
        return builder;
    }

    public WriteDescChainBuilder writeDesc(UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value) {
        WriteDescChainBuilder builder = new WriteDescChainBuilder(mac, serviceUuid, chacUuid, descUuid, value, chains);
        chains.add(builder);
        return builder;
    }

    public InterruptChainBuilder<byte[]> acceptNotify(Function<byte[], Boolean> chacChangedCallback) {
        QsBle.getInstance().verifyDefaultNotifyUuid();
        return acceptNotify(BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID, chacChangedCallback);
    }

    /**
     * 接收设备返回的数据,一个mtu大小的
     * @param serviceUuid
     * @param notifyUuid
     * @param chacChangedCallback 返回true表示返回了符合要求的数据,可以执行下一条链,false表示继续等待符合要求的数据
     * @return
     */
    public InterruptChainBuilder<byte[]> acceptNotify(UUID serviceUuid, UUID notifyUuid, Function<byte[], Boolean> chacChangedCallback) {
        return interrupt((option) -> {
            IChacChangeCallback callback = (device, characteristic, value) -> {
                if (Utils.uuidIsSame(characteristic,serviceUuid,notifyUuid)){
                    if (chacChangedCallback.apply(value)){
                        option.next(value);
                    }
                }
            };
            QsBle.getInstance().addChacChangeCallback(mac, callback);
            return () -> QsBle.getInstance().rmChacChangeCallback(mac,callback);
        });
    }

    public InterruptChainBuilder<List<byte[]>> acceptMultiPkgNotify(Function<List<byte[]>, Boolean> chacChangedCallback) {
        QsBle.getInstance().verifyDefaultNotifyUuid();
        return acceptMultiPkgNotify(BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID, chacChangedCallback);
    }

    /**
     * 接收设备返回的数据,超过一个mtu大小的
     * @param serviceUuid
     * @param notifyUuid
     * @param chacChangedCallback 返回true表示返回了符合要求的数据,可以执行下一条链,false表示继续等待符合要求的数据
     * @return
     */
    public InterruptChainBuilder<List<byte[]>> acceptMultiPkgNotify(UUID serviceUuid, UUID notifyUuid, Function<List<byte[]>, Boolean> chacChangedCallback) {
        return interrupt((option) -> {
            IBleMultiPkgsCallback callback = (device, characteristic, value) -> {
                if (Utils.uuidIsSame(characteristic,serviceUuid,notifyUuid)){
                    if (chacChangedCallback.apply(value)){
                        option.next(value);
                    }
                }
            };
            QsBle.getInstance().addBleMultiPkgsCallback(mac,notifyUuid, callback);
            return () -> QsBle.getInstance().rmBleMultiPkgsCallback(mac,notifyUuid,callback);
        });
    }

    public BleChainBuilder together(@NonNull ReturnFunction<BleChainBuilder> createBuilder) {
        return together(createBuilder.create());
    }

    /**
     * 传入的链类似原子性,要么同时成功,要么同时失败,可以对传入的整条链设置操作符,比如retry操作
     * 将一条链的逻辑单独拿出来嵌入到当前链中,可以对这段逻辑进行统一的重试或者超时等
     * 传入的必须是一条新创建的链,传入当前链会报异常
     * @param builder
     * @return
     */
    public TogetherChainBuilder together(@NonNull BleChainBuilder builder) {
        if (isSameRoot(builder)){
            throw new IllegalArgumentException("must be introduced to a new BleChainBuilder object");
        }
        TogetherChainBuilder bu = new TogetherChainBuilder(mac, chains,builder);
        chains.add(bu);
        return bu;
    }

    public <E> InterruptChainBuilder<E> interrupt(Function<InterruptChainBuilder.InterruptOption<E>, Runnable> interrupt) {
        InterruptChainBuilder builder = new InterruptChainBuilder(mac, chains, interrupt);
        chains.add(builder);
        return builder;
    }

    public Queue<BleChainBuilder> getChains() {
        return chains;
    }

    /**
     * @return 返回的闭包对象, 用于对整条链操作, 可以手动销毁整条链
     */
    public ChainMessage.ChainHandleOption prepare() {
        Queue<BaseChain> chainQueue = new LinkedList<>();
        while (!chains.isEmpty()) {
            chainQueue.add(chains.poll().build());
        }
        ChainMessage message = new ChainMessage(chainQueue);
        //返回一个闭包,隐藏内部message实现
        return new ChainMessage.ChainHandleOption() {
            @Override
            public void cancel() {
                message.cancel();
            }

            @Override
            public void setHandleStatusCallback(ChainMessage.ChainHandleStatusCallback handleStatusCallback) {
                message.setHandleStatusCallback(handleStatusCallback);
            }

            @Override
            public void start() {
                QsBle.getInstance().sendMessage(message);
            }
        };
    }

    /**
     * 建议每一条链都绑定一个Lifecycle对象,这样可以有效防止内存泄漏
     * 我使用的几个老式的Ble框架,或多或少的都出现过内存泄漏的问题
     * 最好每一条链都绑定一个Lifecycle对象
     *
     * @param lifecycle
     * @param handleStatusCallback 这条链执行状态回调
     */
    public void start(Lifecycle lifecycle, ChainMessage.ChainHandleStatusCallback handleStatusCallback) {
        ChainMessage.ChainHandleOption option = prepare();
        if (lifecycle != null) {
            WeakReference<ChainMessage.ChainHandleOption> weak = new WeakReference<>(option);
            lifecycle.addObserver((DestroyLifecycleObserver) () -> {
                ChainMessage.ChainHandleOption optionNullable = weak.get();
                if (optionNullable != null) {
                    optionNullable.cancel();
                }
            });
        }
        option.setHandleStatusCallback(handleStatusCallback);
        option.start();
    }

    public void start(ChainMessage.ChainHandleStatusCallback handleStatusCallback) {
        start(null, handleStatusCallback);
    }

    public void start(Lifecycle lifecycle) {
        start(lifecycle, null);
    }

    public void start() {
        start(null, null);
    }

    private boolean isSameRoot(BleChainBuilder builder) {
        return chains.peek() == builder.chains.peek();
    }

    /**
     * 由子类实现
     *
     * @return
     */
    public abstract BleChain build();

    protected static void sendMessage(AbsMessage message){
        QsBle.getInstance().sendMessage(message);
    }

}
