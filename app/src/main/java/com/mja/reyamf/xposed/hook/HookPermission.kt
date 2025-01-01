package com.mja.reyamf.xposed.hook

import android.app.AndroidAppHelper
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.mja.reyamf.manager.services.YAMFManagerProxy
import com.mja.reyamf.xposed.utils.Instances.systemContext
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookPermission : IXposedHookLoadPackage {
    private var classLoader: ClassLoader? = null


    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName !="com.google.android.permissioncontroller") {
            return
        }

        XposedHelpers.findAndHookMethod(Application::class.java, "attach", Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                classLoader = (param.thisObject as Application).classLoader
                XposedBridge.log("Hooked into ${lpparam.packageName}")
                intentHook()
            }
        })
    }

    private fun intentHook() {
        try {

            XposedHelpers.findAndHookMethod(
                "com.android.permissioncontroller.permission.ui.GrantPermissionsActivity",
                classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {

                        val serviceIntent = Intent().apply {
                            setComponent(
                                ComponentName(
                                    "com.mja.reyamf",
                                    "com.mja.reyamf.manager.services.SidebarHiderService"
                                )
                            )
                            putExtra("act", "KILL")
                        }
                        AndroidAppHelper.currentApplication().startService(serviceIntent)
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                "com.android.permissioncontroller.permission.ui.GrantPermissionsActivity",
                classLoader,
                "onDestroy",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {

                        val serviceIntent = Intent().apply {
                            setComponent(
                                ComponentName(
                                    "com.mja.reyamf",
                                    "com.mja.reyamf.manager.services.SidebarHiderService"
                                )
                            )
                            putExtra("act", "LAUNCH")
                        }
                        AndroidAppHelper.currentApplication().startService(serviceIntent)
                    }
                }
            )
        } catch (e: Throwable) {
            XposedBridge.log(e)
        }
    }
}