**QsBle is an Android Ble framework**

[中文文档](https://github.com/zqisheng/QsBle/blob/master/README_CN.md)

[English Document](https://github.com/zqisheng/QsBle/blob/master/README.md)

[Click to download demo](https://raw.githubusercontent.com/zqisheng/QsBle/master/screen/app-debug.apk)

**Features**
* Compared with the old Ble framework, it is more in line with the code style of current Android developers
* Fully functional programming, all Ble callbacks are functional
* Supports callbacks for all Ble operations
* The data sent exceeds mtu, and QsBle also has a perfect packet grouping mechanism
* Support disconnection and automatic reconnection
* Supports Kotlin coroutines, allowing you to operate asynchronous code in a synchronous way without worrying about the various callback hells of Bluetooth
* Support Flow
* Supports chain programming, allowing you to perform various operations on Bluetooth sequentially. The logic that could only be implemented in hundreds of lines of code before, QsBle only needs one line of code
* Perfect ota support, you only need to provide your ota file, all other operations are handed over to QsBle
* The perfect exception handling mechanism allows you to locate the problem in time when an exception occurs.
* The core code is written in Java, and people who develop in Java do not need to worry about the inability to use Java, and Kotlin calls Java code also provides various nullability mechanisms, so that you don't have to worry about the null pointer of Kotlin calling Java code
* Other Ble frameworks have some, and QsBle also has them

### Add dependencies
[![](https://jitpack.io/v/zqisheng/QsBle.svg)](https://jitpack.io/#zqisheng/QsBle)
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }


dependencies {
    //Dependencies that QsBle must add
   implementation 'com.github.zqisheng.QsBle:ble:1.2.3
    //If you want to use the kotlin coroutine function, you must add the following dependencies, not required
   //implementation 'com.github.zqisheng.QsBle:ble_coroutines:1.2.3
}
```

### Before use
Adding permissions for Bluetooth scanning needs to be added, and apply for permissions at runtime
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
For the Bluetooth adaptation of Android 12, the following three permissions have been added by default in the framework, so there is no need to add them manually, but these three permissions are dynamically applied for permissions. In Android 12 and above, they must be dynamically applied in the code.
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### Initialization
**Usage 1(Recommend):**
```java
QsBle.getInstance().init(Context context);
```
**Usage 2:**
```java
QsBle.getInstance().init(Context context,Handler handler);
```
*Note 1: All operations and callbacks of QsBle to Ble are executed in one thread. The initialization of mode 1 is to use the thread implemented by the framework to operate Ble by default. But if you want all operations of Ble to be specified by yourself You can also pass in a Handler, so that all Ble operations and callbacks will be called in the specified Handler <br/>For example, if you want all operations to be called back in the main thread, you can pass a The Handler of the main thread, so that all Ble operations and callbacks are called back in the main thread, **but the author strongly does not recommend doing this***

*Note 2: The initialization of QsBle will not obtain any private information of the user's mobile phone, so rest assured to initialize at any time*


### Simple to use
**Usage 1:**
```java
QsBle.getInstance().startScan();
QsBle.getInstance().connect(mac);
QsBle.getInstance().connect(mac);
QsBle.getInstance().openNotify(mac,serviceuuid,notifyUuid);
QsBle.getInstance().writeByLock(mac,serviceUuid,writeUuid,value)
....
```

**Usage 2(Chain)(Java Recommended):**
```java
QsBle.getInstance().chain(mac)
    .connect()...
    .openNotify(serviceUuid,notifyUuid)...
    .requestConnectionToHigh()...
    .writeByLock(serviceUuid,writeUuid,value)...
    .writeByLock(serviceUuid,writeUuid,value)...
    .writeByLock(serviceUuid,writeUuid,value)...
    .disconnect()...
    ...
    .start()
```
*Note: The above chain operation process is: first connect to the device whose address is mac. After the connection is successful, the notify of the device will be opened. After the notify is opened successfully, the Connection parameter will be set. After the parameter is set successfully, data will be written to the feature. , After the framework confirms that the data is written successfully, it will disconnect the connection of the device, and any part of the link will not continue to execute if it does not achieve the expected result*

**Usage 3(Kotlin coroutines)(Kotlin Recommended)**
```kotlin
//Use kotlin coroutine to implement the operation of mode 2
bleLifeScope.launch ({
    val chain = ble.chain(mac)
    //true:connection succeeded
    val isConnect:Boolean=chain.connect().reConnectCount(3).connectTimeout(7000).dump(false).await()
    //Three values: notification, indication, disable, the first two are notifications already turned on
    val notifyStatus1:String?=chain.openNotify(serviceUuid,notifyUuid).dump(false).retry(3).await()
    val notifyStatus2:String?=chain.cancelNotify().await()
    //new int[]{interval, latency, timeout}
    //As long as it is not empty, the setting is successful
    val status1:IntArray?=chain.requestConnectionToLowPower().dump(false).await()
    val status2:IntArray?=chain.requestConnectionToBalanced().dump(false).await()
    val status3:IntArray?=chain.requestConnectionToHigh().dump(false).await()
    //true: write successfully false: write failed
    val status4:Boolean=chain.writeByLock(serviceUuid,writeUuid,value).dump(false).await()
    //true: write successfully false: write failed
    //The speed of norsp is about 3-30 times faster than that without norsp, which is related to the connection parameters of the device
    val status5:Boolean=chain.writeByLockNoRsp(serviceUuid,writeUuid,value).dump(false).await()
    //Returns the value of the characteristic value in the device
    val readValue:ByteArray?=chain.read(serviceUuid,readUuid).dump(false).await()
    //Returns the value of the description in the device
    val readDescValue:ByteArray?=chain.readDesc(serviceUuid,readUuid,descUuid).dump(false).await()
    //new int[]{txPhy,rxPhy}
    val phy:IntArray?=chain.readPhy().await()
    //return rssi
    val rssi:Int?=chain.readRssi().await()
    //Returns the mtu that has been successfully set, and if it is null, the setting fails.
    val mtu:Int?=chain.requestMtu(517).await()
    //Disconnect
    chain.disconnect().await()
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```
*Note: bleLifeScope is an extension object of the Lifecycle object. When the Lifecycle is destroyed, bleLifeScope will automatically interrupt the internal Bluetooth coroutine operation and destroy the coroutine*
### Description of public operators
**The characteristics of these operators are that all chained operations are supported. Using each operator together can solve some very complex scenarios.**

**Operator1:dump(boolean dump)**<br/>
**Explanation:Whether the current chained segment ends the whole execution chain when the execution fails. The default value is true, which ends the whole chain**<br/>
**Chain**:
```java
//Connect 5 devices directly by chain
QsBle.getInstance().chain()
    .connect(mac1)...
    .connect(mac2).dump(false)...
    .connect(mac3).dump(true)...//The default is true, which can not be set
    .connect(mac4)
    .connect(mac5)
    .start()
```
**Explanation:After the connection of mac1 is successful, it will connect to mac2. If the connection of mac2 fails, such as the connection timeout or the system returns an error, will mac3 continue to connect? The answer is whether it will continue to connect. Because the dump of mac2 chain is false, even if the connection fails, it will not interrupt the whole chain. Mac3 will continue to connect after the connection of mac2 fails. If the connection of mac3 fails, it will also fail? Because dump=true, the execution of the whole chain is interrupted directly, and mac4 and mac5 will not perform connection operations later**<br/>
**Coroutine:**
```kotlin
bleLifeScope.launch ({
    //Connect 5 devices directly by chain
    QsBle.getInstance().chain().apply{
        connect(mac1).await()
        connect(mac2).dump(false).await()
        connect(mac3).dump(true).await()//The default is true, which can not be set
        connect(mac4).await()
        connect(mac5).await()
    }
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```
**Explanation:The chain execution process is the same as that above. The only difference in the collaboration process is that when the dump of this chain=true, the connection failure occurs, and an exception will be thrown directly in the collaboration process. Therefore, when the connection to the mac3 fails, the onErro function will be called back. If you want to fail without throwing an exception, the execution device dump=false**<br/>

**Operator2:async())**<br/>
**Explanation:Those who have used the kotlin coroutine should know the role of this operator. The role of this operator is very similar to that of the coroutine. It is used to asynchronously execute the current chain. When executing this chain, the next chain will be returned successfully without waiting for the return result of this chain**<br/>
**Chain**:
```java
//Connect 5 devices simultaneously by chain
QsBle.getInstance().chain()
    .connect(mac1).async()...
    .connect(mac2).async().dump(false)...
    //Since async returns success directly and there is no failure, the value of dump has no effect on the execution of the whole chain
    .connect(mac3).async().dump(true)...//The default is true, which can not be set
    .connect(mac4).async()
    .connect(mac5).async()
    .start()
```
**Coroutine:**
```kotlin
bleLifeScope.launch ({
    //It should be noted that even if you call the async() operator
    //But then I call await()
    //Then the async() operator will become invalid
    /**
     * So the execution process is to wait for the connection of mac1 to succeed
*Of course, an exception will be thrown directly if the connection to the mac1 fails, because dump defaults to true
*Then connect the following four devices at the same time
     * */
    QsBle.getInstance().chain().apply{
        connect(mac1).async().await()
        connect(mac2).async().start()
        connect(mac3).async().start()
        connect(mac4).async().start()
        connect(mac5).async().start()
    }
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```
**Operator3:delay(long delay)**<br/>
**Explanation:The current chain segment will delay the execution of delay ms. Note that only the current chain**<br/>
**Chain**:
```java
QsBle.getInstance().chain()
    .connect(mac1)...
    .connect(mac2).delay(1000).dump(false)...
    .connect(mac3).delay(2000).dump(true)...//The default is true, which can not be set
    .connect(mac4).delay(3000)
    .connect(mac5).delay(4000)
    .start()
```
**Coroutine:**
```kotlin
bleLifeScope.launch ({
    QsBle.getInstance().chain().apply{
        connect(mac1).await()
        connect(mac2).await()
        //This uses the blocking function provided by the kotlin coprogram
        delay(1000)
        //This uses the delay () operator implemented by the framework itself
        //Although their execution results and time are the same, we should distinguish
        connect(mac3).delay(2000).await()
        connect(mac4).await()
        delay(3000)
        connect(mac5).delay(4000).await()
    }
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```

**Operator4:retry(int retry)**<br/>
**Explanation:Current chain segment execution failure and re execution times**<br/>
**Chain**:
```java
QsBle.getInstance().chain()
    .connect(mac1)...
    //The retry operator works on the current chain
    //If all mac2 connections fail, the connection will be rewritten 3 times
    .connect(mac2).retry(3)...
    //Here, the mac3 is reconnected 3 * 3=9 times. The retry operator is a retry of the current chained segment
    .connect(mac3).reConnectCount(3).retry(3)...//The default is true, which can not be set
    .start()
```
**Coroutine:**
```kotlin
bleLifeScope.launch ({
    QsBle.getInstance().chain().apply{
        connect(mac1).await()
        connect(mac2).retry(3).await()
        connect(mac3).reConnectCount(3).retry(3).await()
    }
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```
**Operator5:timeout(long timeout)**<br/>
**Explanation:The maximum execution time of the current chain segment, that is, the timeout**<br/>
**Chain**:
```java
QsBle.getInstance().chain()
    /**
     *If it takes 5000ms and timeout=4000ms to connect the device, the chain will be judged as failed, but the device is connected
     *This chain failed to execute
    **/
    .connect(mac1).connectTimeout(7000).timeout(4000)...
    .start()
```
**Coroutine:**
```kotlin
bleLifeScope.launch ({
    QsBle.getInstance().chain().apply{
        //How long will the tour wait?
        //In the longest case, wait for 4000ms. If the timeout period is exceeded and there is no result, the execution will be judged as failed
        connect(mac1).timeout(4000).retry(3).await()
    }
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```
**Operator6:withMac(String mac)**<br/>
**Explanation:The MAC address of the device corresponding to the current chain segment and the chain executed after it. This parameter is not required except for the scanned chain. Other chains are required, but some are implicit and some are explicit**<br/>

**Chain**:
```java
//Take the following inappropriate example
QsBle.getInstance().chain()
            //If the following comments are removed, an error will be reported directly when the chain is executed here, because there is no default mac incoming
            //.connect()
            .connect(mac1)
            .connect(mac2)
            //The value0 is sent to mac2, because the upstream incoming is mac2
            .writeByLock(serviceUuid,chacUuid,value0).
            //Value1 is sent to mac1
            .writeByLock(serviceUuid,chacUuid,value1).withMac(mac1)
            //Value2 is sent to mac1
            .writeByLock(serviceUuid,chacUuid,value2)
            //Value3 is sent to mac1
            .writeByLock(serviceUuid,chacUuid,value3)
            //Value4 is sent to mac2 because the mac value is switched
            .writeByLock(serviceUuid,chacUuid,value4).withMac(mac2)
            //Value5 is sent to mac2
            .writeByLock(serviceUuid,chacUuid,value5)
            //Connect mac3
            .connect().withMac(mac3)
            //Value6 is sent to mac3
            .writeByLock(serviceUuid,chacUuid,value6)
            //Value7 is sent to mac3
            .writeByLock(serviceUuid,chacUuid,value7)
            //Disconnect mac3, because the upstream incoming is mac3
            .disconnect()
            //Disconnect mac2
            .disconnect(mac2)
            //Disconnect mac3
            .disconnect(mac3)
            .start()
```
**Coroutine:**
```kotlin
Refer to the example provided above
```
### Global default configuration
*Change the default global configuration and set the corresponding value directly*
```java
public class BleGlobalConfig {
    //Default scan time
    public static long scanTime = 20000;//ms
    //Default rewrite times of write characteristic value
    public static int rewriteCount = 3;
    //The number of times the default discovery service fails to rediscover
    public static int discoveryServiceFailRetryCount = 3;
    //Default service uuid
    public static UUID serviceUUID;
    //Default write feature uuid
    public static UUID writeUUID;
    //Default notification uuid
    public static UUID notifyUUID;
    //Default connection timeout
    public static int connectTimeout = 7000;
    //Default reconnection times
    public static int reconnectCount = 0;
    //The default number of times a single mtu packet fails to rewrite its write eigenvalue
    public static int singlePkgWriteTimeout = 200;
    //The maximum number of connections. When the number of connected devices exceeds the maximum number of connected devices, the farthest device will be disconnected according to the device connection time
    public static int maxConnectCount = 7;
    //The default number of times a single mtu package fails to rewrite its ota write eigenvalue
    public static int otaSingleRewriteCount = 3;
    //The segment size sent by ota, such as the file size of 1000b, otaSegmentSize=200b, will be recalled every 200 timesprogress
    public static int otaSegmentSize = 200;
    //Global scan configuration
    public static SimpleScanConfig globalScanConfig;
    //The default number of disconnection and automatic reconnection is 0. No automatic reconnection
    public static int autoReconnectCount = 0;

}
```
### Listen to Ble callback
All functions added in the form of addXXXCallback must be removed by calling the corresponding rmXXXCallback method at an appropriate time, or memory leaks will occur
```java
    IConnectStatusChangeCallback connectStatusChangeCallback = (device, isConnect, status, profileState) -> {
        //Device connection status callback
    };
    IScanCallback scanCallback = (device, rssi, scanRecord) -> {
        //Scan to device callback
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        QsBle.getInstance().addScanCallback(scanCallback);
        QsBle.getInstance().addConnectStatusChangeCallback(mac,connectStatusChangeCallback);
    }
```
When the activity is destroyed, you must remove the callback, or memory leaks or even app flashbacks will occur
```java
    @Override
    protected void onDestroy() {
        QsBle.getInstance().rmScanCallback(scanCallback);
        QsBle.getInstance().rmConnectStatusChangeCallback(mac,connectStatusChangeCallback);
    }
```
QsBle also provides a better way to add callbacks bound to a lifecycle
For example, in activity, some activities implement the interface of the Lifecycle class. You just call the addXXXCallback method and pass in the Lifecycle parameter. When the lifecycle object is destroyed, the callback will be automatically removed
```java
//Kotlin mode
QsBle.getInstance().addConnectStatusChangeCallback(mac,this.lifecycle, IConnectStatusChangeCallback { device, isConnect, status, profileState -> 
        //Connection status callback
 })
QsBle.getInstance().addScanCallback(this.lifecycle, IScanCallback { device, rssi, scanRecord -> 
        //Scan callback
})
//java mode
QsBle.getInstance().addConnectStatusChangeCallback(mac,this.getLifecycle(), (device, isConnect, status, profileState) ->{
        //Connection status callback
});
QsBle.getInstance().addScanCallback(this.getLifecycle(), (device, rssi, scanRecord) ->{
        //Scan callback
});
```

### Subcontracting and packaging beyond Mtu
The default mtu of Android Ble is 20 bytes long. For data larger than 20 bytes, app and device developers need to determine the subcontracting protocol. Generally, the subcontracting protocol divides an mtu data into packet header and data bits. The packet header specifies the serial number of this data and other data. The device receives an mtu to remove the packet header and assemble the data to form a complete packet

**1.Subcontracting**

There are two ways to write eigenvalues in QsBle: one is to write data of one mtu length, and the other is to write data of more than one mtu length

**Method of writing mtu length data:**
```java
void write(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount);
void writeNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount);
```
**A method for writing data longer than one mtu:**
```java
void writeByLock(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallbac);
void writeByLockNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount, Function2<Boolean,Integer> writeCallback);
```

**The following situations should be noted when dealing with these two situations**
<br/>Note: It is better to use one of these two methods in a type of device. * * The author suggests that all write characteristic values use two methods: writeByLock and writeByLockNoRsp * *. The method with Lock refers to that the data written in the device's characteristics will enter the write queue, and subsequent messages will be sent after receiving a successful write callback from the system or triggering a timeout, Since the methods without lock are sent directly, there is no need to queue. If you send a long data, and the long data is not sent all at once, and then you call the write method to send a mtu data, packet collision will occur on the device side, causing the long data to be unrecognized on the device side, unless your packet header contains the flag bit of the long data or your data does not need to be subcontracted
So the author strongly suggests<br/>**All write eigenvalues use two methods: writeByLock and writeByLockNoRsp**

**2.Packaging**

QsBle also provides an interface for packaging when the device side responds to messages that are longer than one mtu
You only need to implement the abstract class BaseMultiPackageAssembly<br/>Here's an example
```kotlin
class MyMulitPkgParser :
    BaseMultiPackageAssembly(){

    override fun onChanged(chac: BluetoothGattCharacteristic?, value: ByteArray?) {
        super.onChanged(chac, value)
    }

    override fun onError(e: Exception?) {
    }

    override fun isLastPkg(value: ByteArray): Boolean {
        //Determine whether the returned mtu data is the last package to determine whether the assembly is successful
        //If true is returned, all data are accepted
        //If false is returned, it indicates that there is another package and the assembly cannot be determined
    }

    override fun getPkgIndex(value: ByteArray): Int {
        //Obtain the package serial number of the whole data from the returned mtu data
    }
}
```
After implementing the package class, you need to call
```kotlin
QsBle.getInstance().setMultiPackageAssembly(mac, MyMulitPkgParser())
```
Set the global package parser for this device
After the settings are successful, you only need to add a package callback listener when you need to obtain data to obtain the successfully assembled package
```kotlin
ble.addBleMultiPkgsCallback(mac,MainActivity.nUuid, IBleMultiPkgsCallback { device, characteristic, result ->
    //All packages will be called back after successful packaging
    //The result is a List<byte []>type, which contains all the results after assembly
})
```
*Note: This also requires calling rmBleMultiPkgsCallback when appropriate to remove the callback*


### Ota Upgrade
For app developers, Ota is how to transfer a file to the device. QsBle provides complete Ota upgrade support
**Usage 1:**
Call QsBle's writeFile method:
```java
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] fileBytes, IOtaUpdateCallback otaUpdateCallback);
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid, UUID chacUuid, File file, IOtaUpdateCallback otaUpdateCallback)
//All the methods above encapsulate the writeFileNoRsp method below. QsBle only focuses on the IO stream you send in
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid,UUID chacUuid, int fileByteCount, int segmentLength,InputStream datasource,IOtaUpdateCallback otaUpdateCallback);
```
Parameter Description:
fileByteCount:Total byte length of ota file to be sent
segmentLength:Segment length, how many length callbacks are sent to recall the file sending progress
datasource:Incoming ota file stream
otaUpdateCallback:Callback of ota upgrade
```java
public interface IOtaUpdateCallback {
    //A callback will be made before the file is sent
    void onStart();
    //The file has been successfully sent to the device
    void onSuccess();
    //If an error occurs during file sending, a callback will be made
    void onError(Exception e);
    //Send File Progress
    void onProgress(int progress);

}
```

**Usage 2(Only kotlin is supported):**
Sending ota files by using the cooperative process operation provided by the framework. Compared with Mode 1, this mode has poor performance, but developers can flexibly operate to cope with various complex ota scenarios
Coroutine idea:<br/>Operation of mode 2 using kotlin coprocess<br/>**Pseudo code**
```kotlin
bleLifeScope.launch ({
    val datasource:InputStream = ...
    val chain = ble.chain(mac)
    val value = ByteArray()
    while(datasource.available()>0){
        datasource.read(value)
        chain.writeByLock().await()
        //Write a piece of data and then process your logic. For example, each ota package includes the header, footer, and data
    }
    
},onError = {
    //The coroutine execution error will be called, and the callback will be in the main thread
},onStart = {
   //Called before the execution of the coroutine starts, the callback is in the main thread
},onFinally = {
    //Regardless of the success or failure of the coroutine execution, this method will eventually be called, and the callback is in the main thread
})
```

**Usage 3:**
Use chained operation to recursively send files<br/>chained recursion idea:<br/>**Pseudo code***
```java
public void writeFile(byte[] value){
   QsBle.getInstance().chain(mac).writeByLockNoRsp(value).start((isSuccess, e) -> {
            if (isSuccess){
                //Fill byte array
                writeFile(value)
            }
    });
}
```
### TODO
- [ ] Documentation
- [ ] Android Ble Tutorial
- [ ] Classic Bluetooth access
- [ ] Ble peripheral function support
- [ ] Coming soon
