package com.mja.reyamf.manager.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SidebarHiderService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val receivedString = intent?.getStringExtra("act")
        receivedString?.let {
            if (it == "KILL") {
                YAMFManagerProxy.killSideBar()
            } else {
                YAMFManagerProxy.launchSideBar()
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }
}