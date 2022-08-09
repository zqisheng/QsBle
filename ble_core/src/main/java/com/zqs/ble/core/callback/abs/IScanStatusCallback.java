package com.zqs.ble.core.callback.abs;

/*
 *   @author zhangqisheng
 *   @date 2022-02-22
 *   @description
 */
@FunctionalInterface
public interface IScanStatusCallback extends IBleOptionCallback {

    void onScanStatusChanged(boolean isScanning);

}
