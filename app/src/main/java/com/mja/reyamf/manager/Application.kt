package com.mja.reyamf.manager

import com.google.android.material.color.DynamicColors
import com.mja.reyamf.manager.utils.AppContext

lateinit var application: Application

class Application: android.app.Application() {
    init {
        application = this
        AppContext.context = this
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}