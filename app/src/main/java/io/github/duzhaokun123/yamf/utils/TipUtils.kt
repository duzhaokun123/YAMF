package io.github.duzhaokun123.yamf.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object TipUtils {
    private lateinit var context: Context
    private lateinit var prefix: String

    fun init(context: Context, prefix: String = "") {
        this.context = context
        this.prefix = prefix
    }


    fun showToast(msg: String) {
        Toast.makeText(context, "$prefix$msg", Toast.LENGTH_LONG).show()
    }
}