package com.mja.reyamf.manager.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mja.reyamf.manager.services.YAMFManagerProxy


class CurrentToFloatingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()

//        val intent = Intent(this, CurrentToFloatingService::class.java)
//        intent.setAction(Intent.ACTION_ASSIST)

//        val intent = Intent()
//        intent.action = "com.mja.reyamf.action.ACTION_OPEN_IN_YAMF"
//        intent.addCategory(Intent.CATEGORY_DEFAULT)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        Log.d("reYAMFWOO", "long pressed")

//        startService(intent)
//        sendBroadcast(intent)

        YAMFManagerProxy.currentToWindow()

//        finish()
    }
}