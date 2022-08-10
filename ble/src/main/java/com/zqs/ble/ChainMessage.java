package com.zqs.ble;

import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.message.exception.ChainHandleTimeoutException;

import java.util.Queue;

import androidx.annotation.Nullable;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class ChainMessage extends AbsMessage {

    private Queue<BaseChain> chains;
    private ChainHandleStatusCallback handleStatusCallback;
    private boolean isShouldHandle = true;

    public ChainMessage(Queue<BaseChain> chains){
        this.chains = chains;
    }

    public void setShouldHandle(boolean shouldHandle) {
        isShouldHandle = shouldHandle;
    }

    public ChainHandleStatusCallback getHandleStatusCallback() {
        return handleStatusCallback;
    }

    public void setHandleStatusCallback(ChainHandleStatusCallback handleStatusCallback) {
        this.handleStatusCallback = handleStatusCallback;
    }

    @Override
    public void onHandlerMessage() {
        if (chains!=null&&!chains.isEmpty()){
            BaseChain chain = chains.poll();
            handleChain(chain);
        }else{
            if (handleStatusCallback!=null){
                handleStatusCallback.onReport(true, null);
            }
        }
    }

    private void handleChain(BaseChain chain) {
        if (!isShouldHandle){
            Exception e = new IllegalStateException(String.format("%s has been cancelled",this.getClass().getName()));
            callReport(chain,e,true);
            return;
        }
        chain.setCallback(false);
        chain.setParentMessage(this);
        chain.onCreate();
        QsBle.getInstance().sendMessageByDelay(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                chain.onHandle();
                if (!chain.isAlreadHandleTimeoutOption()){
                    QsBle.getInstance().sendMessageByDelay(new AbsMessage() {
                        @Override
                        public void onHandlerMessage() {
                            if (chain.isDump()){
                                chains.clear();
                                if (handleStatusCallback!=null){
                                    handleStatusCallback.onReport(false, new IllegalStateException());
                                }
                            }
                            chain.onFail(new ChainHandleTimeoutException(chain.getClass().getName()));
                        }

                        @Override
                        public boolean isLive() {
                            return !chain.isCallback();
                        }
                    }, chain.getTimeout()==0?2000:chain.getTimeout());
                }
            }
        },chain.getDelay());
    }

    public void onChainHandleFail(BaseChain chain, Exception e) {
        if (chain.getRetry()>0){
            chain.letRetryLess();
            QsBle.getInstance().sendMessageByDelay(new AbsMessage() {
                @Override
                public void onHandlerMessage() {
                    handleChain(chain);
                }
            },chain.getDelay());
        }else{
            //不是最后一个chain
            if (!chain.isDump()&&!chains.isEmpty()){
                onHandlerMessage();
            }else{
                callReport(chain,e,false);
            }
        }
    }

    private void callReport(BaseChain chain, Exception e,boolean isSuccess){
        if (chain!=null){
            chain.onReport(isSuccess,chain.isDump(),null,e);
        }
        if (handleStatusCallback!=null){
            handleStatusCallback.onReport(isSuccess, e);
        }
    }

    public void onChainHandleSuccess(BaseChain chain, Object data) {
        chain.onReport(true,chain.isDump(),data,null);
        onHandlerMessage();
    }

    @FunctionalInterface
    public interface ChainHandleStatusCallback{
        void onReport(Boolean isSuccess, @Nullable Exception e);
    }

    public interface ChainHandleOption{
        void cancel();

        void setHandleStatusCallback(ChainHandleStatusCallback handleStatusCallback);

        void start();

    }
}
