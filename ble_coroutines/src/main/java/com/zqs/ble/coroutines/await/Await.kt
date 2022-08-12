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