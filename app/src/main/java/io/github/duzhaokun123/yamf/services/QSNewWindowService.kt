package io.github.duzhaokun123.yamf.services

import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import io.github.duzhaokun123.yamf.xposed.YAMFManagerHelper

class QSNewWindowService : TileService() {
    override fun onClick() {
        super.onClick()
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("useAppList", true))
            YAMFManagerHelper.openAppList()
        else YAMFManagerHelper.createWindow()
    }
}