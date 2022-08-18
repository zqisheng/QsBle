package com.zqs.ble;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.message.exception.ChainHandleTimeoutException;

import java.util.Objects;
import java.util.Queue;

import androidx.annotation.Nullable;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public class ChainMessage extends AbsMessage {

    private Queue<BaseChain> chains;
    private BaseChain currentChain;
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
            callAfterCallback(currentChain,()->pollChainsAndHandle());
        }else{
            callAfterCallback(currentChain,()->{
                if (handleStatusCallback!=null){
                    handleStatusCallback.onReport(true, null);
                }
            });
        }
    }

    private void pollChainsAndHandle(){
        currentChain = chains.poll();
        currentChain.onCreate();
        handleChain(currentChain);
    }

    private void handleChain(BaseChain chain) {
        if (!isShouldHandle){
            chain.onDestroy();
            Exception e = new IllegalStateException(String.format("%s has been cancelled",this.getClass().getName()));
            callReport(chain,e,true);
            return;
        }
        callBeforeCallback(chain,()->handleChainOption(chain));
    }

    private void handleChainOption(BaseChain chain){
        chain.setCallback(false);
        chain.setParentMessage(this);
        QsBle.getInstance().sendMessageByDelay(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                chain.onHandle();
                if (!chain.isAlreadHandleTimeoutOption()){
                    QsBle.getInstance().sendMessageByDelay(new AbsMessage() {
                        @Override
                        public void onHandlerMessage() {
                            if (!isShouldHandle){
                                return;
                            }
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
        if (BleDebugConfig.isOpenChainHandleLog){
            BleLog.d(String.format("chain handle fail:%s,%s", chain.getClass().getName(), e));
        }
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
                callErrorCallback(chain,e,()->callReport(chain,e,false));
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
        if (BleDebugConfig.isOpenChainHandleLog){
            if (e!=null){
                BleLog.e(String.format("handle chain message error:%s",e));
            }
        }
    }

    public void onChainHandleSuccess(BaseChain chain, Object data) {
        if (BleDebugConfig.isOpenChainHandleLog){
            BleLog.d(String.format("chain handle success:%s", chain.getClass().getName()));
        }
        callDataCallback(chain,data,()->{
            chain.onReport(true,chain.isDump(),data,null);
            onHandlerMessage();
        });
    }

    private void callBeforeCallback(BaseChain chain,Runnable callFinish) {
        if (chain.beforeCallback!=null){
            if (chain.beforeIsRunMain){
                sendMessageToMain(()->{
                    try{
                        chain.beforeCallback.run();
                    }catch (Exception e){
                        chain.setRetry(0);
                        onChainHandleFail(chain,e);
                    }
                    QsBle.getInstance().sendMessage(new AbsMessage() {
                        @Override
                        public void onHandlerMessage() {
                           callFinish.run();
                        }
                    });
                });
            }else{
                try{
                    chain.beforeCallback.run();
                }catch (Exception e){
                    chain.setRetry(0);
                    onChainHandleFail(chain,e);
                }
                callFinish.run();
            }
        }else{
            callFinish.run();
        }
    }

    private void callAfterCallback(BaseChain chain,Runnable callFinish) {
        if (chain!=null&&chain.afterCallback!=null){
            if (chain.afterIsRunMain){
                sendMessageToMain(()->{
                    try{
                        chain.afterCallback.run();
                    }catch (Exception e){
                        chain.setRetry(0);
                        onChainHandleFail(chain,e);
                    }
                    QsBle.getInstance().sendMessage(new AbsMessage() {
                        @Override
                        public void onHandlerMessage() {
                            callFinish.run();
                        }
                    });
                });
            }else{
                try{
                    chain.afterCallback.run();
                }catch (Exception e){
                    chain.setRetry(0);
                    onChainHandleFail(chain,e);
                }
                callFinish.run();
            }
        }else{
            callFinish.run();
        }
    }

    private void callDataCallback(BaseChain chain,Object data,Runnable callFinish){
        if (data!=null&&chain.acceptDataCallback!=null){
            if (chain.acceptDataIsRunMain){
                sendMessageToMain(()->{
                    try{
                        chain.acceptDataCallback.apply(data);
                    }catch (Exception e){
                        BleLog.d(String.format("message acceptDataCallback throw error:%s", e.toString()));
                        cancel();
                    }
                    callFinish.run();
                });
            }else{
                try{
                    chain.acceptDataCallback.apply(data);
                }catch (Exception e){
                    BleLog.d(String.format("message acceptDataCallback throw error:%s", e.toString()));
                    cancel();
                }
                callFinish.run();
            }
        }else{
            callFinish.run();
        }
    }

    private void callErrorCallback(BaseChain chain,Exception e,Runnable callFinish){
        if (e!=null&&chain.errorCallback!=null){
            if (chain.errorIsRunMain){
                sendMessageToMain(()->{
                    chain.errorCallback.apply(e);
                    QsBle.getInstance().sendMessage(new AbsMessage() {
                        @Override
                        public void onHandlerMessage() {
                            callFinish.run();
                        }
                    });
                });
            }else{
                chain.errorCallback.apply(e);
                callFinish.run();
            }
        }else{
            callFinish.run();
        }
    }

    @FunctionalInterface
    public interface ChainHandleStatusCallback{
        void onReport(Boolean isSuccess, @Nullable Exception e);
    }

    public void cancel(){
        setShouldHandle(false);
    }

    private void sendMessageToMain(Runnable callback){
        QsBle.getInstance().sendMessageToMain(()->{
            if (isShouldHandle){
                callback.run();
            }else {
                if (currentChain!=null){
                    currentChain.onDestroy();
                }
            }
        });
    }

    public interface ChainHandleOption{
        void cancel();

        void setHandleStatusCallback(ChainHandleStatusCallback handleStatusCallback);

        void start();

    }
}
