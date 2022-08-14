package com.zqs.ble.core.deamon.message.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.BleGlobalConfig;
import com.zqs.ble.core.callback.GlobalBleCallback;
import com.zqs.ble.core.callback.abs.IConnectStatusChangeCallback;
import com.zqs.ble.core.deamon.AbsBleMessage;
import com.zqs.ble.core.deamon.message.option.DiscoverServiceMessage;
import com.zqs.ble.core.utils.BleLog;

import java.util.List;

/*
 *   @author zhangqisheng
 *   @date 2022-04-19
 *   @description
 */
public class OnConnectionStateMessage extends AbsBleMessage implements ICallbackMessage {

    private BluetoothDevice device;
    //, int status, int profileState
    private int status;
    private int profileState;

    public OnConnectionStateMessage(BluetoothDevice device,int status,int profileState){
        super(device.getAddress());
        this.device=device;
        this.status=status;
        this.profileState=profileState;
    }

    @Override
    public final void onHandlerMessage() {
        assertCurrentIsSenderThread();
        if (BleDebugConfig.isOpenGattCallbackLog){
            BleLog.d(String.format("BleCallback OnConnectionStateMessage:mac=%s,status=%s,profileState=%s", device.getAddress(), status, profileState));
        }
        boolean isConnect = profileState == BluetoothProfile.STATE_CONNECTED;
        boolean oldConnectStatus = getSimpleBle().isConnect(getMac());
        String disconnecter = null;
        if (isConnect){
            clearBeforeOption();
            getSimpleBle().sendMessage(new DiscoverServiceMessage(getMac(), BleGlobalConfig.discoveryServiceFailRetryCount));
        }else{
            //老的连接状态为连接,新的连接状态为断开,说明是从系统一侧断开连接,非代码主动断开
            if (oldConnectStatus){
                disconnecter = "device";
            }
            BluetoothGatt gatt = getGatt();
            if (gatt!=null){
                gatt.close();
            }
            getSimpleBle().setGatt(getMac(), null);
        }
        getSimpleBle().updateConnectStatus(getMac(), isConnect,status,profileState,disconnecter);
        GlobalBleCallback globalBleCallback = getSimpleBle().getGlobalBleGattCallback();
        if (globalBleCallback!=null){
            globalBleCallback.onConnectionStateChange(device,status,profileState);
        }
        List<IConnectStatusChangeCallback> callbacks = getSimpleBle().getCallbackManage().getConnectStatusChangeCallbacks(getMac());
        if (callbacks!=null&&!callbacks.isEmpty()){
            for (IConnectStatusChangeCallback callback:callbacks){
                callback.onConnectStatusChanged(device, profileState == BluetoothProfile.STATE_CONNECTED, status, profileState);
            }
        }
    }
}
