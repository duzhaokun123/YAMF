package io.github.duzhaokun123.androidapptemplate.utils

import android.app.Activity
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.manager.application

object TipUtil {
    private val map = mutableMapOf<Int, CoordinatorLayout>()

    fun registerCoordinatorLayout(context: Context, coordinatorLayout: CoordinatorLayout?) {
        coordinatorLayout?.let { map[context.hashCode()] = it }
    }

    fun unregisterCoordinatorLayout(context: Context) {
        map.remove(context.hashCode())
    }

    fun showToast(msg: CharSequence?) {
        runMain {
            Toast.makeText(application, "$msg", Toast.LENGTH_LONG).show()
        }
    }

    fun showToast(@StringRes resId: Int) =
        showToast(application.getText(resId))

    fun showSnackbar(coordinatorLayout: CoordinatorLayout, msg: CharSequence?) {
        runMain {
            Snackbar.make(coordinatorLayout, "$msg", BaseTransientBottomBar.LENGTH_LONG).show()
        }
    }

    fun showSnackbar(coordinatorLayout: CoordinatorLayout, @StringRes resId: Int) =
        showSnackbar(coordinatorLayout, application.getText(resId))

    fun showSnackbar(coordinatorLayout: CoordinatorLayout, t: Throwable) {
        runMain {
            val msg = t.localizedMessage ?: t.message ?: application.getString(R.string.unknown_error)
            Snackbar.make(coordinatorLayout, msg, BaseTransientBottomBar.LENGTH_LONG)
                .setAction(R.string.details) {
                    MaterialAlertDialogBuilder(coordinatorLayout.context)
                        .setTitle(msg)
                        .setMessage("${t.message}\n${t.stackTraceToString()}")
                        .show()
                        .findViewById<TextView>(android.R.id.message)
                        ?.setTextIsSelectable(true)
                }.show()
        }
    }

    fun showTip(context: Context?, t: Throwable) {
        val msg = t.localizedMessage ?: t.message ?: "未知错误"
        if (context == null || (context is Activity && context.window.decorView.isVisible.not())) {
            showToast(msg)
            return
        }
        map[context.hashCode()]?.let {
            showSnackbar(it, t)
            return
        }
        showToast(msg)
    }

    fun showTip(context: Context?, msg: CharSequence?) {
        if (context == null || (context is Activity && context.window.decorView.isVisible.not())) {
            showToast(msg)
            return
        }
        map[context.hashCode()]?.let {
            showSnackbar(it, msg)
            return
        }
        showToast(msg)
    }

    fun showTip(context: Context?, @StringRes resId: Int) =
        showTip(context, application.getText(resId))
}
