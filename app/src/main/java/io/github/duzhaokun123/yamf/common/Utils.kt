package io.github.duzhaokun123.yamf.common

import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

val gson by lazy { Gson() }

fun RecyclerView.resetAdapter() {
    this.adapter = adapter
}

inline fun <T> Result<T>.onException(action: (exception: Exception) -> Unit): Result<T> =
    this.onFailure { t ->
        if (t is Error) throw t
        action(t as Exception)
    }