package com.zqs.ble.message.ota;

/*
 *   @author zhangqisheng
 *   @date 2022-08-08
 *   @description
 */
public interface IOtaUpdateCallback {
    /**
     * 发送文件开始
     */
    void onStart();

    /**
     * 发送文件成功
     */
    void onSuccess();

    /**
     * 发送文件错误
     * @param e
     */
    void onError(Exception e);

    /**
     * 发送文件的进度
     * @param progress
     */
    void onProgress(int progress);

}
