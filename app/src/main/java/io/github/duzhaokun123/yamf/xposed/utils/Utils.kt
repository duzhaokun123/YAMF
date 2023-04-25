package io.github.duzhaokun123.yamf.xposed.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import de.robv.android.xposed.XposedBridge
import io.github.duzhaokun123.yamf.utils.onException
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.YAMFManager

fun log(tag: String, message: String) {
    XposedBridge.log("[$tag] $message")
}

fun log(tag: String, message: String, t: Throwable) {
    XposedBridge.log("[$tag] $message")
    XposedBridge.log(t)
}

fun moveToDisplay(context: Context, taskId: Int, componentName: ComponentName, userId: Int, displayId: Int) {
    when (YAMFManager.config.windowfy) {
        0 -> {
            runCatching {
                moveTask(taskId, displayId)
            }.onException {
                TipUtil.showToast("can't move task $taskId")
            }
        }
        1 -> {
            runCatching {
                startActivity(context, componentName, userId, displayId)
            }.onException {
                TipUtil.showToast("can't start activity $componentName")
            }
        }
        2 -> {
            runCatching {
                moveTask(taskId, displayId)
            }.onException {
                TipUtil.showToast("can't move task $taskId")
                runCatching {
                    startActivity(context, componentName, userId, displayId)
                }.onException {
                    TipUtil.showToast("can't start activity $componentName")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun moveTask(taskId: Int, displayId: Int) {
    Instances.activityTaskManager.moveRootTaskToDisplay(taskId, displayId)
    Instances.activityManager.moveTaskToFront(taskId, 0)
}