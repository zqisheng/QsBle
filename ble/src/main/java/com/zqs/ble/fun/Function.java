package com.zqs.ble.fun;

@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}