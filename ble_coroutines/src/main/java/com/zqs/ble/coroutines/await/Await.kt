package com.zqs.ble.coroutines.await

import com.zqs.ble.BaseChain
import com.zqs.ble.BleChain
import com.zqs.ble.BleChainBuilder
import com.zqs.ble.message.builder.*
import com.zqs.ble.message.pojo.Entry
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 *   @author zhangqisheng
 *   @date 2022-07-31 23:09
 *   @description 
 */

private fun getBuildChains(builder:BleChainBuilder<*,*,*>): LinkedList<BaseChain<*>> {
    val chain: LinkedList<BaseChain<*>> = LinkedList()
    builder.chains.forEach {
        chain.add(it.build())
    }
    return chain
}

/**
 * 连接协程,返回值
 * true:连接成功
 * false:连接失败或者协程出现异常
 * 当链式dump=true(默认),连接失败协程直接抛出异常
 * dump=false时,返回false
 */
suspend fun ConnectChainBuilder.await(): Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 断开连接
 * true:断开连接成功
 * false:断开连接失败或者协程出错
 */
suspend fun DisconnectChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 关闭通知
 * 返回值
 * disable:通知没有打开
 * notification:没有确认机制机制的的通知,这种接受的比较快
 * indication:这个由确认机制,但是可能回的比较慢,但是可以确保到达
 * notification和indication都是通知设置成功,这个状态的选择,app端无法主动进行设置,由设备那边设置
 * null:dump=false时,设置超时,没有设置成功并且没有接收到设备的反馈
 */
suspend fun CancelNotifyChainBuilder.await(): String? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as String)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 打开通知
 * 返回值
 * disable:通知没有打开
 * notification:没有确认机制机制的的通知,这种接受的比较快
 * indication:这个由确认机制,但是可能回的比较慢,但是可以确保到达
 * notification和indication都是通知设置成功,这个状态的选择,app端无法主动进行设置,由设备那边设置
 * null:dump=false时,设置超时,没有设置成功并且没有接收到设备的反馈
 */
suspend fun OpenNotifyChainBuilder.await(): String? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as String)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 读特征值
 * 返回值
 * ByteArray:这个特征读出的值
 * null:dump=false时,读超时
 */
suspend fun ReadChacChainBuilder.await(): ByteArray? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as ByteArray)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 读描述
 * ByteArray:这个描述读出的值
 * null:dump=false时,读超时
 */
suspend fun ReadDescChainBuilder.await(): ByteArray? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as ByteArray)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 读物理信道
 * IntArray:new int[]{txPhy:发送信道,rxPhy:接收信道}
 * null:dump=false时,读超时
 */
suspend fun ReadPhyChainBuilder.await(): IntArray? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as IntArray)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 读rssi
 * Int:rssi的值
 * null:dump=false时,读超时
 */
suspend fun ReadRssiChainBuilder.await(): Int? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as Int)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 设置mtu
 * Int:返回的mtu,有可能设置不成功,返回之前的mtu,所以设置成功后记得对比要设置的是否成功
 * 设置mtu,设置时需要注意,有点设置返回的虽然时成功,但是也有可能用这个mtu时无法通信的,这个不光需要
 * 手机软件和硬件的支持还需要设备端软件和硬件的支持
 * 我建议最好统一设置20byte长度,这也是系统默认的
 * null:dump=false时,读超时
 */
suspend fun RequestMtuChainBuilder.await(): Int? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as Int)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 设置连接参数
 * 这个连接参数影响着手机收发数据的速度
 * IntArray:new int[]{interval:连接间隔, latency:设备时延, timeout:超时时间}
 * null:dump=false时,读超时
 */
suspend fun SetConnectionPriorityChainBuilder.await(): IntArray? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as IntArray)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 设置信道
 * IntArray:new int[]{txPhy:发送信道, rxPhy:接收信道}
 * null:dump=false时,读超时
 */
suspend fun SetPhyChainBuilder.await(): IntArray? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as IntArray)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

suspend fun StopScanChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}


/**
 * 写特征值
 * true:写成功
 * false:写失败,或者dump=false时,写超时
 */
suspend fun WriteByLockChacChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}


/**
 * 写特征值
 * true:写成功
 * false:写失败,或者dump=false时,写超时
 */
suspend fun WriteByLockNoRspChacChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}


/**
 * 写特征值
 * true:写成功
 * false:写失败,或者dump=false时,写超时
 */
suspend fun WriteChacChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}


/**
 * 写描述
 * true:写成功
 * false:写失败,或者dump=false时,写超时
 */
suspend fun WriteDescChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}


/**
 * 写特征值
 * true:写成功
 * false:写失败,或者dump=false时,写超时
 */
suspend fun WriteNoRspChacChainBuilder.await():Boolean {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(true)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else {
                    continuation.resume(false)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}

/**
 * 扫描指定mac的设备
 * Entry<Int, ByteArray>:Int->rssi,ByteArray->广播的字节数据
 * null:没有找到指定的设备
 */
suspend fun StartScanChainBuilder.await(): Entry<Int, ByteArray>? {
    return suspendCancellableCoroutine { continuation ->
        getBuildChains(this).apply {
            (last as StartScanChainBuilder.StartScanChain).isRecordDevice = true
            last.isAsync=false
            last.setChainHandleStatusCallback { isSuccess, isDump, data, e ->
                if (isSuccess) {
                    continuation.resume(data as Entry<Int, ByteArray>)
                } else if (isDump) {
                    continuation.resumeWithException(e!!)
                } else{
                    continuation.resume(null)
                }
            }
            val option=prepare()
            option.setHandleStatusCallback { isSuccess, e ->
                if (!isSuccess){
                    if (continuation.isActive){
                        continuation.resumeWithException(e!!)
                    }
                }
            }
            option.start()
            continuation.invokeOnCancellation {
                option.cancel()
            }
        }
    }
}