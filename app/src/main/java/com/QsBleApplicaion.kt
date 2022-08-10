package com

import android.app.Application
import com.zqs.ble.QsBle
import com.zqs.ble.core.BleDebugConfig

/*
 *   @author zhangqisheng
 *   @date 2022-08-06 16:20
 *   @description 
 */
class QsBleApplicaion: Application() {

    override fun onCreate() {
        super.onCreate()
        QsBle.getInstance().init(this)
        QsBle.getInstance().setDebug(true)
    }


}