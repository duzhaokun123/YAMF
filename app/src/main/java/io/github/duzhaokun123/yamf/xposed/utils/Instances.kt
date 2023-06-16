package io.github.duzhaokun123.yamf.xposed.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.IActivityTaskManager
import android.content.Context
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.input.IInputManager
import android.os.ServiceManager
import android.os.UserManager
import android.view.IWindowManager
import android.view.WindowManager
import com.android.internal.statusbar.IStatusBarService
import com.github.kyuubiran.ezxhelper.utils.getObjectAs

@SuppressLint("StaticFieldLeak")
object Instances {
    lateinit var windowManager: WindowManager
        private set
    lateinit var iWindowManager: IWindowManager
        private set
    lateinit var inputManager: IInputManager
        private set
    lateinit var displayManager: DisplayManager
        private set
    lateinit var activityTaskManager: IActivityTaskManager
        private set
    lateinit var packageManager: PackageManager
        private set
    lateinit var activityManager: ActivityManager
        private set
    lateinit var userManager: UserManager
        private set
    lateinit var iPackageManager: IPackageManager
        private set
    lateinit var iStatusBarService: IStatusBarService
        private set
    lateinit var activityManagerService: Any
        private set
    lateinit var systemContext: Context
        private set
    val systemUiContext: Context
        get() = activityManagerService.getObjectAs("mUiContext")


    fun init(activityManagerService: Any) {
        this.activityManagerService = activityManagerService
        systemContext = activityManagerService.getObjectAs("mContext")
        windowManager = systemContext.getSystemService(WindowManager::class.java)
        iWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"))
        inputManager = IInputManager.Stub.asInterface(ServiceManager.getService("input"))
        displayManager = systemContext.getSystemService(DisplayManager::class.java)
        activityTaskManager =
            IActivityTaskManager.Stub.asInterface(ServiceManager.getService("activity_task"))
        packageManager = systemContext.packageManager
        activityManager = systemContext.getSystemService(ActivityManager::class.java)
        userManager = systemContext.getSystemService(UserManager::class.java)
        iPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))
        iStatusBarService =
            IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"))
    }
}