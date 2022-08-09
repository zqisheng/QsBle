package com.zqs.utils

import com.alibaba.fastjson.JSON

/*
 *   @author zhangqisheng
 *   @date 2022-07-22
 *   @description 
 */

fun Any.toJson(): String {
    return JSON.toJSONString(this)
}