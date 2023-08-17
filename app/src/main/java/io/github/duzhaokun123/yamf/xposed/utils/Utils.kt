package io.github.duzhaokun123.yamf.xposed.utils

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.ActivityTaskManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.IPackageManagerHidden
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.util.TypedValue
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.newInstance
import de.robv.android.xposed.XposedBridge
import io.github.duzhaokun123.yamf.common.model.StartCmd
import io.github.duzhaokun123.yamf.common.onException
import io.github.duzhaokun123.yamf.xposed.services.YAMFManager

fun log(tag: String, message: String) {
    XposedBridge.log("[$tag] $message")
}

fun log(tag: String, message: String, t: Throwable) {
    XposedBridge.log("[$tag] $message")
    XposedBridge.log(t)
}

@SuppressLint("MissingPermission")
fun moveTask(taskId: Int, displayId: Int) {
    Instances.activityTaskManager.moveRootTaskToDisplay(taskId, displayId)
    Instances.activityManager.moveTaskToFront(taskId, 0)
}

fun Number.dpToPx() =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    )

val emptyContextParams = ContextParams.Builder().build()

fun Context.createContext() = createContext(emptyContextParams)

fun startActivity(context: Context, componentName: ComponentName, userId: Int, displayId: Int) {
    context.invokeMethod(
        "startActivityAsUser",
        args(
            Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = componentName
                `package` = component!!.packageName
                action = Intent.ACTION_VIEW
            },
            ActivityOptions.makeBasic().apply {
                launchDisplayId = displayId
                this.invokeMethod("setCallerDisplayId", args(displayId), argTypes(Integer.TYPE))
            }.toBundle(),
            UserHandle::class.java.newInstance(
                args(userId),
                argTypes(Integer.TYPE)
            )
        ), argTypes(Intent::class.java, Bundle::class.java, UserHandle::class.java)
    )
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

fun StartCmd.startAuto(displayId: Int) {
        when {
            canStartActivity && canMoveTask ->
                moveToDisplay(Instances.systemContext, taskId!!, componentName!!, userId!!, displayId)
            canMoveTask -> {
                runCatching {
                    moveTask(taskId!!, displayId)
                }.onException {
                    TipUtil.showToast("can't move task $taskId")
                }
            }
            canStartActivity -> {
                runCatching {
                    startActivity(Instances.systemContext, componentName!!, userId!!, displayId)
                }.onException {
                    TipUtil.showToast("can't start activity $componentName")
                }
            }
        }
    }

    fun getTopRootTask(displayId: Int): ActivityTaskManager.RootTaskInfo? {
        Instances.activityTaskManager.getAllRootTaskInfosOnDisplay(displayId).forEach { task ->
            if (task.visible)
                return task
        }
        return null
    }

fun Context.registerReceiver(action: String, onReceive: BroadcastReceiver.(Context, Intent) -> Unit) =
    registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onReceive(this, context, intent)
        }
    }, android.content.IntentFilter(action))

val ActivityInfo.componentName: ComponentName
    get() = ComponentName(packageName, name)

fun IPackageManagerHidden.getActivityInfoCompat(className: ComponentName, flags: Int, userId: Int): ActivityInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getActivityInfo(className, flags.toLong(), userId)
    } else {
        getActivityInfo(className, flags, userId)
    }