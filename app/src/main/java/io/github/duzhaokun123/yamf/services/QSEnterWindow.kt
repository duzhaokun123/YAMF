package io.github.duzhaokun123.yamf.services

import android.service.quicksettings.TileService
import io.github.duzhaokun123.yamf.xposed.YAMFManagerHelper

class QSEnterWindow: TileService() {
    override fun onClick() {
        super.onClick()
        YAMFManagerHelper.currentToWindow()
    }
}