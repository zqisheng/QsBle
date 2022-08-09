package com.zqs.ble.core.utils.fun;

/*
 *   @author zhangqisheng
 *   @date 2022-07-25 22:42
 *   @description
 */
@FunctionalInterface
public interface BooleanFunction<T> {

    boolean apply(T t);

}
