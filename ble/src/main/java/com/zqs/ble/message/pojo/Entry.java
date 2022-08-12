package com.zqs.ble.message.pojo;

/*
 *   @author zhangqisheng
 *   @date 2022-08-04
 *   @description
 */
public class Entry<First,Second> {

    public Entry(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    public First first;
    public Second second;

}
