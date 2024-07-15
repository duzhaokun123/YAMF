package com.mja.reyamf.common.model

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class AppInfo(
    val id: Int,
    val icon: @RawValue Drawable,
    val label: CharSequence,
    val componentName: ComponentName,
    val userId: Int
) : Parcelable
