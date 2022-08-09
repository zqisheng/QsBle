package com.zqs.ble.core.api;

import com.zqs.ble.core.deamon.AbsMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-07-13
 *   @description
 */
public interface IMessageSender {

    void sendMessage(AbsMessage message);

    void rmMessage(AbsMessage message);

    void rmMessages(String token);

    void sendMessageByDelay(AbsMessage message, long delay);

    boolean currentIsSenderThread();
}
