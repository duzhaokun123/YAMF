package io.github.duzhaokun123.yamf.utils

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import androidx.wear.widget.RoundedDrawable
import io.github.duzhaokun123.yamf.xposed.utils.Instances

object AppInfoCache {
    private val map = mutableMapOf<String, Pair<Drawable, CharSequence>>()

    fun getIconLabel(info: PackageInfo): Pair<Drawable, CharSequence> {
        val pn = info.packageName
        var r = map[pn]
        if (r == null) {
            r = RoundedDrawable().apply {
                isClipEnabled = true
                radius = 100
                drawable = Instances.packageManager.getApplicationIcon(pn)
            } to Instances.packageManager.getApplicationLabel(Instances.packageManager.getApplicationInfo(pn, 0))
            map[pn] = r
        }
        return r
    }
}