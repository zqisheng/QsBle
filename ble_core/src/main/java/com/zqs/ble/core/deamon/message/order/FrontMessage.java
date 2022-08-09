package com.zqs.ble.core.deamon.message.order;

import com.zqs.ble.core.deamon.AbsMessage;

/*
 *   @author zhangqisheng
 *   @date 2022-03-29
 *   @description
 */
public class FrontMessage extends AbsMessage implements IFrontMessage {

    public FrontMessage() {

    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
    }

    protected String printLog(){
        return String.format("FrontMessage->%s", this.getClass().getSimpleName());
    }


}
