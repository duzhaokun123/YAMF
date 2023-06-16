package io.github.duzhaokun123.yamf.xposed.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object TipUtil {
    fun showToast(msg: String) {
        Toast.makeText(Instances.systemContext, "[YAMF] $msg", Toast.LENGTH_LONG).show()
    }
}