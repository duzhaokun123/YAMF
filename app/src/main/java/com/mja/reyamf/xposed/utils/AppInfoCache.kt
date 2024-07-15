package com.mja.reyamf.xposed.utils

import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import androidx.wear.widget.RoundedDrawable

object AppInfoCache {
    fun getIconLabel(info: ActivityInfo): Pair<Drawable, CharSequence> {
        return Pair(
            RoundedDrawable().apply {
                isClipEnabled = true
                radius = 100
                drawable = info.loadIcon(Instances.packageManager)
            },
            info.loadLabel(Instances.packageManager)
        )
    }
}