package com.zqs.ble.message.ota;

/*
 *   @author zhangqisheng
 *   @date 2022-08-08
 *   @description
 */
public interface IOtaUpdateCallback {

    void onStart();

    void onSuccess();

    void onError(Exception e);

    void onProgress(int progress);

}
