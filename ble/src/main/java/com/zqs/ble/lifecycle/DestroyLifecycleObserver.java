package com.zqs.ble.lifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/*
 *   @author zhangqisheng
 *   @date 2022-07-19
 *   @description
 */
public interface DestroyLifecycleObserver extends LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy();
}