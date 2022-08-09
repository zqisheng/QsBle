package com.zqs.ble.coroutines.await

import com.zqs.ble.message.builder.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/*
 *   @author zhangqisheng
 *   @date 2022-08-03 22:45
 *   @description 
 */

suspend fun CancelNotifyChainBuilder.asFlow():Flow<String?> = flow {
    emit(await())
}

suspend fun OpenNotifyChainBuilder.asFlow():Flow<String?> = flow {
    emit(await())
}

suspend fun ReadChacChainBuilder.asFlow():Flow<ByteArray?> = flow {
    emit(await())
}

suspend fun ReadDescChainBuilder.asFlow():Flow<ByteArray?> = flow {
    emit(await())
}

suspend fun ReadPhyChainBuilder.asFlow():Flow<IntArray?> = flow {
    emit(await())
}

suspend fun ReadRssiChainBuilder.asFlow():Flow<Int?> = flow {
    emit(await())
}

suspend fun RequestMtuChainBuilder.asFlow():Flow<Int?> = flow {
    emit(await())
}

suspend fun SetConnectionPriorityChainBuilder.asFlow():Flow<IntArray?> = flow {
    emit(await())
}

suspend fun SetPhyChainBuilder.asFlow():Flow<IntArray?> = flow {
    emit(await())
}
//-----
/*


suspend fun ConnectChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun DisconnectChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun StopScanChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun WriteByLockChacChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun WriteByLockNoRspChacChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun WriteChacChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun WriteDescChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}

suspend fun WriteNoRspChacChainBuilder.asFlow():Flow<Boolean> = flow {
    emit(await())
}*/
