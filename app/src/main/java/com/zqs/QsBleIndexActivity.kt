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
import com.zqs.ble.core.utils.Utils
import com.zqs.ble.coroutines.await.await
import com.zqs.ble.coroutines.await.bleLifeScope
import kotlinx.android.synthetic.main.activity_ble_index.*
import kotlinx.android.synthetic.main.item_scan_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QsBleIndexActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_index)
        requestPermission()
        if (!ble.bluetoothEnable()){
            ble.openBluetooth()
        }
        ble.chain().startScan().filterName("TT").distinct().setScanStatusCallback {
            runOnUiThread {
                if (it){
                    Toast.makeText(this@QsBleIndexActivity, "扫描已经开始", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@QsBleIndexActivity, "扫描已经结束", Toast.LENGTH_SHORT).show()
                }
            }
        }.setScanCallback { device, rssi, scanRecord ->
            runOnUiThread {
                addToDeviceList(device,rssi,scanRecord)
            }
        }.start()
        start_scan.setOnClickListener {
            if (ble.isScaning){
                Toast.makeText(this, "请先结束扫描", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            scan_device_con.removeAllViews()
            ble.chain().startScan().filterName("TT").distinct().setScanStatusCallback {
                runOnUiThread {
                    if (it){
                        Toast.makeText(this@QsBleIndexActivity, "扫描已经开始", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@QsBleIndexActivity, "扫描已经结束", Toast.LENGTH_SHORT).show()
                    }
                }
            }.setScanCallback { device, rssi, scanRecord ->
                runOnUiThread {
                    addToDeviceList(device,rssi,scanRecord)
                }
            }.start()
        }

        stop_scan.setOnClickListener {
            ble.chain().stopScan().setScanStatusCallback {
                runOnUiThread {
                    if (it){
                        Toast.makeText(this@QsBleIndexActivity, "扫描已经开始", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@QsBleIndexActivity, "扫描已经结束", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

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
                    }else{
                        Toast.makeText(this@QsBleIndexActivity, "连接失败", Toast.LENGTH_SHORT).show()
                    }
                    hideLoading()
                    Intent(this@QsBleIndexActivity,BleDeviceInfoActivity::class.java).apply {
                        this.putExtra("mac",device.address)
                        startActivity(this)
                    }
                }
            }
        }
        scan_device_con.addView(view)
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
                ,Manifest.permission.ACCESS_COARSE_LOCATION
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