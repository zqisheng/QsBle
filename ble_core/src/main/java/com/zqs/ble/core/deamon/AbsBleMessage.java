package com.zqs.ble.core.deamon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.zqs.ble.core.SimpleBle;
import com.zqs.ble.core.deamon.message.option.IOptionMessage;
import com.zqs.ble.core.utils.fun.BooleanFunction;

import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description
 */
public abstract class AbsBleMessage extends AbsMessage {

    static SimpleBle simpleBle;

    public static void setSimpleBle(SimpleBle simpleBle){
        AbsBleMessage.simpleBle = simpleBle;
    }

    public AbsBleMessage(String mac){
        this.mac = mac.toUpperCase();
    }

    private String mac;

    @Override
    public void verifyMessage() {
        super.verifyMessage();
        if (BluetoothAdapter.checkBluetoothAddress(mac)==false){
            throw new IllegalStateException("MAC address format error");
        }
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public BluetoothGatt getGatt(){
        return getSimpleBle().getGatt(getMac());
    }

    protected BluetoothGattService getGattService(UUID uuid){
        BluetoothGatt gatt = getGatt();
        if (gatt!=null){
            return gatt.getService(uuid);
        }
        return null;
    }

    protected BluetoothGattCharacteristic getGattCharacteristic(UUID serviceUuid,UUID uuid) {
        BluetoothGattService service = getGattService(serviceUuid);
        if (service!=null){
            return service.getCharacteristic(uuid);
        }
        return null;
    }

    protected BluetoothGattDescriptor getGattDescriptor(UUID serviceUuid, UUID chacUuid, UUID uuid) {
        BluetoothGattCharacteristic characteristic = getGattCharacteristic(serviceUuid,chacUuid);
        if (characteristic!=null){
            return characteristic.getDescriptor(uuid);
        }
        return null;
    }

    protected SimpleBle getSimpleBle(){
        return simpleBle;
    }

    @Override
    protected String printLog() {
        return String.format("AbsMessage->%s,%s", this.getClass().getSimpleName(), getMac());
    }

    protected void clearBeforeMessageIf(BooleanFunction<AbsMessage> condition){
        getSimpleBle().clearMessageIf(condition,null);
    }

    protected void clearBeforeOption(){
        long time = System.currentTimeMillis();
        getSimpleBle().clearMessageIf((msg) -> msg instanceof IOptionMessage && ((AbsBleMessage) msg).getMac().equals(getMac()) && msg.getAddQueueTime() < time, null);
    }

    @Override
    public void assertCurrentIsSenderThread() {
        getSimpleBle().assertCurrentIsSenderThread();
    }
}
