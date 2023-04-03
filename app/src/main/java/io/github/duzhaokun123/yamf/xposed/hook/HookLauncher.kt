package io.github.duzhaokun123.yamf.xposed.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Icon
import android.view.View
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.newInstance
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.xposed.OpenInYAMFBroadcastReceiver
import io.github.duzhaokun123.yamf.xposed.utils.log


class HookLauncher : IXposedHookLoadPackage {
    companion object {
        const val TAG = "YAMF_HookLauncher"
    }

    private var mUserContext: Context? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookRecents(lpparam)
        hookTaskbar(lpparam)
    }

    private fun hookRecents(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (runCatching{ XposedHelpers.findClass("com.android.quickstep.TaskOverlayFactory", lpparam.classLoader) }.isFailure) return
        log(TAG, "hooking recents ${lpparam.packageName}")
        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.quickstep.TaskOverlayFactory", lpparam.classLoader), "getEnabledShortcuts", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val taskView = param.args[0] as View
                val shortcuts = param.result as MutableList<Any>
                var itemInfo = XposedHelpers.getObjectField(shortcuts[0], "mItemInfo")
                itemInfo = itemInfo.javaClass.newInstance(args(itemInfo), argTypes(itemInfo.javaClass))
                val activity = taskView.context.getActivity()
                val task = XposedHelpers.callMethod(taskView, "getTask")
                val key = XposedHelpers.getObjectField(task, "key")
                val taskId = XposedHelpers.getIntField(key, "id")
                val topComponent = XposedHelpers.callMethod(itemInfo, "getTargetComponent") as ComponentName
                val userId = XposedHelpers.getIntField(key, "userId")

                val class_RemoteActionShortcut = XposedHelpers.findClass("com.android.launcher3.popup.RemoteActionShortcut", lpparam.classLoader)
                val intent = Intent(OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF).apply {
                    setPackage("android")
                    putExtra(OpenInYAMFBroadcastReceiver.EXTRA_TASK_ID, taskId)
                    putExtra(OpenInYAMFBroadcastReceiver.EXTRA_COMPONENT_NAME, topComponent)
                    putExtra(OpenInYAMFBroadcastReceiver.EXTRA_USER_ID, userId)
                    putExtra(OpenInYAMFBroadcastReceiver.EXTRA_SOURCE, "recents")
                }
                val action = RemoteAction(
                    Icon.createWithResource(getUserContext(), R.drawable.ic_picture_in_picture_alt_24),
                    getUserContext().getString(R.string.open_with_yamf) + if (BuildConfig.DEBUG) " ($taskId)" else "",
                    "",
                    PendingIntent.getBroadcast(
                        AndroidAppHelper.currentApplication(),
                        1345,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                val c = class_RemoteActionShortcut.constructors[0]
                val shortcut = when (c.parameterCount) {
                    4 -> c.newInstance(action, activity, itemInfo, null)
                    3 -> c.newInstance(action, activity, itemInfo)
                    else -> {
                        log(TAG, "unknown RemoteActionShortcut constructor: ${c.toGenericString()}")
                        null
                    }
                }

                if (shortcut != null) {
                    shortcuts.add(shortcut)
                }
            }
        })
    }

    private fun hookTaskbar(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (runCatching{ XposedHelpers.findClass("com.android.launcher3.taskbar.TaskbarActivityContext", lpparam.classLoader) }.isFailure) return
        log(TAG, "hooking taskbar ${lpparam.packageName}")
        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.launcher3.taskbar.TaskbarActivityContext", lpparam.classLoader), "startItemInfoActivity", object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any? {
                val infoIntent = XposedHelpers.callMethod(param.args[0], "getIntent") as Intent
                val intent = Intent(OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF).apply {
                    setPackage("android")
                    putExtra(OpenInYAMFBroadcastReceiver.EXTRA_COMPONENT_NAME, infoIntent.component)
                    putExtra(OpenInYAMFBroadcastReceiver.EXTRA_SOURCE, "taskbar")
                }
                AndroidAppHelper.currentApplication().sendBroadcast(intent)
                return null
            }
        })
    }

    private fun getUserContext(): Context {
        return if (null != mUserContext) mUserContext!!
        else {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null)
            val application = activityThread.getMethod("getApplication").invoke(currentActivityThread) as Application
            mUserContext = application.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
            mUserContext!!
        }
    }

    fun Context.getActivity(): Activity? {
        if (this is Activity) return this
        if (this is ContextWrapper) return this.baseContext.getActivity()
        return null
    }
}