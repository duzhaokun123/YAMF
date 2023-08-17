package io.github.duzhaokun123.yamf.manager.services

import android.service.quicksettings.TileService

class QSEnterWindow: TileService() {
    override fun onClick() {
        super.onClick()
        YAMFManagerProxy.currentToWindow()
    }
}