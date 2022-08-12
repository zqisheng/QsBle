package com.zqs.ble.core.deamon;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.api.IBleMessageSender;
import com.zqs.ble.core.api.IMessageLooper;
import com.zqs.ble.core.deamon.message.callback.ICallbackMessage;
import com.zqs.ble.core.deamon.message.option.WriteChacLockMessage;
import com.zqs.ble.core.deamon.message.order.FrontMessage;
import com.zqs.ble.core.deamon.message.order.IFrontMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.fun.BooleanFunction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 *   @author zhangqisheng
 *   @date 2022-08-05
 *   @description
 */
public class DefaultMessageLooper implements IMessageLooper, IBleMessageSender {

    private Thread thread;
    private volatile boolean isPrepareWait = false;
    protected Runnable startLooper = this::loop;

    public DefaultMessageLooper(){
        this.thread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    startLooper.run();
                }
            }
        };
    }

    public DefaultMessageLooper(Thread thread){
        this.thread = thread;

    }

    private ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    /**
     * callbackMessages中的消息都是回调在系统线程中
     * optionMessages和frontMessages中的消息都是回调在同一个线程中,默认是main线程
     */
    private Queue<AbsMessage> callbackMessages = new LinkedBlockingQueue<>();
    //所有的add操作必须在一个线程中回调,BleThread主要是对线程的遍历操作，不改变messages中的size
    private Queue<AbsMessage> optionMessages = new LinkedBlockingQueue<>();
    //该消息会最优先执行
    private Queue<AbsMessage> frontMessages = new LinkedBlockingQueue<>();
    //延迟执行的消息
    private Queue<AbsMessage> delayMessages = new LinkedBlockingQueue<>();

    private Map<String, Queue<WriteChacLockMessage>> writeLockMessages = new HashMap<>();

    private Queue<AbsMessage> delayCacheMessage = new LinkedList<>();

    //BleThread当前最小等待时时间
    private long minHandlerTime = Long.MAX_VALUE;

    public final void block(){
        try {
            //阻塞
            isPrepareWait = true;
            lock.lock();
            if (!callbackMessages.isEmpty() || !optionMessages.isEmpty() || !frontMessages.isEmpty()) {
                lock.unlock();
            } else {
                if (minHandlerTime==Long.MAX_VALUE) {
                    if (BleDebugConfig.isOpenBleThreadLog){
                        BleLog.d("BleThread->NoDelay await");
                    }
                    block(Long.MAX_VALUE);
                } else {
                    long awaitTime = minHandlerTime - System.currentTimeMillis();
                    minHandlerTime = Long.MAX_VALUE;
                    if (BleDebugConfig.isOpenBleThreadLog){
                        BleLog.d("BleThread->toAwait:" + awaitTime + "ms");
                    }
                    if (awaitTime >=0) {
                        block(awaitTime);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            isPrepareWait = false;
        }
    }

    public void block(long awaitTime) throws InterruptedException {
        if (awaitTime==Long.MAX_VALUE){
            condition.await();
        }else{
            condition.await(awaitTime, TimeUnit.MILLISECONDS);
        }
    }

    public void awake(Thread thread){
        try {
            if (thread.getState() == Thread.State.TIMED_WAITING) {
                lock.lock();
                if (thread.getState() == Thread.State.TIMED_WAITING) {
                    condition.signal();
                }
            } else if (thread.getState() == Thread.State.WAITING) {
                lock.lock();
                if (thread.getState() == Thread.State.WAITING) {
                    condition.signal();
                }
            } else if (isPrepareWait) {
                lock.lock();
                awake(thread);
            }else if (thread.getState() == Thread.State.NEW) {
                lock.lock();
                if (thread.getState() == Thread.State.NEW) {
                    thread.start();
                }
            }
        } catch (Exception e) {
            if (AbsBleMessage.simpleBle.isStrictMode()){
                throw e;
            }else{
                e.printStackTrace();
            }
        } finally {
            while (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void loop() {
        try {
            loopFrontMessages();
            loopOptionMessages();
            loopCallbackMessages();
            loopWriteMessage();
            loopDelayMessage();
        } catch (Exception e) {
            if (AbsBleMessage.simpleBle.isStrictMode()){
                throw e;
            }else{
                e.printStackTrace();
            }
        } finally {
            block();
        }
    }

    private Queue<WriteChacLockMessage> getWriteQueueIfNullCreate(String key){
        Queue<WriteChacLockMessage> queue = writeLockMessages.get(key);
        if (queue == null) {
            queue = new LinkedList<>();
            writeLockMessages.put(key, queue);
        }
        return queue;
    }

    private void loopFrontMessages() {
        AbsMessage msg;
        while ((msg = frontMessages.poll()) != null) {
            if (!msg.isLive()) continue;
            try {
                msg.verifyMessage();
                handleMessage(msg);
            } catch (Exception e) {
                if (AbsBleMessage.simpleBle.isStrictMode()){
                    throw e;
                }else{
                    e.printStackTrace();
                }
            }finally {
                msg.onDestroy();
            }
        }
    }

    private void loopOptionMessages() {
        AbsMessage msg;
        while (frontMessages.isEmpty() && (msg = optionMessages.poll()) != null) {
            if (!msg.isLive()) continue;
            try {
                if (msg instanceof WriteChacLockMessage){
                    getWriteQueueIfNullCreate(((WriteChacLockMessage) msg).getWriteKey()).add((WriteChacLockMessage) msg);
                }else{
                    msg.verifyMessage();
                    handleMessage(msg);
                }
            } catch (Exception e) {
                if (AbsBleMessage.simpleBle.isStrictMode()){
                    throw e;
                }else{
                    e.printStackTrace();
                }
            } finally {
                msg.onDestroy();
            }
        }
    }

    private void loopCallbackMessages() {
        AbsMessage msg;
        while (frontMessages.isEmpty() && optionMessages.isEmpty() && (msg = callbackMessages.poll()) != null) {
            if (!msg.isLive()) continue;
            try {
                msg.verifyMessage();
                handleMessage(msg);
            } catch (Exception e) {
                if (AbsBleMessage.simpleBle.isStrictMode()){
                    throw e;
                }else{
                    e.printStackTrace();
                }
            } finally {
                msg.onDestroy();
            }
        }
    }

    private void loopDelayMessage() {
        AbsMessage msg;
        delayCacheMessage.clear();
        while (frontMessages.isEmpty() && optionMessages.isEmpty() && callbackMessages.isEmpty() && (msg = delayMessages.poll()) != null) {
            if (!msg.isLive()) continue;
            try {
                msg.verifyMessage();
                if (System.currentTimeMillis() - msg.getHandleTime() >= 0) {
                    handleMessage(msg);
                    msg.onDestroy();
                } else {
                    if (msg.getHandleTime() < minHandlerTime) {
                        minHandlerTime = msg.getHandleTime();
                    }
                    delayCacheMessage.add(msg);
                }
                if (BleDebugConfig.isOpenBleThreadLog){
                    BleLog.d(String.format("BleThread->%s,time=%d,minHandlerTime=%d,currentMinAwaitTime=%d", msg.getClass().getSimpleName(), System.currentTimeMillis(), minHandlerTime, minHandlerTime - System.currentTimeMillis()));
                }
            } catch (Exception e) {
                if (AbsBleMessage.simpleBle.isStrictMode()){
                    throw e;
                }else{
                    e.printStackTrace();
                }
            }
        }
        if (!delayCacheMessage.isEmpty()) {
            delayMessages.addAll(delayCacheMessage);
        }
    }

    private void loopWriteMessage() {
        if (writeLockMessages.isEmpty())return;
        WriteChacLockMessage msg;
        for (String key:writeLockMessages.keySet()){
            Queue<WriteChacLockMessage> queue = writeLockMessages.get(key);
            boolean isHandle = true;
            while (isHandle&&!queue.isEmpty()){
                isHandle = false;
                msg = queue.peek();
                if (msg==null||!msg.isLive()) {
                    queue.poll();
                    msg.onDestroy();
                    isHandle = true;
                    continue;
                }
                if (msg!=null){
                    msg.verifyMessage();
                    if (msg.getShouldHandle()) {
                        if (System.currentTimeMillis() - msg.getHandleTime() >= 0){
                            handleMessage(msg);
                            if (!msg.getShouldHandle()){
                                queue.poll();
                                msg.onDestroy();
                                isHandle = true;
                            }
                        }else if (msg.getHandleTime() < minHandlerTime) {
                            minHandlerTime = msg.getHandleTime();
                        }
                    } else {
                        queue.poll();
                        msg.onDestroy();
                        isHandle = true;
                    }
                }
            }
        }
    }

    private void handleMessage(AbsMessage message) {
        if (BleDebugConfig.isOpenBleThreadLog){
            BleLog.d(String.format("BleThread->handleMessage:%s", message.getClass().getName()));
        }
        message.onHandlerMessage();
    }

    @Override
    public void sendMessage(AbsMessage message) {
        if (BleDebugConfig.isOpenBleThreadLog){
            BleLog.d(String.format("BleThread->sendMessage:%s", message.getClass().getName()));
        }
        if (message instanceof IFrontMessage) {
            message.setAddQueueTime(System.currentTimeMillis());
            frontMessages.add(message);
        } else if (message instanceof ICallbackMessage) {
            message.setAddQueueTime(System.currentTimeMillis());
            callbackMessages.add(message);
        }else {
            message.setAddQueueTime(System.currentTimeMillis());
            optionMessages.add(message);
        }
        awake(thread);
    }

    @Override
    public void rmMessage(AbsMessage message) {
        if (BleDebugConfig.isOpenBleThreadLog){
            BleLog.d(String.format("BleThread->rmMessage:%s", message.getClass().getSimpleName()));
        }
        message.letDead();
    }

    @Override
    public void rmMessages(String token) {
        if (BleDebugConfig.isOpenBleThreadLog){
            BleLog.d("BleThread->rmMessages token=" + token);
        }
        frontMessages.add(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                for (AbsMessage msg : optionMessages) {
                    if (msg.getToken().equals(token)) {
                        msg.letDead();
                    }
                }
                for (AbsMessage msg : delayMessages) {
                    if (msg.getToken().equals(token)) {
                        msg.letDead();
                    }
                }
            }
        });
        awake(thread);
    }

    @Override
    public void sendMessageByDelay(AbsMessage message, long delay) {
        if (BleDebugConfig.isOpenBleThreadLog){
            BleLog.d(String.format("BleThread->sendMessageByDelay:%s,delay=%d", message.getClass().getName(),delay));
        }
        message.setHandleTime(System.currentTimeMillis() + delay);
        delayMessages.add(message);
        awake(thread);
    }

    @Override
    public boolean currentIsSenderThread() {
        return Thread.currentThread() == thread;
    }

    @Override
    public void rmMessagesByMac(String mac) {
        if (BleDebugConfig.isOpenBleThreadLog){
            BleLog.d(String.format("BleThread->rmMessagesByMac:%s", mac));
        }
        AbsMessage message = new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                for (AbsMessage msg : frontMessages) {
                    if (msg instanceof AbsBleMessage && ((AbsBleMessage) msg).getMac().equals(mac) && this.getAddQueueTime() > msg.getAddQueueTime()) {
                        msg.letDead();
                    }
                }
                for (AbsMessage msg : optionMessages) {
                    if (msg instanceof AbsBleMessage && ((AbsBleMessage) msg).getMac().equals(mac) && this.getAddQueueTime() > msg.getAddQueueTime()) {
                        msg.letDead();
                    }
                }
                for (AbsMessage msg : delayMessages) {
                    if (msg instanceof AbsBleMessage && ((AbsBleMessage) msg).getMac().equals(mac) && this.getAddQueueTime() > msg.getAddQueueTime()) {
                        msg.letDead();
                    }
                }
                for (String key:writeLockMessages.keySet()){
                    for (AbsMessage msg : writeLockMessages.get(key)) {
                        if (msg instanceof AbsBleMessage && ((AbsBleMessage) msg).getMac().equals(mac) && this.getAddQueueTime() > msg.getAddQueueTime()) {
                            msg.letDead();
                        }
                    }
                }
            }
        };
        message.setAddQueueTime(System.currentTimeMillis());
        frontMessages.add(message);
        awake(thread);
    }

    @Override
    public void clearMessageIf(BooleanFunction<AbsMessage> condition, Runnable clearFinishCallback) {
        sendMessage(new FrontMessage(){
            @Override
            public void onHandlerMessage() {
                for (AbsMessage msg:optionMessages){
                    if (condition.apply(msg)){
                        msg.letDead();
                    }
                }
                for (AbsMessage msg:delayMessages){
                    if (condition.apply(msg)){
                        msg.letDead();
                    }
                }
                for (String key:writeLockMessages.keySet()){
                    for (AbsMessage msg : writeLockMessages.get(key)) {
                        if (condition.apply(msg)){
                            msg.letDead();
                        }
                    }
                }
                if (clearFinishCallback!=null){
                    clearFinishCallback.run();
                }
            }
        });
    }

}
