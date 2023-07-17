package io.github.duzhaokun123.yamf.xposed.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.UserHandle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findConstructor
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.utils.newInstance
import com.github.kyuubiran.ezxhelper.utils.paramCount
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.xposed.OpenInYAMFBroadcastReceiver
import io.github.duzhaokun123.yamf.xposed.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.log
import java.lang.reflect.Proxy
import java.util.stream.Stream


class HookLauncher : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        const val TAG = "YAMF_HookLauncher"
        const val ACTION_RECEIVE_LAUNCHER_CONFIG = "io.github.duzhaokun123.yamf.ACTION_RECEIVE_LAUNCHER_CONFIG"
    }

    private var mUserContext: Context? = null

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        loadClassOrNull("com.android.launcher3.Launcher") ?: return
        Application::class.java.findMethod {
            name == "onCreate"
        }.hookAfter {
            val application = it.thisObject as Application
            application.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val hookRecents = intent.getBooleanExtra("hookRecents", false)
                    val hookTaskbar = intent.getBooleanExtra("hookTaskbar", false)
                    val hookPopup = intent.getBooleanExtra("hookPopup", false)
                    log(TAG, "receive config hookRecents=$hookRecents hookTaskbar=$hookTaskbar hookPopup=$hookPopup")
                    if (hookRecents) hookRecents(lpparam)
                    if (hookTaskbar) hookTaskbar(lpparam)
                    if (hookPopup) hookPopup(lpparam)
                    application.unregisterReceiver(this)
                }
            }, IntentFilter(ACTION_RECEIVE_LAUNCHER_CONFIG))
            application.sendBroadcast(Intent(YAMFManager.ACTION_GET_LAUNCHER_CONFIG).apply {
                `package` = "android"
                putExtra("sender", application.packageName)
            })
        }
    }

    private fun hookRecents(lpparam: XC_LoadPackage.LoadPackageParam) {
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

    var proxyClass: Any? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun handleProxyMethod(param: XC_MethodHook.MethodHookParam) {
        val methodName = param.method.name
        val thiz = param.thisObject
        when(methodName) {
            "onClick" -> {
                val mItemInfo = thiz.getObject("mItemInfo")
                val componentName = mItemInfo.invokeMethod("getTargetComponent") as ComponentName
                val userId = (mItemInfo.getObject("user") as UserHandle)
                AndroidAppHelper.currentApplication().sendBroadcast(Intent(YAMFManager.ACTION_OPEN_APP).apply {
                    setPackage("android")
                    putExtra(YAMFManager.EXTRA_COMPONENT_NAME, componentName)
                    putExtra(YAMFManager.EXTRA_USER_ID, userId)
                })
                thiz.invokeMethodAuto("dismissTaskMenuView", thiz.getObject("mTarget"))
                param.result = Unit
            }
            "setIconAndContentDescriptionFor" -> {
                val view = param.args[0] as ImageView
                view.setImageDrawable(InitFields.moduleRes.getDrawable(R.drawable.ic_picture_in_picture_alt_24, null))
                view.contentDescription = InitFields.moduleRes.getString(R.string.open_with_yamf)
                param.result = Unit
            }
            "setIconAndLabelFor" -> {
                val iconView = param.args[0] as View
                val labelView = param.args[1] as TextView
                iconView.background = InitFields.moduleRes.getDrawable(R.drawable.ic_picture_in_picture_alt_24, null)
                labelView.text = InitFields.moduleRes.getString(R.string.open_with_yamf)
                param.result = Unit
            }
        }
    }

    private fun hookPopup(lpparam: XC_LoadPackage.LoadPackageParam) {
        log(TAG, "hooking popup ${lpparam.packageName}")
        loadClass("com.android.launcher3.Launcher")
            .findMethod { name == "getSupportedShortcuts" }
            .hookAfter {
                val r = (it.result as Stream<*>).toArray()
                it.result = Stream.of(*r, getOpenInYAMFSystemShortcutFactory(lpparam.classLoader))
            }
        loadClass("com.android.launcher3.popup.SystemShortcut")
            .findAllMethods { true }
            .hookBefore {
                val thiz = it.thisObject
                if (thiz !== proxyClass) return@hookBefore
                handleProxyMethod(it)
            }
        loadClass("com.android.launcher3.popup.SystemShortcut\$Install")
            .findAllMethods { true }
            .hookBefore {
                val thiz = it.thisObject
                if (thiz !== proxyClass) return@hookBefore
                handleProxyMethod(it)
            }
    }

    fun getOpenInYAMFSystemShortcutFactory(classLoader: ClassLoader): Any {
        return Proxy.newProxyInstance(classLoader, arrayOf(loadClass("com.android.launcher3.popup.SystemShortcut\$Factory"))
        ) { _, method, args ->
            if (method.name != "getShortcut") return@newProxyInstance Unit
            return@newProxyInstance loadClass("com.android.launcher3.popup.SystemShortcut\$Install")
                .findConstructor { paramCount == 3 }
                .newInstance(args[0], args[1], args[2])
                .also { proxyClass = it }
        }
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