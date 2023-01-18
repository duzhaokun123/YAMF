package io.github.duzhaokun123.yamf.xposed.utils

import de.robv.android.xposed.XposedBridge

fun log(tag: String, message: String) {
    XposedBridge.log("[$tag] $message")
}

fun log(tag: String, message: String, t: Throwable) {
    XposedBridge.log("[$tag] $message")
    XposedBridge.log(t)
}