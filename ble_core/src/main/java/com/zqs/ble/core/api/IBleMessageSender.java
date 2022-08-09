package com.zqs.ble.core.api;

import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.utils.fun.BooleanFunction;

/*
 *   @author zhangqisheng
 *   @date 2022-07-20
 *   @description
 */
public interface IBleMessageSender extends IMessageSender {

    void rmMessagesByMac(String mac);

    void clearMessageIf(BooleanFunction<AbsMessage> condition,Runnable clearFinishCallback);

}
