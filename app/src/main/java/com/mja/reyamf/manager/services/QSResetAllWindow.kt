package com.mja.reyamf.manager.services

import android.service.quicksettings.TileService

class QSResetAllWindow: TileService() {
    override fun onClick() {
        YAMFManagerProxy.resetAllWindow()
    }
}