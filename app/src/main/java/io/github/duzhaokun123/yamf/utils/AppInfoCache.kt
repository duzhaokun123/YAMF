package io.github.duzhaokun123.yamf.utils

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import io.github.duzhaokun123.yamf.xposed.utils.Instances

object AppInfoCache {
    val map = mutableMapOf<String, Pair<Drawable, CharSequence>>()

    fun getIconLabel(info: PackageInfo): Pair<Drawable, CharSequence> {
        val pn = info.packageName
        var r = map[pn]
        if (r == null) {
            r = Instances.packageManager.getApplicationIcon(pn) to Instances.packageManager.getApplicationLabel(Instances.packageManager.getApplicationInfo(pn, 0))
            map[pn] = r
        }
        return r
    }
}