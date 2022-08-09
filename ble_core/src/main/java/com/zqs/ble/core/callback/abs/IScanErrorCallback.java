package com.zqs.ble.core.callback.abs;

/*
 *   @author zhangqisheng
 *   @date 2022-07-14
 *   @description
 */
@FunctionalInterface
public interface IScanErrorCallback {

    void onScanError(int errorCode);

}
