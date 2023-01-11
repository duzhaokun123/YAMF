package io.github.duzhaokun123.yamf

import android.app.NotificationManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.material.color.DynamicColors
import io.github.duzhaokun123.yamf.overlay.OverlayService
import io.github.duzhaokun123.yamf.utils.TipUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass

lateinit var application: Application

class Application: android.app.Application() {
    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        HiddenApiBypass.addHiddenApiExemptions("")
        TipUtils.init(this)
        initNotification()
//        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initNotification() {
        val nm = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat.Builder("service", NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("service")
            .build()
        nm.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, "service")
            .setSmallIcon(IconCompat.createWithResource(this, R.drawable.ic_android_black_24dp))
            .build()
        OverlayService.init(0, notification)
    }
}