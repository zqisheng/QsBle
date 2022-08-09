package com.zqs.ble.coroutines.await

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.zqs.ble.lifecycle.DestroyLifecycleObserver
import kotlinx.coroutines.*
import java.io.Closeable


val LifecycleOwner.bleLifeScope
    get() = BleLifeScope(this.lifecycle)

val Lifecycle.bleLifeScope
    get() = BleLifeScope(this)

class BleLifeScope() : Closeable {

    constructor(lifecycle: Lifecycle) : this() {
        lifecycle.addObserver(object : DestroyLifecycleObserver {
            override fun onDestory() {
                close()
                lifecycle.removeObserver(this)
            }
        })
    }

    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return launch(block, null)
    }

    /**
     * @param block     协程代码块，运行在UI线程
     * @param onError   异常回调，运行在UI线程
     * @param onStart   协程开始回调，运行在UI线程
     * @param onFinally 协程结束回调，不管成功/失败，都会回调，运行在UI线程
     */
    fun launch(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onFinally: (() -> Unit)? = null
    ): Job {
        return coroutineScope.launch {
            try {
                coroutineScope {
                    onStart?.invoke()
                    block()
                }
            } catch (e: Throwable) {
                if (onError != null && isActive) {
                    try {
                        onError(e)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                } else {
                    e.printStackTrace()
                }
            } finally {
                onFinally?.invoke()
            }
        }
    }

    override fun close() {
        coroutineScope.cancel()
    }
}