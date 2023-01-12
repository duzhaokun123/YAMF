package io.github.duzhaokun123.yamf.model


import android.hardware.display.DisplayManager
import com.google.gson.annotations.SerializedName

data class Config(
    @SerializedName("densityDpi")
    var densityDpi: Int = 200,
    @SerializedName("flags")
    var flags: Int = (1 shl 10) or (1 shl 9) or DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE,
    @SerializedName("coloredController")
    var coloredController: Boolean = false
)