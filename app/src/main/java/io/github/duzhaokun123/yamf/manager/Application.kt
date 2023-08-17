package io.github.duzhaokun123.yamf.manager

import com.google.android.material.color.DynamicColors

lateinit var application: Application

class Application: android.app.Application() {
    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}