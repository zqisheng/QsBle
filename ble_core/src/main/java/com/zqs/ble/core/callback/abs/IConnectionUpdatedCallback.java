package com.zqs.ble.core.callback.abs;

import android.bluetooth.BluetoothDevice;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description 连接参数更新回调,包括设备的连接间隔，连接时延，连接事件的参数改变，部分android手机支持回调，有些不支持
 */
@FunctionalInterface
public interface IConnectionUpdatedCallback extends IBleOptionCallback {

    void onConnectionUpdated(BluetoothDevice device, int interval, int latency, int timeout, int status);

}
