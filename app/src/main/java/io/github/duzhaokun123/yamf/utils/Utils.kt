package io.github.duzhaokun123.yamf.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Context.getActivity(): Activity? {
    if (this is Activity) return this
    if (this is ContextWrapper) return this.baseContext.getActivity()
    return null
}

val gson by lazy { Gson() }