package com.zqs.ble.core.api;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *   @author zhangqisheng
 *   @date 2022-07-16
 *   @description
 */
public abstract class BaseMultiPackageAssembly implements IMultiPackageAssembly {

    private Map<Integer, byte[]> allValue = new HashMap<>();
    private int maxPkgIndex = 0;

    @Override
    public void onChanged(BluetoothGattCharacteristic chac, byte[] value) {
        if (!verifyPkg(value)){
            return;
        }
        int index = getPkgIndex(value);
        allValue.put(index, value);
        if (isLastPkg(value)){
            maxPkgIndex = index;
            try {
                verifyPkgs(index);
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    private boolean verifyPkgs(int lastIndex){
        for (int i=0;i<lastIndex+1;i++){
            if (allValue.get(i)==null){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasNext(byte[] value) {
        return !isLastPkg(value);
    }

    @Override
    public List<byte[]> getResult() {
        ArrayList datas = new ArrayList();
        for (int i = 0; i <= maxPkgIndex; i++) {
            datas.add(allValue.get(i));
        }
        return datas;
    }

    public boolean verifyPkg(byte[] value){
        //默认对包不做验证
        return true;
    }
    public abstract void onError(Exception e);
    //是否是最后一包
    public abstract boolean isLastPkg(byte[] value);
    //从0开始,例如第一个返回的包它的index就是0
    public abstract int getPkgIndex(byte[] value);

}
