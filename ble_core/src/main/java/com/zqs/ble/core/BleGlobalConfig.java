package com.zqs.ble.core;

import com.zqs.ble.core.callback.scan.SimpleScanConfig;

import java.util.UUID;

/*
 *   @author zhangqisheng
 *   @date 2022-07-29
 *   @description
 */
public class BleGlobalConfig {
    //默认的扫描时间
    public static long scanTime = 20000;//ms
    //默认的写特征值重写次数
    public static int rewriteCount = 3;
    //默认的发现服务失败重新发现的次数
    public static int discoveryServiceFailRetryCount = 3;
    //默认的服务uuid
    public static UUID serviceUUID;
    //默认的写特特征uuid
    public static UUID writeUUID;
    //默认的通知uuid
    public static UUID notifyUUID;
    //默认的连接超时时间
    public static int connectTimeout = 7000;
    //默认的重连次数
    public static int reconnectCount = 0;
    //默认的单个mtu包的写特征值失败重写次数
    public static int singlePkgWriteTimeout = 200;
    //最大的连接数量,当连接的设备超过最大连接设备数时,会按照设备连接时间断开最远的设备
    public static int maxConnectCount = 7;
    //默认的单个mtu包的ota写特征值失败重写次数
    public static int otaSingleRewriteCount = 3;
    //ota发送的段尺寸,比如文件大小1000b,otaSegmentSize=200b,那么每发200个长度就会回调一下progress
    public static int otaSegmentSize = 200;
    //全局的扫描配置
    public static SimpleScanConfig globalScanConfig;

}
