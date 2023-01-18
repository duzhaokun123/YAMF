package io.github.duzhaokun123.yamf.xposed.utils

import android.app.IActivityTaskManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.input.IInputManager
import android.os.ServiceManager
import android.view.IWindowManager
import android.view.WindowManager
import androidx.core.content.getSystemService

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

    fun init(context: Context) {
        windowManager = context.getSystemService(WindowManager::class.java)
        iWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"))
        inputManager = IInputManager.Stub.asInterface(ServiceManager.getService("input"))
        displayManager = context.getSystemService(DisplayManager::class.java)
        activityTaskManager = IActivityTaskManager.Stub.asInterface(ServiceManager.getService("activity_task"))
        packageManager = context.packageManager
    }
}