package io.github.duzhaokun123.yamf

import android.content.Intent
import android.hardware.display.DisplayManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Button
import io.github.duzhaokun123.yamf.xposed.OpenInYAMFBroadcastReceiver
import io.github.duzhaokun123.yamf.utils.TipUtils
//import io.github.duzhaokun123.yamf.ui.overlay.AppWindow
import io.github.duzhaokun123.yamf.xposed.YAMFManagerHelper

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn1).setOnClickListener {
//            OverlayService.addWindow(AppWindow(this), "test${System.currentTimeMillis()}").show()
            TipUtils.showToast("${YAMFManagerHelper.versionName}(${YAMFManagerHelper.versionCode})")
        }
        findViewById<Button>(R.id.btn2).setOnClickListener {
//            OverlayService.stop(this)
            YAMFManagerHelper.createWindow(200, (1 shl 10) or (1 shl 9) or DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE, 0)
        }
        findViewById<Button>(R.id.btn3).setOnClickListener {
//            YAMFManagerHelper.releaseAll()
        }
        findViewById<Button>(R.id.btn4).setOnClickListener {
//            YAMFDisplayManagerHelper.showOverlay()
            sendBroadcast(Intent(OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF))
        }
//        packageManager.getPackagesForUid(Process.SYSTEM_UID)?.forEach {
//            Log.d(TAG, "onCreate: $it")
//        }
    }
}