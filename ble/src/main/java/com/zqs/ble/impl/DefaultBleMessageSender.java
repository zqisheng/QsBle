package com.zqs.ble.impl;

import com.zqs.ble.core.api.IBleMessageSender;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.deamon.DefaultMessageLooper;
import com.zqs.ble.core.utils.fun.BooleanFunction;

/*
 *   @author zhangqisheng
 *   @date 2022-07-19
 *   @description
 */
public class DefaultBleMessageSender implements IBleMessageSender {

    DefaultMessageLooper looper;

    public DefaultBleMessageSender(){
        this(new DefaultMessageLooper());
    }

    public DefaultBleMessageSender(DefaultMessageLooper looper){
        this.looper = looper;
    }

    @Override
    public void sendMessage(AbsMessage message) {
        looper.sendMessage(message);
    }

    @Override
    public void rmMessage(AbsMessage message) {
        looper.rmMessage(message);
    }

    @Override
    public void rmMessages(String token) {
        looper.rmMessages(token);
    }

    @Override
    public void sendMessageByDelay(AbsMessage message, long delay) {
        looper.sendMessageByDelay(message,delay);
    }

    @Override
    public boolean currentIsSenderThread() {
        return looper.currentIsSenderThread();
    }

    @Override
    public void rmMessagesByMac(String mac) {
        looper.rmMessagesByMac(mac);
    }

    @Override
    public void clearMessageIf(BooleanFunction<AbsMessage> condition,Runnable clearFinishCallback) {
        looper.clearMessageIf(condition, clearFinishCallback);
    }
}
