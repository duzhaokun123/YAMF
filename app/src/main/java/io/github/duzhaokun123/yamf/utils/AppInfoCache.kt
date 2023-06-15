package io.github.duzhaokun123.yamf.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManagerHidden
import android.graphics.drawable.Drawable
import androidx.wear.widget.RoundedDrawable
import io.github.duzhaokun123.yamf.xposed.utils.Instances

object AppInfoCache {
    private val map = mutableMapOf<String, Pair<Drawable, CharSequence>>()

    fun getIconLabel(info: PackageInfo, userId: Int): Pair<Drawable, CharSequence> {
        val pn = info.packageName
        var r = map[pn]
        if (r == null) {
            val applicationInfo = (Instances.packageManager as PackageManagerHidden).getApplicationInfoAsUser(pn, 0, userId)
            r = RoundedDrawable().apply {
                isClipEnabled = true
                radius = 100
                drawable = applicationInfo.loadIcon(Instances.packageManager)
            } to applicationInfo.loadLabel(Instances.packageManager)
            map[pn] = r
        }
        return r
    }
}