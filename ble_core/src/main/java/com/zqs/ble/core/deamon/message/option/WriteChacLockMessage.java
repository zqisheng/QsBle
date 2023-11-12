package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.abs.IChacWriteCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;
import com.zqs.ble.core.utils.fun.Function1;
import com.zqs.ble.core.utils.fun.Function2;

import java.util.Arrays;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public class WriteChacLockMessage extends AbsBleMessage implements IOptionMessage {

    public static int ON_CREATE = 0;
    public static int ON_DESTROY = 1;

    private UUID serviceUuid;
    private UUID chacUuid;
    protected byte[] value;

    protected byte[] sendPkg;
    private int retryWriteCount = 0;
    //当前sendPkg剩余重新次数
    private int remainRetryWriteCount = 0;
    private int writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    private long singlePkgWriteTimeout = BleGlobalConfig.singlePkgWriteTimeout;
    protected int sendPoint = 0;
    protected int mtu;
    protected int pkgCount;
    private int lastSendSuccessPkgIndex = 0;
    protected long lastHandleTime = 0;
    private boolean shouldHandle = true;
    private int writeCallbackStatus = -1;
    private long maxLiveTime = Long.MAX_VALUE;
    private Function2<Boolean, Integer> writeCallback;
    private Function1<Integer> writeLifecycleCallback;
    private String writeKey;

    public WriteChacLockMessage(String mac, UUID serviceUuid, UUID chacUuid, byte[] value) {
        super(mac);
        this.serviceUuid = serviceUuid;
        this.chacUuid = chacUuid;
        writeKey=getMac() + chacUuid.toString();
        mtu=getSimpleBle().getCurrentMtu(getMac());
        this.value = Utils.expandBytes(value, (byte) 0,mtu);
        pkgCount = value.length / mtu + (value.length % mtu == 0 ? 0 : 1);
    }

    public void setWriteType(int writeType) {
        this.writeType = writeType;
    }

    public void setSinglePkgWriteTimeout(long singlePkgWriteTimeout) {
        this.singlePkgWriteTimeout = singlePkgWriteTimeout;
    }

    public void setWriteLifecycleCallback(Function1<Integer> writeLifecycleCallback) {
        this.writeLifecycleCallback = writeLifecycleCallback;
    }

    public void setWriteCallback(Function2<Boolean, Integer> writeCallback) {
        this.writeCallback = writeCallback;
    }

    public void setRetryWriteCount(int retryWriteCount) {
        this.retryWriteCount = retryWriteCount;
    }

    private IChacWriteCallback chacWriteCallback = (device, chac, value, status) -> {
        if (!Utils.uuidIsSame(chac.getUuid(), chacUuid)) {
            return;
        }
        if (BleDebugConfig.isOpenWriteLog){
            BleLog.d(String.format("write lock chac callback:value=%s,sendPkg=%s", Utils.bytesToHexStr(value), Utils.bytesToHexStr(sendPkg)));
        }
        if (!Arrays.equals(sendPkg, value)) {
            BleLog.d(String.format("Received expired write lock chac callback:value=%s,sendPkg=%s", Utils.bytesToHexStr(value), Utils.bytesToHexStr(sendPkg)));
            return;
        }
        writeCallbackStatus = status;
        if (status == BluetoothGatt.GATT_SUCCESS) {
            ++lastSendSuccessPkgIndex;
            remainRetryWriteCount = retryWriteCount;
            if (lastSendSuccessPkgIndex >= pkgCount) {
                setShouldHandle(false);
                if (writeCallback != null) {
                    writeCallback.onCallback(true, status);
                }
            }else{
                prepareSendPkg();
            }
        } else {
            if (remainRetryWriteCount <= 0) {
                setShouldHandle(false);
                if (writeCallback != null) {
                    writeCallback.onCallback(false, status);
                }
            }
        }
    };

    private void prepareSendPkg() {
        Arrays.fill(sendPkg, (byte) 0);
        fillSendPkgData();
        sendPoint += mtu;
    }

    protected void fillSendPkgData(){
        System.arraycopy(value, sendPoint, sendPkg, 0, mtu);
    }

    @Override
    public void onHandlerMessage() {
        if (!getSimpleBle().isConnect(getMac())){
            BleLog.d(String.format("device is disconnect:%s",getMac()));
            setShouldHandle(false);
            return;
        }
        //第一次回调
        if (lastHandleTime == 0) {
            if (writeLifecycleCallback!=null){
                writeLifecycleCallback.onCallback(ON_CREATE);
            }
            maxLiveTime = System.currentTimeMillis() + pkgCount * singlePkgWriteTimeout;
            remainRetryWriteCount = retryWriteCount;
            getSimpleBle().setLockWriteChacCallback(getMac(), chacWriteCallback);
            sendPkg = new byte[mtu];
            prepareSendPkg();
        }else{
            //可能是gatt write fail或者是超时回调
            if (writeCallbackStatus!=BluetoothGatt.GATT_SUCCESS){
                --remainRetryWriteCount;
            }
        }
        writeCallbackStatus = -1;
        if (remainRetryWriteCount<0){
            setShouldHandle(false);
            if (writeCallback != null) {
                writeCallback.onCallback(false, -1);
            }
            return;
        }
        writeValue(sendPkg);
        lastHandleTime = System.currentTimeMillis();
    }

    private void writeValue(byte[] data) {
        BluetoothGatt gatt = getGatt();
        if (gatt == null) return;
        BluetoothGattCharacteristic characteristic = getGattCharacteristic(serviceUuid, chacUuid);
        if (characteristic != null) {
            if (BleDebugConfig.isOpenWriteLog){
                BleLog.d(String.format("set chac by lock:%s,%s", chacUuid.toString(), Utils.bytesToHexStr(data)));
            }
            characteristic.setWriteType(writeType);
            characteristic.setValue(data);
            boolean result = gatt.writeCharacteristic(characteristic);
            //方法返回层面的重写
            if (!result) {
                while (remainRetryWriteCount > 0 && (result = !gatt.writeCharacteristic(characteristic))) {
                    --remainRetryWriteCount;
                }
                if (remainRetryWriteCount <= 0) {
                    setShouldHandle(false);
                }
            }
        }
    }

    public boolean getShouldHandle() {
        return shouldHandle;
    }

    public void setShouldHandle(boolean shouldHandle) {
        this.shouldHandle = shouldHandle;
        if (!shouldHandle) {
            getSimpleBle().rmLockWriteChacCallback(getMac());
        }
    }

    @Override
    public long getHandleTime() {
        if (writeCallbackStatus >= 0) {
            return 0;
        }
        return lastHandleTime + singlePkgWriteTimeout;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
        pkgCount = value.length / mtu + (value.length % mtu == 0 ? 0 : 1);
    }

    //防止多特征值通知的情况
    public String getWriteKey() {
        return writeKey;
    }

    @Override
    public boolean isLive() {
        if (!getSimpleBle().isConnect(getMac())||System.currentTimeMillis()>maxLiveTime){
            if (writeCallback != null) {
                writeCallback.onCallback(true, -2);
            }
            return false;
        };
        return super.isLive();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (writeLifecycleCallback!=null){
            writeLifecycleCallback.onCallback(ON_DESTROY);
        }
    }
}
