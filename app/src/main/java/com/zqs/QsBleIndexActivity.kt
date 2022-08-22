package com.zqs

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zqs.app.R
import com.zqs.ble.QsBle
import com.zqs.ble.core.BleGlobalConfig
import com.zqs.ble.core.callback.abs.IScanCallback
import com.zqs.ble.core.callback.abs.IScanErrorCallback
import com.zqs.ble.core.callback.abs.IScanStatusCallback
import com.zqs.ble.core.callback.scan.SimpleScanConfig
import com.zqs.ble.core.utils.BleLog
import com.zqs.ble.core.utils.Utils
import com.zqs.ble.coroutines.await.await
import com.zqs.ble.coroutines.await.bleLifeScope
import com.zqs.utils.toJson
import kotlinx.android.synthetic.main.activity_ble_index.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class QsBleIndexActivity : AppCompatActivity() {

    val scanCallback:IScanCallback = IScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            addToDeviceList(device,rssi,scanRecord)
        }
    }

    val scanStatusCallback:IScanStatusCallback = IScanStatusCallback {
        runOnUiThread {
            if (it){
                Toast.makeText(this@QsBleIndexActivity, "扫描已经开始", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@QsBleIndexActivity, "扫描已经结束", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val scanError:IScanErrorCallback= IScanErrorCallback {
        runOnUiThread {
            Toast.makeText(this@QsBleIndexActivity, "扫描错误errorCode:${it}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_index)
        requestPermission()
        if (!ble.bluetoothEnable()){
            ble.openBluetooth()
        }
        start_scan.setOnClickListener {
            if (ble.isScaning){
                Toast.makeText(this, "请先结束扫描", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            scan_device_con.removeAllViews()
            SimpleScanConfig().apply {
                if (BleGlobalConfig.globalScanConfig!=null){
                    BleGlobalConfig.globalScanConfig.toApplyConfig(this)
                }
                if (!scan_mac.text.toString().isNullOrEmpty()){
                    this.mac=scan_mac.text.toString()
                }
                if (!scan_name.text.toString().isNullOrEmpty()){
                    this.deviceName=scan_name.text.toString()
                }
                if (!scan_service_uuid.text.toString().isNullOrEmpty()){
                    this.serviceUuid=UUID.fromString("0000${scan_service_uuid.text}-0000-1000-8000-00805f9b34fb")
                }
                this.isRepeatCallback=is_repeat_scan.isChecked
                ble.startScan(20000, null, this)
            }
        }

        stop_scan.setOnClickListener {
            ble.stopScan()
        }
        ble.addScanCallback(lifecycle,scanCallback)
        ble.addScanStatusCallback(lifecycle,scanStatusCallback)
        ble.addScanErrorCallback(lifecycle,scanError)
    }

    private fun addToDeviceList(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
        val view = layoutInflater.inflate(R.layout.item_scan_device, scan_device_con, false)
        view.findViewById<TextView>(R.id.name).text = "name:${device.name?.toString()}"
        view.findViewById<TextView>(R.id.mac).text = "mac:${device.address}"
        view.findViewById<TextView>(R.id.rssi).text = "rssi:${rssi}"
        view.findViewById<TextView>(R.id.data).text = "data:${Utils.bytesToHexStr(scanRecord)}"
        view.findViewById<TextView>(R.id.connect).setOnClickListener {
            showLoading()
            bleLifeScope.launch {
                val isConnect = ble.chain(device.address).connect().dump(false).await()
                withContext(Dispatchers.Main){
                    if (isConnect){
                        Toast.makeText(this@QsBleIndexActivity, "连接成功", Toast.LENGTH_SHORT).show()
                        Intent(this@QsBleIndexActivity,BleDeviceInfoActivity::class.java).apply {
                            this.putExtra("mac",device.address)
                            startActivity(this)
                        }
                    }else{
                        Toast.makeText(this@QsBleIndexActivity, "连接失败", Toast.LENGTH_SHORT).show()
                    }
                    hideLoading()
                }
            }
        }
        scan_device_con.addView(view)
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ,"android.permission.BLUETOOTH_SCAN"
            ,"android.permission.BLUETOOTH_ADVERTISE"
            ,"android.permission.BLUETOOTH_CONNECT"),1)
        }
    }

    var loadingDialog:LoadingDialog? = null

    private fun showLoading(){
        loadingDialog?.dismiss()
        loadingDialog = LoadingDialog()
        loadingDialog?.show(supportFragmentManager,"")
    }

    private fun hideLoading(){
        loadingDialog?.dismiss()
    }

}