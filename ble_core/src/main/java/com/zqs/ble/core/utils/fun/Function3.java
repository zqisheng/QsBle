package com.zqs.ble.core.utils.fun;

/*
 *   @author zhangqisheng
 *   @date 2022-07-18
 *   @description
 */
@FunctionalInterface
public interface Function3<P1,P2,P3> {

    void onCallback(P1 p1, P2 p2,P3 p3);

}
