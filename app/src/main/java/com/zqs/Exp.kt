package com.zqs

import android.app.Activity
import com.zqs.ble.QsBle

/*
 *   @author zhangqisheng
 *   @date 2022-08-06 16:32
 *   @description 
 */

val Activity.ble: QsBle
    get() = QsBle.getInstance()