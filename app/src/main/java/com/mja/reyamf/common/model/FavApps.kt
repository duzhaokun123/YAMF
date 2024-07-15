package com.mja.reyamf.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavApps(
    val packageName: String,
    val userId: Int
): Parcelable
