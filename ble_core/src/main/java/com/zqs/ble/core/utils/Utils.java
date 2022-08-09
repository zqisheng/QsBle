package com.zqs.ble.core.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.Arrays;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-14
 *   @description
 */
public class Utils {

    public static String bytesToHexStr(byte[] bytes){
        if (bytes==null) return null;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i<bytes.length; i++) {
            b.append(String.format("%02x", bytes[i] & 0xFF));
        }
        return b.toString();
    }

    public static byte[] hexStrToBytes(String hexStr){
        if (hexStr == null) {
            return null;
        }
        if (hexStr.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[hexStr.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = hexStr.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static byte[] expandBytes(byte[] value,byte fillByte,int size){
        if (value.length%size==0){
            return value;
        }
        int len = size;
        if (value.length>size){
            len=value.length+size-value.length%size;
        }
        byte[] temp = new byte[len];
        Arrays.fill(temp,fillByte);
        System.arraycopy(value,0,temp,0,value.length);
        return temp;
    }

    public static boolean uuidIsSame(UUID u1,UUID u2){
        if (u1==null||u2==null) return false;
        return u1.toString().equals(u2.toString());
    }

    public static boolean uuidIsSame(BluetoothGattService service, UUID serviceUuid){
        return uuidIsSame(service.getUuid(), serviceUuid);
    }

    public static boolean uuidIsSame(BluetoothGattCharacteristic characteristic,UUID serviceUuid,UUID chacUuid){
        return uuidIsSame(characteristic.getUuid(), chacUuid) && uuidIsSame(characteristic.getService().getUuid(), serviceUuid);
    }

    public static boolean uuidIsSame(BluetoothGattDescriptor descriptor,UUID serviceUuid,UUID chacUuid,UUID descUuid){
        return uuidIsSame(descriptor.getUuid(), descUuid) && uuidIsSame(descriptor.getCharacteristic().getUuid(), chacUuid) && uuidIsSame(descriptor.getCharacteristic().getService().getUuid(), serviceUuid);
    }

}
