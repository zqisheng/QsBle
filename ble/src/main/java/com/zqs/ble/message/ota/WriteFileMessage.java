package com.zqs.ble.message.ota;

import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.deamon.message.option.WriteChacLockMessage;
import com.zqs.ble.core.utils.BleLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-08-08
 *   @description
 */
public class WriteFileMessage extends WriteChacLockMessage {
    private InputStream datasource;
    private int fileByteCount;
    private int sendByteCount = 0;
    private int totalSegment;
    private float currentSegmentIndex = 1;
    private IOtaUpdateCallback otaUpdateCallback;
    private IOtaUpdateCallback innerOtaUpdateCallback = new IOtaUpdateCallback() {
        @Override
        public void onStart() {
            BleLog.d("ota开始");
            otaUpdateCallback.onStart();
        }

        @Override
        public void onSuccess() {
            BleLog.d("ota成功");
            try {
                datasource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            otaUpdateCallback.onSuccess();
        }

        @Override
        public void onError(Exception e) {
            setShouldHandle(false);
            BleLog.d(()->{
                if (e==null){
                    return "ota错误";
                }else{
                    return "ota错误:" + e.getMessage();
                }
            });
            try {
                datasource.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            otaUpdateCallback.onError(e);
        }

        @Override
        public void onProgress(int progress) {
            BleLog.d("ota进度:"+progress);
            otaUpdateCallback.onProgress(progress);
        }
    };

    public WriteFileMessage(String mac, UUID serviceUuid, UUID chacUuid,int fileByteCount,int segmentLength, InputStream datasource,IOtaUpdateCallback otaUpdateCallback) {
        super(mac, serviceUuid, chacUuid, new byte[segmentLength]);
        if (segmentLength>fileByteCount){
            throw new IllegalStateException("segmentLength can`t lt fileByteCount");
        }
        setRetryWriteCount(BleGlobalConfig.otaSingleRewriteCount);
        this.fileByteCount = fileByteCount;
        this.datasource = datasource;
        this.otaUpdateCallback = otaUpdateCallback;
        pkgCount = fileByteCount / mtu + (fileByteCount % mtu == 0 ? 0 : 1);
        totalSegment=fileByteCount / segmentLength + (fileByteCount % segmentLength == 0 ? 0 : 1);
        setWriteCallback((isSuccess, status) -> {
            if (isSuccess){
                innerOtaUpdateCallback.onSuccess();
            }else{
                if (status>=0){
                    innerOtaUpdateCallback.onError(new IllegalStateException("write file fail,gatt status is " + status));
                }else if(status==-2){
                    innerOtaUpdateCallback.onError(new IllegalStateException("write file fail,write message live time lt max live time,status is " + status));
                }else{
                    innerOtaUpdateCallback.onError(new IllegalStateException("write file fail,unknown status:" + status));
                }
            }
        });
    }

    @Override
    public void onHandlerMessage() {
        if (lastHandleTime==0){
            innerOtaUpdateCallback.onStart();
            try {
                datasource.read(value);
            } catch (IOException e) {
                innerOtaUpdateCallback.onError(e);
            }
        }
        super.onHandlerMessage();
    }

    @Override
    protected void fillSendPkgData() {
        if (sendPoint < value.length) {
            System.arraycopy(value, sendPoint, sendPkg, 0, mtu);
            sendByteCount += mtu;
            if (sendByteCount>=fileByteCount){
                innerOtaUpdateCallback.onSuccess();
            }
        } else {
            currentSegmentIndex++;
            innerOtaUpdateCallback.onProgress((int) ((currentSegmentIndex / totalSegment) * 100));
            Arrays.fill(value, (byte) 0);
            try {
                if (datasource.available()>0){
                    datasource.read(value);
                    sendPoint = 0;
                    fillSendPkgData();
                }else{
                    innerOtaUpdateCallback.onError(new IllegalStateException());
                }
            } catch (IOException e) {
                innerOtaUpdateCallback.onError(e);
            }
        }
    }
}
