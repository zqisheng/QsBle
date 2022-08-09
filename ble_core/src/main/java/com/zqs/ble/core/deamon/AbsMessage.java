package com.zqs.ble.core.deamon;

/*
 *   @author zhangqisheng
 *   @date 2022-07-15
 *   @description
 */
public abstract class AbsMessage implements Comparable<AbsMessage> {

    public static int PRIORITY_LOW = 30;
    public static int PRIORITY_NORMAL = 60;
    public static int PRIORITY_HIGH =90;

    private String token = null;

    //消息优先级
    private int messagePriority = PRIORITY_NORMAL;
    //被消息线程执行的时间
    private long handleTime;

    private long addQueueTime;

    private volatile boolean isLive = true;

    public abstract void onHandlerMessage();

    public void verifyMessage(){
        if (messagePriority <0|| messagePriority >100){
            throw new IllegalStateException("消息优先级在[0,100]之间");
        }
    }

    @Override
    public int compareTo(AbsMessage o) {
        return o.messagePriority -this.messagePriority;
    }

    public long getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(long handleTime) {
        this.handleTime = handleTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    void letDead(){
        this.isLive = false;
    }

    public boolean isLive(){
        return isLive;
    }

    public long getAddQueueTime() {
        return addQueueTime;
    }

    void setAddQueueTime(long addQueueTime) {
        this.addQueueTime = addQueueTime;
    }

    public void assertCurrentIsSenderThread(){

    }

    public void onDestroy(){

    }

    protected String printLog(){
        return String.format("AbsMessage->%s", this.getClass().getSimpleName());
    }

}
