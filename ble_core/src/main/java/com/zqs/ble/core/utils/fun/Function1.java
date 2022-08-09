package com.zqs.ble.core.utils.fun;

/*
 *   @author zhangqisheng
 *   @date 2022-07-18
 *   @description
 */
@FunctionalInterface
public interface Function1<P> {

    void onCallback(P p);

}
