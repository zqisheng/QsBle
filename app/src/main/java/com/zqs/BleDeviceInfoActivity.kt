package com.zqs

import android.Manifest
import android.bluetooth.BluetoothGattService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.rxLifeScope
import com.alibaba.fastjson.JSON
import com.zqs.app.R
import com.zqs.ble.core.utils.BleLog
import com.zqs.ble.core.utils.Utils
import com.zqs.ble.coroutines.await.await
import com.zqs.ble.coroutines.await.bleLifeScope
import com.zqs.ble.message.ota.IOtaUpdateCallback
import com.zqs.utils.toJson
import kotlinx.android.synthetic.main.activity_ble_device_info.*
import kotlinx.android.synthetic.main.activity_ble_device_info.connect
import kotlinx.android.synthetic.main.activity_ble_device_info.connection_priority_high
import kotlinx.android.synthetic.main.activity_ble_device_info.connection_priority_balanced
import kotlinx.android.synthetic.main.activity_ble_device_info.connection_priority_low_power
import kotlinx.android.synthetic.main.activity_ble_device_info.disconnect
import kotlinx.android.synthetic.main.activity_ble_device_info.read_phy
import kotlinx.android.synthetic.main.activity_ble_device_info.read_rssi_tv
import kotlinx.android.synthetic.main.activity_ble_device_info.write_chac
import kotlinx.android.synthetic.main.activity_main.*
import rxhttp.toDownload
import rxhttp.wrapper.param.RxHttp
import java.io.File
import java.util.*


class BleDeviceInfoActivity : AppCompatActivity() {
    lateinit var mac:String

    private var selectFile:File? = File("/storage/emulated/0/Downloads/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_device_info)
        mac = intent.getStringExtra("mac")!!
        requestPermission()
        initBleListener()
        setServiceValue()
        macaddress.text=mac
        connect_status.text = if (ble.isConnect(mac)) "连接状态:连接" else "连接状态:断开"
        disconnec_auto_reconnect_count_btn.setOnClickListener {
            if (!disconnec_auto_reconnect_count.text.toString().isNullOrEmpty()){
                ble.setAutoReconnectCount(mac,disconnec_auto_reconnect_count.text.toString().toInt())
            }
        }
        connect.setOnClickListener {
            if (ble.isConnect(mac)){
                Toast.makeText(this, "已经被连接", Toast.LENGTH_SHORT).show()
            }else{
                bleLifeScope.launch({
                    val time = System.currentTimeMillis()
                    ble.chain(mac).connect().await()
                    Toast.makeText(this@BleDeviceInfoActivity, "连接成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
                },onError = {
                    Toast.makeText(this@BleDeviceInfoActivity, "连接失败:${it.cause}", Toast.LENGTH_SHORT).show()
                },onStart = {
                    showLoading()
                },onFinally = {
                    hideLoading()
                })
            }
        }
        disconnect.setOnClickListener {
            bleLifeScope.launch ({
                val time=System.currentTimeMillis()
                ble.chain(mac).disconnect().await()
                service_uuids.text=""
                Toast.makeText(this@BleDeviceInfoActivity, "设备已经断开,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "设备断开失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }
        service_uuids.text=JSON.toJSONString(ble.getGattService(mac),true)
        write_chac.setOnClickListener {
            var suuid = main_servuce_uuid.text.toString()
            if (suuid.isNullOrEmpty()){
                Toast.makeText(this, "服务uuid是null的", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            suuid="0000${suuid}-0000-1000-8000-00805f9b34fb"
            var cuuid = write_chac_uuid.text.toString()
            if (cuuid.isNullOrEmpty()){
                Toast.makeText(this, "特征uuid是null的", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            cuuid="0000${cuuid}-0000-1000-8000-00805f9b34fb"
            var hexData = write_chac_data.text.toString()
            if (hexData.isNullOrEmpty()){
                Toast.makeText(this, "写数据是null", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            bleLifeScope.launch ({
                val time=System.currentTimeMillis()
                if (is_norsp.isChecked){
                    ble.chain(mac).connect().writeByLockNoRsp(
                        UUID.fromString(suuid),
                        UUID.fromString(cuuid),
                        Utils.hexStrToBytes(hexData)
                    ).before(true) {
                        BleLog.d("操作之前")
                    }.after(true) {
                        BleLog.d("操作之后")
                    }.error(true) {
                        BleLog.d("操作错误:${it.message}")
                    }.data(true) {
                        BleLog.d("操作数据:${it}")
                    }.await()
                }else{
                    ble.chain(mac).connect().writeByLock(
                        UUID.fromString(suuid),
                        UUID.fromString(cuuid),
                        Utils.hexStrToBytes(hexData)
                    ).await()
                }
                Toast.makeText(this@BleDeviceInfoActivity, "特征值写成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "特征值写失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }
        open_notify_chac.setOnClickListener {
            bleLifeScope.launch ({
                var suuid = main_servuce_uuid.text.toString()
                if (suuid.isNullOrEmpty()){
                    Toast.makeText(this@BleDeviceInfoActivity, "服务uuid是null的", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                suuid="0000${suuid}-0000-1000-8000-00805f9b34fb"
                var nuuid = notify_chac_uuid.text.toString()
                if (nuuid.isNullOrEmpty()){
                    Toast.makeText(this@BleDeviceInfoActivity, "通知uuid是null的", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                nuuid="0000${nuuid}-0000-1000-8000-00805f9b34fb"
                val time=System.currentTimeMillis()
                val result:String?=ble.chain(mac).connect().openNotify(UUID.fromString(suuid),UUID.fromString(nuuid)).await()
                notify_status.text="当前通知状态:${result}"
                Toast.makeText(this@BleDeviceInfoActivity, "通知设置成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "通知设置失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }
        cancel_notify_chac.setOnClickListener {
            bleLifeScope.launch ({
                var suuid = main_servuce_uuid.text.toString()
                if (suuid.isNullOrEmpty()){
                    Toast.makeText(this@BleDeviceInfoActivity, "服务uuid是null的", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                suuid="0000${suuid}-0000-1000-8000-00805f9b34fb"
                var nuuid = notify_chac_uuid.text.toString()
                if (nuuid.isNullOrEmpty()){
                    Toast.makeText(this@BleDeviceInfoActivity, "通知uuid是null的", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                nuuid="0000${nuuid}-0000-1000-8000-00805f9b34fb"
                val time=System.currentTimeMillis()
                val result:String?=ble.chain(mac).connect().cancelNotify(UUID.fromString(suuid),UUID.fromString(nuuid)).await()
                notify_status.text="当前通知状态:${result}"
                Toast.makeText(this@BleDeviceInfoActivity, "特征值设置成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "特征值设置失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }
        read_phy.setOnClickListener {
            bleLifeScope.launch ({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val result:IntArray?=ble.chain(mac).readPhy().before(true) {
                        BleLog.d("操作之前")
                    }.after(true) {
                        BleLog.d("操作之后")
                    }.error(true) {
                        BleLog.d("操作错误:${it.message}")
                    }.data(true) {
                        BleLog.d("操作数据:${it.toJson()}")
                    }.await()
                    phy_tv.text="txPhy=${result?.get(0)},rxPhy=${result?.get(1)}"
                } else {
                    phy_tv.text="不支持"
                }
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "readPhy失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }
        read_rssi.setOnClickListener {
            bleLifeScope.launch ({
                val result:Int?=ble.chain(mac).connect().readRssi().await()
                read_rssi_tv.text="rssi=${result}"
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "readRssi失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }
        read_mtu.setOnClickListener {
            val mtu:Int = read_mtu_et.text.toString().toInt()
            bleLifeScope.launch ({
                val time=System.currentTimeMillis()
                val result:Int?=ble.chain(mac).connect().requestMtu(mtu).timeout(5000).await()
                Toast.makeText(this@BleDeviceInfoActivity, "requestMtu成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
                read_mtu_tv.text="当前mtu=${result}"
            },onError = {
                Toast.makeText(this@BleDeviceInfoActivity, "requestMtu失败:${it.message}", Toast.LENGTH_SHORT).show()
            },onStart = {
                showLoading()
            },onFinally = {
                hideLoading()
            })
        }

        write_file.setOnClickListener {
            if (selectFile!=null){
                var suuid = main_servuce_uuid.text.toString()
                if (suuid.isNullOrEmpty()){
                    Toast.makeText(this, "服务uuid是null的", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                suuid="0000${suuid}-0000-1000-8000-00805f9b34fb"
                var cuuid = write_chac_uuid.text.toString()
                if (cuuid.isNullOrEmpty()){
                    Toast.makeText(this, "特征uuid是null的", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                cuuid="0000${cuuid}-0000-1000-8000-00805f9b34fb"
                if (write_file_tv.text.toString().isNullOrEmpty()){
                    Toast.makeText(this, "文件链接是null的", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                rxLifeScope.launch ({
                    loadingDialog?.setText("下载文件中...")
                    RxHttp.get(write_file_tv.text.toString()).toDownload(externalCacheDir!!.absolutePath+File.separator+"writeBleFile")
                        .await().let {
                            ble.writeFileNoRsp(mac, UUID.fromString(suuid), UUID.fromString(cuuid),
                                File(it),object :IOtaUpdateCallback{
                                    override fun onStart() {
                                        runOnUiThread {
                                            showLoading()
                                        }
                                    }

                                    override fun onSuccess() {
                                        runOnUiThread {
                                            hideLoading()
                                            Toast.makeText(this@BleDeviceInfoActivity, "ota成功", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onError(e: Exception?) {
                                        runOnUiThread {
                                            hideLoading()
                                        }
                                    }

                                    override fun onProgress(progress: Int) {
                                        runOnUiThread {
                                            loadingDialog?.setText("ota进度:${progress}")
                                        }
                                    }
                                })
                        }
                },onStart = {
                    showLoading()
                },onError = {
                    hideLoading()
                    Toast.makeText(this, "发生错误 ${it.javaClass.name} ${it.message} ${it.cause}", Toast.LENGTH_SHORT).show()
                },onFinally = {
                    hideLoading()
                })
            }
        }

        connection_priority_low_power.setOnClickListener {
            bleLifeScope.launch({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val time=System.currentTimeMillis()
                    ble.chain(mac).requestConnectionToLowPower().await()
                    Toast.makeText(this@BleDeviceInfoActivity, "设置成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
                }
            }, onStart = {
                showLoading()
            }, onError = {
                hideLoading()
            }, onFinally = {
                hideLoading()
            })
        }

        connection_priority_balanced.setOnClickListener {
            bleLifeScope.launch({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val time=System.currentTimeMillis()
                    ble.chain(mac).requestConnectionToBalanced().await()
                    Toast.makeText(this@BleDeviceInfoActivity, "设置成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
                }
            }, onStart = {
                showLoading()
            }, onError = {
                hideLoading()
            }, onFinally = {
                hideLoading()
            })
        }

        connection_priority_high.setOnClickListener {
            bleLifeScope.launch({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val time=System.currentTimeMillis()
                    ble.chain(mac).requestConnectionToHigh().await()
                    Toast.makeText(this@BleDeviceInfoActivity, "设置成功,耗时:${System.currentTimeMillis()-time}ms", Toast.LENGTH_SHORT).show()
                }
            }, onStart = {
                showLoading()
            }, onError = {
                hideLoading()
            }, onFinally = {
                hideLoading()
            })
        }

        bleLifeScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val result:IntArray?=ble.chain(mac).readPhy().await()
                phy_tv.text="txPhy=${result?.get(0)},rxPhy=${result?.get(1)}"
            } else {
                phy_tv.text="不支持"
            }
            val result:Int?=ble.chain(mac).readRssi().await()
            read_rssi_tv.text="rssi=${result}"
        }

    }

    //简单的设置一下
    private fun setServiceValue() {
        val services = ble.getGattService(mac) ?: return
        if (services.isEmpty())return
        val service:BluetoothGattService = services.get(0)
        main_servuce_uuid.setText(service.uuid.toString().substring(4, 8))
        service.characteristics?.forEach {
            if (ble.isSupportWriteProperty(it)){
                write_chac_uuid.setText(it.uuid.toString().substring(4, 8))
            }
            if (ble.isSupportNotifyProperty(it)) {
                notify_chac_uuid.setText(it.uuid.toString().substring(4, 8))
            }
        }
    }

    private fun initBleListener(){
        ble.addConnectStatusChangeCallback(mac,this.lifecycle){ device, isConnect, status, profileState ->
            runOnUiThread {
                connect_status.text = if (isConnect) "连接状态:连接" else "连接状态:断开"
                if (!isConnect) {
                    service_uuids.text = ""
                    read_mtu_tv.text = ""
                } else {

                }
            }
        }
        ble.addServicesDiscoveredCallback(mac,this.lifecycle){ device, services, status ->
            runOnUiThread {
                service_uuids.text=JSON.toJSONString(services,true)
            }
        }
        ble.addNotifyStatusCallback(mac,this.lifecycle) { device, descriptor, notifyEnable ->
            runOnUiThread {
                if (!notifyEnable){
                    notify_status.text="当前通知状态:disable"
                }
            }
        }
        ble.addConnectionUpdatedCallback(mac,this.lifecycle) { device, interval, latency, timeout, status ->
            runOnUiThread {
                connection_params.text="interval=${interval},latency=${latency},timeout=${timeout}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //ble.rmConnectStatusChangeCallback(mac,connectStateChangedCallback)
        //ble.rmServicesDiscoveredCallback(mac,serviceDiscoveredCallback)
        ble.disconnect(mac)
    }


    var loadingDialog:LoadingDialog? = null

    private fun showLoading(){
        loadingDialog?.dismiss()
        loadingDialog = LoadingDialog()
        loadingDialog?.dialog?.setCanceledOnTouchOutside(false)
        loadingDialog?.show(supportFragmentManager,"")
    }

    private fun hideLoading(){
        loadingDialog?.dismiss()
        loadingDialog=null
    }



    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
                ,"android.permission.BLUETOOTH_SCAN"
                ,"android.permission.BLUETOOTH_ADVERTISE"
                ,"android.permission.BLUETOOTH_CONNECT"),1)
        }
    }

}