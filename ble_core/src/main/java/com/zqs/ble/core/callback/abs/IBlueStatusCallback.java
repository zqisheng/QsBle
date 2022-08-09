package com.zqs.ble.core.callback.abs;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IBlueStatusCallback extends IBleOptionCallback {

    void onBluetoothStatusChanged(boolean isOpen);

}
