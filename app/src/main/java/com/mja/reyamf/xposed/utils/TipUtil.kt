package com.mja.reyamf.xposed.utils

import android.annotation.SuppressLint
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object TipUtil {
    fun showToast(msg: String) {
        Toast.makeText(Instances.systemContext, "[reYAMF] $msg", Toast.LENGTH_LONG).show()
    }
}