package io.github.duzhaokun123.yamf.model


import com.google.gson.annotations.SerializedName

data class Config(
    @SerializedName("densityDpi")
    var densityDpi: Int = 200,
    @SerializedName("flags")
    /*
     * VIRTUAL_DISPLAY_FLAG_SECURE                          1 << 2
     * VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT            1 << 7
     * VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS  1 << 9
     * VIRTUAL_DISPLAY_FLAG_TRUSTED                         1 << 10
     */
    var flags: Int = 1668,
    @SerializedName("coloredController")
    var coloredController: Boolean = false,
    @SerializedName("windowfy")
    /*
     * 0: move task only
     * 1: start activity only
     * 2: move task, failback to start activity
     */
    var windowfy: Int = 0
)