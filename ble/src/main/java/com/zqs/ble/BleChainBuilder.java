package com.zqs.ble;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.deamon.message.order.FrontMessage;
import com.zqs.ble.core.utils.fun.VoidFunction;
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

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 QsBle.requestPermission().startScan(mac:String).connect().openNotify(nUuid:UUID).write(cUuid:UUID).await()
 */
public abstract class BleChainBuilder<T extends BleChainBuilder,C extends BleChain,D> {

    protected Queue<BleChainBuilder> chains;

    protected String mac;

    public BleChainBuilder(Queue<BleChainBuilder> chains){
        this.chains = chains;
    }

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

    public T withMac(String mac){
        setMac(mac);
        return (T) this;
    }

    public T delay(long delay) {
        getBleChain().setDelay(delay);
        return (T) this;
    }

    public T retry(int retry){
        getBleChain().setRetry(retry);
        return (T) this;
    }

    public T timeout(long timeout) {
        getBleChain().setTimeout(timeout);
        return (T) this;
    }

    public T async(){
        getBleChain().setAsync(true);
        return (T) this;
    }

    public T dump(boolean dump){
        getBleChain().setDump(dump);
        return (T) this;
    }

    public T before(boolean isRunOnMain,@NonNull Runnable before){
        getBleChain().beforeIsRunMain = isRunOnMain;
        getBleChain().setBeforeCallback(before);
        return (T) this;
    }

    public T before(@NonNull Runnable before){
        return before(false, before);
    }

    public T after(boolean isRunOnMain,@NonNull Runnable after){
        getBleChain().afterIsRunMain = isRunOnMain;
        getBleChain().setAfterCallback(after);
        return (T) this;
    }

    public T after(@NonNull Runnable after){
        return after(false, after);
    }

    public T data(boolean isRunOnMain,@NonNull VoidFunction<D> acceptData){
        getBleChain().acceptDataIsRunMain=isRunOnMain;
        getBleChain().setAcceptDataCallback(acceptData);
        return (T) this;
    }

    public T data(@NonNull VoidFunction<D> acceptData){
        return data(false, acceptData);
    }

    public T error(boolean isRunOnMain,@NonNull VoidFunction<Exception> error){
        getBleChain().errorIsRunMain = isRunOnMain;
        getBleChain().errorCallback = error;
        return (T) this;
    }

    public T error(@NonNull VoidFunction<Exception> error){
        return error(false, error);
    }

    public StartScanChainBuilder startScan(){
        return startScan(null);
    }

    public StartScanChainBuilder startScan(String mac){
        StartScanChainBuilder builder = new StartScanChainBuilder(chains);
        builder.setMac(mac);
        chains.add(builder);
        return builder;
    }

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

    public void start(ChainMessage.ChainHandleStatusCallback handleStatusCallback){
        ChainMessage.ChainHandleOption option = prepare();
        option.setHandleStatusCallback(handleStatusCallback);
        option.start();
    }

    public void start() {
        start(null);
    }

    public BleChain build(){
        throw new IllegalStateException();
    }

}
