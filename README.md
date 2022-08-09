**QsBle是一款Android Ble框架**

**特点**
* 相比老的Ble框架,更契合现在Android开发人员的代码风格
* 完全函数式编程,所有的Ble回调都是函数式的
* 支持所有的Ble操作的回调
* 发送的数据超过mtu,QsBle也有完善的分包组包机制
* 支持Kotlin协程,让你用同步的方式操作异步的代码,不用再为蓝牙的各种回调地狱而烦恼
* 支持Flow
* 支持链式编程,让你对蓝牙的各种操作能够顺序执行,以前几百行代码才能实现的逻辑,QsBle只需要一行代码
* 完善的ota支持,你只需提供你的ota文件,其它所有的操作都交给QsBle
* 完善的异常处理机制,让你出现异常能够及时的定位问题
* 核心代码都是使用Java编写,使用Java开发的人也不需要为Java不能使用而担心,并且Kotlin调用Java代码也提供了各种判空机制,让你不用为Kotlin调用Java代码的空指针而担心
* 其它Ble框架有的,QsBle也都有
* **最重要的一个特点是,作者这段时间是会一直维护的[狗头]**

### 使用前
添加蓝牙扫描需要添加的权限,并且在运行时申请权限
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
对于Android12的蓝牙适配,框架中已经默认添加了以下三样权限,所以无须手动添加,但是这三个权限是动态申请权限,在Android12版本及以上,必须在代码中动态申请
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### 初始化
**方式1(推荐):**
```java
QsBle.getInstance().init(Context context);
```
**方式2:**
```java
QsBle.getInstance().init(Context context,Handler handler);
```
*注1:QsBle的所有对Ble的操作和回调都是在一个线程中执行的,方式1的初始化是默认使用框架实现的线程对Ble进行操作.但是如果你想让Ble的所有操作都在自己指定的线程中执行,你也可以传入Handler,这样所有Ble的操作和回调都会在这个指定的Handler中调用了<br/>比如你想让所有的操作都在主线程中回调,你可以传一个主线程的Handler,这样所有Ble的操作和回调都是在主线程中回调了,**但是作者强烈不建议这样做***

*注2:QsBle的初始化获取任何用户手机的隐私信息,所以放心在任何时候初始化*


### 简单使用
**使用方式1:**
```java
QsBle.getInstance().startScan();
QsBle.getInstance().connect(mac);
QsBle.getInstance().connect(mac);
QsBle.getInstance().openNotify(mac,serviceuuid,notifyUuid);
QsBle.getInstance().writeByLock(mac,serviceUuid,writeUuid,value)
....
```

**使用方式2(链式操作)(Java推荐):**
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
*注:以上的链式操作流程是:先连接地址为mac的设备,连接成功后,会打开该设备的notify,打开notify成功后,会设置Connection参数,参数设置成功后,会向特征写数据,框架确认数据写成功后,会断开该设备的连接,其中任何一环没有达到预期结果都不会继续执行*

**使用方式3(kotlin协程)(kotlin推荐)**
```kotlin
//使用kotlin协程实现方式2的操作
bleLifeScope.launch ({
    val chain = ble.chain(mac)
    chain.connect().await()
    chain.openNotify().await()
    chain.requestConnectionToHigh().await()
    chain.writeByLock().await()
    chain.disconnect().await()
},onError = {
    //协程执行错误会调用,回调在主线程中
},onStart = {
   //协程执行开始之前会调用,回调在主线程中
},onFinally = {
    //不管协程执行成功还是失败,这个方法最终都会调用,回调在主线程中
})
```
*注:bleLifeScope是Lifecycle对象的扩展对象,在Lifecycle销毁时,bleLifeScope会自动中断内部的蓝牙协程操作,并且将协程销毁*
### 全局默认配置
*改变默认的全局配置,直接设置对应的值就行*
```java
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
}
```
### Ble回调的监听
所有通过addXXXCallback形式添加的函数,必须在适当的时机调用对应的rmXXXCallback方法将其移除,不然会造成内存泄漏<br/>例如:我在activity的onCreate方法中添加了一个扫描的监听和设备连接状态的监听
```java
    IConnectStatusChangeCallback connectStatusChangeCallback = (device, isConnect, status, profileState) -> {
        //设备连接状态回调
    };
    IScanCallback scanCallback = (device, rssi, scanRecord) -> {
        //扫描到设备回调
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        QsBle.getInstance().addScanCallback(scanCallback);
        QsBle.getInstance().addConnectStatusChangeCallback(mac,connectStatusChangeCallback);
    }
```
那在这个activity销毁的时候,你必须要将这个回调移除,不然会造成内存泄漏甚至app闪退
```java
    @Override
    protected void onDestroy() {
        QsBle.getInstance().rmScanCallback(scanCallback);
        QsBle.getInstance().rmConnectStatusChangeCallback(mac,connectStatusChangeCallback);
    }
```
对于和某个Lifecycle生命周期绑定的回调,QsBle也提供了更好的添加方式
例如在activity中,某些activity实现了Lifecycle类的接口,你只需调用addXXXCallback方法,传入Lifecycle参数,这个生命周期对象被销毁的时候会自动移除回调
```java
//kotlin方式
QsBle.getInstance().addConnectStatusChangeCallback(mac,this.lifecycle, IConnectStatusChangeCallback { device, isConnect, status, profileState -> 
        //连接状态回调
 })
QsBle.getInstance().addScanCallback(this.lifecycle, IScanCallback { device, rssi, scanRecord -> 
        //扫描回调
})
//java方式
QsBle.getInstance().addConnectStatusChangeCallback(mac,this.getLifecycle(), (device, isConnect, status, profileState) ->{
        //连接状态回调
});
QsBle.getInstance().addScanCallback(this.getLifecycle(), (device, rssi, scanRecord) ->{
        //扫描回调
});
```

### 超过Mtu的分包和组包情况
Android Ble的默认mtu是20byte长度,对于大于20byte的数据,需要app和设备开发人员确定分包的协议,一般分包协议将一个mtu数据分为包头和数据位,包头中规定了这个数据的序号等数据,设备收到一个mtu去掉包头,拼装数据,形成一个完整的包

**1.分包的情况**

在QsBle中写特征值分为两种,一种是写一个mtu长度的数据,一种是写超过一个mtu长度的数据

**写一个mtu长度的数据的方法:**
```java
void write(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount);
void writeNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount);
```
**写超过一个mtu长度的数据的方法:**
```java
void writeByLock(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallbac);
void writeByLockNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount, Function2<Boolean,Integer> writeCallback);
```

**处理这两种情况有以下需要注意的情况**
<br/>注:这两种方法在一个类型的设备中最好指使用其中一个,**作者建议所有的写特征值都使用writeByLock和writeByLockNoRsp两个方法**,方法中带Lock的,都是指向这个设备的特征中写的数据都会进入write队列中,后面的消息会等收到系统的write成功回调或者触发超时后再发送下一个消息,由于不带Lock的方法都是直接发送的,不需要排队,如果你发送了一个长数据,长数据没有一下子发送完,后面又调用write方法发送了一个mtu数据,在设备端就会出现撞包现象,导致这该长数据将在设备端无法识别,除非你们的包头中包含了长数据的标志位或者你们的数据不需要分包
所以作者强烈建议<br/>**所有的写特征值都使用writeByLock和writeByLockNoRsp两个方法**

**2.组包的情况**

对于设备端响应的消息大于一个mtu长度,QsBle也提供了组包的接口
你只需实现组包的抽象类BaseMultiPackageAssembly<br/>下面是例子
```kotlin
class MyMulitPkgParser :
    BaseMultiPackageAssembly(){

    override fun onChanged(chac: BluetoothGattCharacteristic?, value: ByteArray?) {
        super.onChanged(chac, value)
    }

    override fun onError(e: Exception?) {
    }

    override fun isLastPkg(value: ByteArray): Boolean {
        //通过判断这个返回的mtu数据是否是最后一个包来判断是否组装成功
        //如果返回true,说明数据全部接受
        //如果返回false,说明还有下一个包,不能判断组装完成
    }

    override fun getPkgIndex(value: ByteArray): Int {
        //从这个返回的mtu数据中取得这个包再整个数据中的包序号
    }
}
```
实现了组包类后,你需要调用
```kotlin
QsBle.getInstance().setMultiPackageAssembly(mac, MyMulitPkgParser())
```
设置这个设备的全局组包解析器
设置成功后,你只需在需要获得数据时添加一个组包回调监听就可以获得组装成功的包了
```kotlin
ble.addBleMultiPkgsCallback(mac,MainActivity.nUuid, IBleMultiPkgsCallback { device, characteristic, result ->
    //所以的包组包成功后会回调
    //result是一个List<byte[]>类型,里面是组装后的所有结果
})
```
*注:这个也需要再合适的时候调用rmBleMultiPkgsCallback将回调移除*


### Ota升级
Ota对app的开发人员来说,就是如何把一个文件传给设备,QsBle提供了完善的Ota升级支持
**方式1:**
调用QsBle的writeFile方法:
```java
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] fileBytes, IOtaUpdateCallback otaUpdateCallback);
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid, UUID chacUuid, File file, IOtaUpdateCallback otaUpdateCallback)
//上面所有的方法都是对下面writeFileNoRsp方法的封装,QsBle只关注你传进来的io流
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid,UUID chacUuid, int fileByteCount, int segmentLength,InputStream datasource,IOtaUpdateCallback otaUpdateCallback);
```
参数说明:
fileByteCount:要发送ota文件的字节总长度
segmentLength:段长度,发送多少长度回调回调一下文件发送的进度
datasource:传入的ota文件流
otaUpdateCallback:ota升级的回调
```java
public interface IOtaUpdateCallback {
    //开始发送文件之前会回调
    void onStart();
    //文件已经成功发送给设备
    void onSuccess();
    //发送文件的过程中出现错误会回调
    void onError(Exception e);
    //发送文件进度
    void onProgress(int progress);

}
```

**方式2(仅支持kotlin):**
使用框架提供的协程操作发送ota文件,相比方式1,这种方式性能较差,但是可以开发人员灵活操作,应对各种复杂ota场景
协程思路:<br/>使用kotlin协程实现方式2的操作<br/>**伪代码**
```kotlin
bleLifeScope.launch ({
    val datasource:InputStream = ...
    val chain = ble.chain(mac)
    val value = ByteArray()
    while(datasource.available()>0){
        datasource.read(value)
        chain.writeByLock().await()
        //写成功一段数据后处理你的逻辑,比如每个ota包中包括包头,包尾,数据部分
    }
    
},onError = {
    //协程执行错误会调用,回调在主线程中
},onStart = {
   //协程执行开始之前会调用,回调在主线程中
},onFinally = {
    //不管协程执行成功还是失败,这个方法最终都会调用,回调在主线程中
})
```

**方式3:**
使用链式操作递归发送文件<br/>链式递归思路:<br/>**伪代码**
```java
public void writeFile(byte[] value){
   QsBle.getInstance().chain(mac).writeByLockNoRsp(value).start((isSuccess, e) -> {
            if (isSuccess){
                //填充字节数组
                writeFile(value)
            }
    });
}
```
### TODO
- [ ] 制作文档
- [ ] Android Ble教程
- [ ] 经典蓝牙接入
- [ ] Ble外设功能支持
- [ ] 敬请期待