package io.github.duzhaokun123.yamf.xposed.hook

import android.content.Context
import android.content.Intent
import android.content.pm.IPackageManager
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findAllConstructors
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.xposed.services.YAMFManager
import io.github.duzhaokun123.yamf.xposed.services.UserService
import io.github.duzhaokun123.yamf.xposed.utils.log
import io.github.qauxv.util.Initiator
import kotlin.concurrent.thread

class HookSystem : IXposedHookZygoteInit, IXposedHookLoadPackage {
    companion object {
        private const val TAG = "YAMF_HookSystem"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android") return
        log(TAG, "xposed init")
        log(TAG, "buildtype: ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}) ${BuildConfig.BUILD_TYPE}")
        EzXHelperInit.initHandleLoadPackage(lpparam)
        Initiator.init(lpparam.classLoader)

         var serviceManagerHook: XC_MethodHook.Unhook? = null
         serviceManagerHook = findMethod("android.os.ServiceManager") {
             name == "addService"
         }.hookBefore { param ->
             if (param.args[0] == "package") {
                 serviceManagerHook?.unhook()
                 val pms = param.args[1] as IPackageManager
                 log(TAG, "Got pms: $pms")
                 thread {
                     runCatching {
                         UserService.register(pms)
                         log(TAG, "UserService started")
                     }.onFailure {
                         log(TAG, "UserService failed to start", it)
                     }
                 }
             }
         }

         var activityManagerServiceSystemReadyHook: XC_MethodHook.Unhook? = null
         activityManagerServiceSystemReadyHook = findMethod("com.android.server.am.ActivityManagerService") {
             name == "systemReady"
         }.hookAfter {
             activityManagerServiceSystemReadyHook?.unhook()
             YAMFManager.activityManagerService = it.thisObject
             YAMFManager.systemReady()
             log(TAG, "system ready")
         }

        findMethod("com.android.server.am.ActivityManagerService") {
            name == "checkBroadcastFromSystem"
        }.hookBefore {
            val intent = it.args[0] as Intent
            if (intent.action == HookLauncher.ACTION_RECEIVE_LAUNCHER_CONFIG)
                it.result = Unit // bypass check
        }
    }
}