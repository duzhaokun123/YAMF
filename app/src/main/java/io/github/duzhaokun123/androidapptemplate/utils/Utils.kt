package io.github.duzhaokun123.androidapptemplate.utils

import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.view.WindowInsetsCompat
import io.github.duzhaokun123.yamf.manager.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val WindowInsetsCompat.maxSystemBarsDisplayCutout
    get() = getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

val WindowInsetsCompat.maxSystemBarsDisplayCutoutIme
    get() = getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime())

fun runMain(block: suspend CoroutineScope.() -> Unit) =
    GlobalScope.launch(Dispatchers.Main, block = block)

fun runIO(block: suspend CoroutineScope.() -> Unit) =
    GlobalScope.launch(Dispatchers.IO, block = block)

fun runNewThread(name: String? = null, block: () -> Unit) =
    (if (name != null) Thread(block, name) else Thread(block)).start()

val isSystemNightMode
    get() = when (application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> null
    }

fun Resources.Theme.getAttr(@AttrRes id: Int) =
    TypedValue().apply { resolveAttribute(id, this, true) }

fun Number.dpToPx() =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), application.resources.displayMetrics
    )