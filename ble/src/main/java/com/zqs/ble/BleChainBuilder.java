package com.zqs.ble;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.utils.fun.VoidFunction;
import com.zqs.ble.lifecycle.DestroyLifecycleObserver;
import com.zqs.ble.message.builder.CancelNotifyChainBuilder;
import com.zqs.ble.message.builder.ConnectChainBuilder;
import com.zqs.ble.message.builder.DisconnectChainBuilder;
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
import com.zqs.ble.message.builder.WriteByLockChacChainBuilder;
import com.zqs.ble.message.builder.WriteByLockNoRspChacChainBuilder;
import com.zqs.ble.message.builder.WriteChacChainBuilder;
import com.zqs.ble.message.builder.WriteDescChainBuilder;
import com.zqs.ble.message.builder.WriteNoRspChacChainBuilder;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 QsBle.requestPermission().startScan(mac:String).connect().openNotify(nUuid:UUID).write(cUuid:UUID).await()
 */
public abstract class BleChainBuilder<T extends BleChainBuilder,C extends BleChain,D> {

    protected Queue<BleChainBuilder> chains;

    protected String mac;

    public BleChainBuilder(String mac, Queue<BleChainBuilder> chains){
        verifyMac(mac);
        this.mac = mac;
        this.chains = chains;
    }

    protected void verifyMac(String mac){
        if (!BluetoothAdapter.checkBluetoothAddress(mac)){
            throw new IllegalArgumentException("please input correct mac");
        }
    }

    protected void setMac(String mac) {
        verifyMac(mac);
        this.mac = mac;
    }

    public abstract C getBleChain();

    /**
     * 切换下游及当前链操作的设备mac地址
     * @param mac
     * @return
     */
    public T withMac(String mac){
        setMac(mac);
        return (T) this;
    }

    /**
     * 当前链延迟多少ms执行
     * @param delay
     * @return
     */
    public T delay(long delay) {
        getBleChain().setDelay(delay);
        return (T) this;
    }

    /**
     * 当前链失败重写执行的次数
     * @param retry
     * @return
     */
    public T retry(int retry){
        getBleChain().setRetry(retry);
        return (T) this;
    }

    /**
     * 当前链一个重试周期内最大的执行时间,超过时间,这条链直接判断执行失败
     * @param timeout
     * @return
     */
    public T timeout(long timeout) {
        getBleChain().setTimeout(timeout);
        return (T) this;
    }

    /**
     * 当前链将异步执行,立即返回成功状态
     * @return
     */
    public T async(){
        getBleChain().setAsync(true);
        return (T) this;
    }

    /**
     * 当前链执行不成功是否中断整条链的执行
     * @param dump
     * @return
     */
    public T dump(boolean dump){
        getBleChain().setDump(dump);
        return (T) this;
    }

    /**
     * 这条链执行之前回调
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param before
     * @return
     */
    public T before(boolean isRunOnMain,@NonNull Runnable before){
        getBleChain().beforeIsRunMain = isRunOnMain;
        getBleChain().setBeforeCallback(before);
        return (T) this;
    }

    public T before(@NonNull Runnable before){
        return before(false, before);
    }

    /**
     * 这条链在执行下一条链之前会执行
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param after
     * @return
     */
    public T after(boolean isRunOnMain,@NonNull Runnable after){
        getBleChain().afterIsRunMain = isRunOnMain;
        getBleChain().setAfterCallback(after);
        return (T) this;
    }

    public T after(@NonNull Runnable after){
        return after(false, after);
    }

    /**
     * 这条链执行成功,并且获得了结果,该值不会为null
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param acceptData
     * @return
     */
    public T data(boolean isRunOnMain,@NonNull VoidFunction<D> acceptData){
        getBleChain().acceptDataIsRunMain=isRunOnMain;
        getBleChain().setAcceptDataCallback(acceptData);
        return (T) this;
    }

    public T data(@NonNull VoidFunction<D> acceptData){
        return data(false, acceptData);
    }

    /**
     * 当前链执行错误会回调
     * @param isRunOnMain true:回调在主线程  false:回调在蓝牙线程
     * @param error
     * @return
     */
    public T error(boolean isRunOnMain,@NonNull VoidFunction<Exception> error){
        getBleChain().errorIsRunMain = isRunOnMain;
        getBleChain().errorCallback = error;
        return (T) this;
    }

    public T error(@NonNull VoidFunction<Exception> error){
        return error(false, error);
    }

    /**
     * 开始扫描
     * 必须要传入mac,从上游传入或者显示指定mac地址
     * 当扫到指定的设备时,会判定成功,执行下一条链,没调用一次,当发现正在扫描时,会先停止扫描,再开始扫描,所以注意超过一分钟最大开始停止扫描调用次数
     * @return
     */
    public StartScanChainBuilder startScan(){
        return startScan(mac);
    }

    public StartScanChainBuilder startScan(String mac){
        StartScanChainBuilder builder = new StartScanChainBuilder(mac,chains);
        builder.setMac(mac);
        chains.add(builder);
        return builder;
    }

    /**
     * 停止扫描
     * @return
     */
    public StopScanChainBuilder stopScan(){
        StopScanChainBuilder builder = new StopScanChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public ConnectChainBuilder connect(){
        return connect(mac);
    }

    public ConnectChainBuilder connect(String mac){
        ConnectChainBuilder builder = new ConnectChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public DisconnectChainBuilder disconnect(){
        return disconnect(mac);
    }

    public DisconnectChainBuilder disconnect(String mac){
        DisconnectChainBuilder builder = new DisconnectChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public OpenNotifyChainBuilder openNotify(){
        QsBle.getInstance().verifyDefaultNotifyUuid();
        return openNotify(BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    public CancelNotifyChainBuilder cancelNotify() {
        QsBle.getInstance().verifyDefaultNotifyUuid();
        return cancelNotify(BleGlobalConfig.serviceUUID, BleGlobalConfig.notifyUUID);
    }

    public OpenNotifyChainBuilder openNotify(UUID serviceUuid,UUID notifyUuid){
        OpenNotifyChainBuilder builder = new OpenNotifyChainBuilder(mac,serviceUuid,notifyUuid,chains);
        chains.add(builder);
        return builder;
    }

    public CancelNotifyChainBuilder cancelNotify(UUID serviceUuid,UUID notifyUuid){
        CancelNotifyChainBuilder builder = new CancelNotifyChainBuilder(mac, serviceUuid,notifyUuid,chains);
        chains.add(builder);
        return builder;
    }

    public WriteChacChainBuilder write(UUID serviceUuid, UUID chacUuid, byte[] value){
        WriteChacChainBuilder builder = new WriteChacChainBuilder(mac, serviceUuid,chacUuid,value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteByLockChacChainBuilder writeByLock(UUID serviceUuid, UUID chacUuid, byte[] value){
        WriteByLockChacChainBuilder builder = new WriteByLockChacChainBuilder(mac, serviceUuid,chacUuid,value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteNoRspChacChainBuilder writeNoRsp(UUID serviceUuid, UUID chacUuid, byte[] value){
        WriteNoRspChacChainBuilder builder = new WriteNoRspChacChainBuilder(mac, serviceUuid, chacUuid, value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteByLockNoRspChacChainBuilder writeByLockNoRsp(UUID serviceUuid, UUID chacUuid, byte[] value){
        WriteByLockNoRspChacChainBuilder builder = new WriteByLockNoRspChacChainBuilder(mac, serviceUuid,chacUuid,value, chains);
        chains.add(builder);
        return builder;
    }

    public WriteChacChainBuilder write(byte[] value){
        QsBle.getInstance().verifyDefaultWriteUuid();
        return write(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID,value);
    }

    public WriteByLockChacChainBuilder writeByLock( byte[] value){
        QsBle.getInstance().verifyDefaultWriteUuid();
        return writeByLock(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID,value);
    }

    public WriteNoRspChacChainBuilder writeNoRsp( byte[] value){
        QsBle.getInstance().verifyDefaultWriteUuid();
        return writeNoRsp(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID,value);
    }

    public WriteByLockNoRspChacChainBuilder writeByLockNoRsp(byte[] value){
        QsBle.getInstance().verifyDefaultWriteUuid();
        return writeByLockNoRsp(BleGlobalConfig.serviceUUID, BleGlobalConfig.writeUUID, value);
    }

    public ReadChacChainBuilder read(UUID serviceUuid, UUID chacUuid){
        ReadChacChainBuilder builder = new ReadChacChainBuilder(mac,serviceUuid,chacUuid, chains);
        chains.add(builder);
        return builder;
    }

    public ReadDescChainBuilder readDesc(UUID serviceUuid, UUID chacUuid, UUID descUuid){
        ReadDescChainBuilder builder = new ReadDescChainBuilder(mac, serviceUuid, chacUuid, descUuid, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ReadPhyChainBuilder readPhy(){
        ReadPhyChainBuilder builder = new ReadPhyChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public ReadRssiChainBuilder readRssi(){
        ReadRssiChainBuilder builder = new ReadRssiChainBuilder(mac, chains);
        chains.add(builder);
        return builder;
    }

    public RequestMtuChainBuilder requestMtu(int mtu){
        RequestMtuChainBuilder builder = new RequestMtuChainBuilder(mac,mtu, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetConnectionPriorityChainBuilder requestConnectionToHigh(){
        SetConnectionPriorityChainBuilder builder = new SetConnectionPriorityChainBuilder(mac,0, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetConnectionPriorityChainBuilder requestConnectionToBalanced(){
        SetConnectionPriorityChainBuilder builder = new SetConnectionPriorityChainBuilder(mac,1, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetConnectionPriorityChainBuilder requestConnectionToLowPower(){
        SetConnectionPriorityChainBuilder builder = new SetConnectionPriorityChainBuilder(mac,2, chains);
        chains.add(builder);
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SetPhyChainBuilder setPreferredPhy(int txPhy, int rxPhy, int phyOptions){
        SetPhyChainBuilder builder = new SetPhyChainBuilder(mac,txPhy,rxPhy,phyOptions, chains);
        chains.add(builder);
        return builder;
    }

    public WriteDescChainBuilder writeDesc(UUID serviceUuid, UUID chacUuid, UUID descUuid, byte[] value){
        WriteDescChainBuilder builder = new WriteDescChainBuilder(mac,serviceUuid,chacUuid,descUuid,value, chains);
        chains.add(builder);
        return builder;
    }

    public Queue<BleChainBuilder> getChains(){
        return chains;
    }

    public ChainMessage.ChainHandleOption prepare(){
        Queue<BaseChain> chainQueue = new LinkedList<>();
        while (!chains.isEmpty()){
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
     * @param lifecycle
     * @param handleStatusCallback 返回的闭包对象,用于对整条链操作,可以手动销毁整条链
     */
    public void start(Lifecycle lifecycle,ChainMessage.ChainHandleStatusCallback handleStatusCallback){
        ChainMessage.ChainHandleOption option = prepare();
        if (lifecycle!=null){
            WeakReference<ChainMessage.ChainHandleOption> weak = new WeakReference<>(option);
            lifecycle.addObserver((DestroyLifecycleObserver) () -> {
                ChainMessage.ChainHandleOption optionNullable = weak.get();
                if (optionNullable!=null){
                    optionNullable.cancel();
                }
            });
        }
        option.setHandleStatusCallback(handleStatusCallback);
        option.start();
    }

    public void start(ChainMessage.ChainHandleStatusCallback handleStatusCallback){
        start(null,handleStatusCallback);
    }

    public void start() {
        start(null);
    }

    /**
     * 由子类实现
     * @return
     */
    public BleChain build(){
        throw new IllegalStateException();
    }

}
