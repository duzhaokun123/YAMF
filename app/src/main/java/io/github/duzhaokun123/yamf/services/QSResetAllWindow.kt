package io.github.duzhaokun123.yamf.services

import android.content.Intent
import android.service.quicksettings.TileService
import io.github.duzhaokun123.yamf.ui.window.AppWindow
import io.github.duzhaokun123.yamf.xposed.YAMFManagerHelper

class QSResetAllWindow: TileService() {
    override fun onClick() {
        YAMFManagerHelper.resetAllWindow()
        sendBroadcast(Intent(AppWindow.ACTION_RESET_ALL_WINDOW))
    }
}