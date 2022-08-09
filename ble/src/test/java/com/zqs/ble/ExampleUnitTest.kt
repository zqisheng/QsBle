package com.zqs.ble

import com.zqs.ble.core.utils.Utils
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test1234(){
        var file= File("C:\\Users\\zhang\\Desktop\\testFile.txt")
        val iso = FileOutputStream(file)
        repeat(100){
            iso.write(Utils.hexStrToBytes("0100019901010101010101010101010101010192010101010101010101010101010101010101018F010200000000000000000000000000000000008201030000000000000000000000000000000000A8010400000000000000000000000000000000007E010500000000000000000000000000000000005401060000303332313737373439380000000000330107000000000000000000000000000000000000010800000000000000000000000000000000008101890000000000000000000000000000000000C0"))
        }
        iso.close()
        println("执行成功")
    }
}