package com.mja.reyamf

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.mja.reyamf.manager.utils.AppContext

lateinit var application: Application

class Application: Application() {

    init {
        application = this
        AppContext.context = this
    }

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}