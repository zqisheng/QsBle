package com.zqs.ble.core.deamon.message.option;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.os.Build;

import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.BleGattCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.fun.Function3;


/*
 *   @author zhangqisheng
 *   @date 2022-02-28
 *   @description 负责连接操作的消息
 */
public class ConnectMessage extends AbsBleMessage implements IOptionMessage {

    /**
     *
     * @param mac
     * @param reconnectCount  重连次数
     */
    public ConnectMessage(String mac,int reconnectCount){
        super(mac);
        this.reconnectCount = reconnectCount;
    }

    private Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback;

    private int reconnectCount = BleGlobalConfig.reconnectCount;

    private long connectTimeout = BleGlobalConfig.connectTimeout;

    public int getReconnectCount() {
        return reconnectCount;
    }

    public void setReconnectCount(int reconnectCount) {
        this.reconnectCount = reconnectCount;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> getConnectFailCallback() {
        return connectFailCallback;
    }

    public void setConnectFailCallback(Function3<Boolean /*isTimeout*/,Integer /*status*/,Integer/*profileState*/> connectFailCallback) {
        this.connectFailCallback = connectFailCallback;
    }

    @Override
    public void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (getSimpleBle().isConnect(getMac())){
            BleLog.w("BluetoothDevice 已经被连接");
            return;
        }
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        BluetoothGatt gatt = getGatt();
        if (gatt!=null){
            gatt.disconnect();
            gatt.close();
        }
        handleConnectOption(bluetoothAdapter.getRemoteDevice(getMac()));
        //重连消息
        ConnectMessage message = new ConnectMessage(getMac(), reconnectCount - 1){

            private boolean isConnectTimeout = true;

            @Override
            public long getHandleTime() {
                long time = super.getAddQueueTime();
                if (time > getSimpleBle().getConnectStatusUpdateTime(getMac())) {
                    isConnectTimeout = false;
                    //立即执行
                    return 0;
                }
                return super.getHandleTime();
            }

            @Override
            public void onHandlerMessage() {
                if (reconnectCount>0){
                    super.onHandlerMessage();
                }else{
                    BluetoothGatt gatt = getGatt();
                    if (gatt!=null){
                        gatt.disconnect();
                        gatt.close();
                    }
                    if (connectFailCallback!=null){
                        if (isConnectTimeout){
                            //连接超时
                            connectFailCallback.onCallback(true, -1, -1);
                        }else{
                            int[] codes = getSimpleBle().getConnectCode(getMac());
                            if (codes!=null){
                                connectFailCallback.onCallback(false, codes[0], codes[1]);
                            }
                        }
                    }
                }
            }
        };
        message.setConnectTimeout(connectTimeout);
        message.setConnectFailCallback(connectFailCallback);
        getSimpleBle().sendMessageByDelay(message, getConnectTimeout());
    }

    protected void handleConnectOption(BluetoothDevice bluetoothDevice){
        BluetoothGatt bluetoothGatt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt=bluetoothDevice.connectGatt(getSimpleBle().getContext(), false, getBluetoothGattCallbackImpl(),BluetoothDevice.TRANSPORT_LE);
        }else{
            bluetoothGatt=bluetoothDevice.connectGatt(getSimpleBle().getContext(), false, getBluetoothGattCallbackImpl());
        }
        if (bluetoothGatt==null){
            if (connectFailCallback!=null){
                connectFailCallback.onCallback(false, -2, -2);
            }
        }
        getSimpleBle().setGatt(getMac(),bluetoothGatt);
    }

    protected BluetoothGattCallback getBluetoothGattCallbackImpl(){
        return new BleGattCallback();
    }
}
