package io.github.duzhaokun123.yamf.manager.services

import android.service.quicksettings.TileService

class QSResetAllWindow: TileService() {
    override fun onClick() {
        YAMFManagerProxy.resetAllWindow()
    }
}