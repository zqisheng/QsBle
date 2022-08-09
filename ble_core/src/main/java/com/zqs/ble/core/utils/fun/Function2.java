package com.zqs.ble.core.utils.fun;

/*
 *   @author zhangqisheng
 *   @date 2022-07-18
 *   @description
 */
@FunctionalInterface
public interface Function2<P1,P2> {

    void onCallback(P1 p1, P2 p2);

}
