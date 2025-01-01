package com.mja.reyamf.xposed.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.mja.reyamf.BuildConfig
import com.mja.reyamf.common.gson
import com.mja.reyamf.common.model.Config
import com.mja.reyamf.common.model.StartCmd
import com.mja.reyamf.common.runMain
import com.mja.reyamf.manager.sidebar.SideBar
import com.mja.reyamf.xposed.IOpenCountListener
import com.mja.reyamf.xposed.IYAMFManager
import com.mja.reyamf.xposed.hook.HookLauncher
import com.mja.reyamf.xposed.ui.window.AppListWindow
import com.mja.reyamf.xposed.ui.window.AppWindow
import com.mja.reyamf.xposed.utils.Instances
import com.mja.reyamf.xposed.utils.Instances.systemContext
import com.mja.reyamf.xposed.utils.Instances.systemUiContext
import com.mja.reyamf.xposed.utils.createContext
import com.mja.reyamf.xposed.utils.getTopRootTask
import com.mja.reyamf.xposed.utils.log
import com.mja.reyamf.xposed.utils.registerReceiver
import com.mja.reyamf.xposed.utils.startAuto
import com.qauxv.ui.CommonContextWrapper
import rikka.hidden.compat.ActivityManagerApis
import java.io.File


object YAMFManager : IYAMFManager.Stub() {
    private const val TAG = "reYAMFManager"

    const val ACTION_GET_LAUNCHER_CONFIG = "com.mja.reyamf.ACTION_GET_LAUNCHER_CONFIG"
    const val ACTION_OPEN_APP = "com.mja.reyamf.action.OPEN_APP"
    private const val ACTION_CURRENT_TO_WINDOW = "com.mja.reyamf.action.CURRENT_TO_WINDOW"
    private const val ACTION_OPEN_APP_LIST = "com.mja.reyamf.action.OPEN_APP_LIST"
    const val ACTION_OPEN_IN_YAMF = "com.mja.reyamf.ACTION_OPEN_IN_YAMF"
    private const val ACTION_LAUNCH_SIDE_BAR = "com.mja.reyamf.action.LAUNCH_SIDE_BAR"

    const val EXTRA_COMPONENT_NAME = "componentName"
    const val EXTRA_USER_ID = "userId"
    const val EXTRA_TASK_ID = "taskId"
    const val EXTRA_SOURCE = "source"

    private const val SOURCE_UNSPECIFIED = 0
    const val SOURCE_RECENT = 1
    const val SOURCE_TASKBAR = 2
    const val SOURCE_POPUP = 3

    private val windowList = mutableListOf<Int>()
    lateinit var config: Config
    private val configFile = File("/data/system/reYAMF.json")
    private var openWindowCount = 0
    private val iOpenCountListenerSet = mutableSetOf<IOpenCountListener>()
    lateinit var activityManagerService: Any
    var isSideBarRun = false
    var sidebarLayout: ConstraintLayout? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun systemReady() {
        Instances.init(activityManagerService)
        systemContext.registerReceiver(ACTION_OPEN_IN_YAMF, OpenInYAMFBroadcastReceiver)
        systemContext.registerReceiver(ACTION_CURRENT_TO_WINDOW) { _, _ ->
            currentToWindow()
        }
        systemContext.registerReceiver(ACTION_OPEN_APP_LIST) { _, _ ->
            AppListWindow(
                CommonContextWrapper.createAppCompatContext(systemUiContext.createContext()),
                null
            )
        }
        systemContext.registerReceiver(ACTION_OPEN_APP) { _, intent ->
            val componentName = intent.getParcelableExtra<ComponentName>(EXTRA_COMPONENT_NAME)
                ?: return@registerReceiver
            val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
            createWindow(StartCmd(componentName = componentName, userId = userId))
        }
        systemContext.registerReceiver(ACTION_GET_LAUNCHER_CONFIG) { _, intent ->
            ActivityManagerApis.broadcastIntent(Intent(HookLauncher.ACTION_RECEIVE_LAUNCHER_CONFIG).apply {
                log(TAG, "send config: ${config.hookLauncher}")
                putExtra(HookLauncher.EXTRA_HOOK_RECENT, config.hookLauncher.hookRecents)
                putExtra(HookLauncher.EXTRA_HOOK_TASKBAR, config.hookLauncher.hookTaskbar)
                putExtra(HookLauncher.EXTRA_HOOK_POPUP, config.hookLauncher.hookPopup)
                putExtra(HookLauncher.EXTRA_HOOK_TRANSIENT_TASKBAR, config.hookLauncher.hookTransientTaskbar)
                `package` = intent.getStringExtra("sender")
            }, 0)
        }
        systemContext.registerReceiver(ACTION_LAUNCH_SIDE_BAR) { _, _ ->
            SideBar(
                CommonContextWrapper.createAppCompatContext(systemUiContext.createContext()),
                null
            )
        }
        configFile.createNewFile()
        config = runCatching {
            gson.fromJson(configFile.readText(), Config::class.java)
        }.getOrNull() ?: Config()

        log(TAG, "config: $config")
    }

    private fun addWindow(id: Int) {
        windowList.add(0, id)
        openWindowCount++
        val toRemove = mutableSetOf<IOpenCountListener>()
        iOpenCountListenerSet.forEach {
            runCatching {
                it.onUpdate(openWindowCount)
            }.onFailure { _ ->
                toRemove.add(it)
            }
        }
        iOpenCountListenerSet.removeAll(toRemove)
    }

    fun removeWindow(id: Int) {
        windowList.remove(id)
    }

    fun isTop(id: Int) = windowList[0] == id

    fun moveToTop(id: Int) {
        windowList.remove(id)
        windowList.add(0, id)
    }

    fun createWindow(startCmd: StartCmd?) {
        Instances.iStatusBarService.collapsePanels()
        AppWindow(
            CommonContextWrapper.createAppCompatContext(systemUiContext.createContext()),
            config.flags
        ) { displayId ->
            addWindow(displayId)
            startCmd?.startAuto(displayId)
        }
    }

    fun restartSideBar(sidebar: ConstraintLayout, duration: Long) {
        sidebarLayout = null
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                Log.d(TAG, "updateConfig: restart")
                launchSideBar()
            } catch (e: Exception) {
                log(SideBar.TAG, "Failed restart sidebar")
            }

        }, duration)
        Instances.windowManager.removeView(sidebar)
    }

    fun sideBarUpdateConfig(newConfig: String) {
        config = gson.fromJson(newConfig, Config::class.java)
        runMain {
            configFile.writeText(newConfig)
            Log.d(TAG, "updateConfig: $config")
        }
    }

    init {
        log(TAG, "reYAMF service initialized")
    }

    override fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    override fun createWindow() {
        runMain {
            createWindow(null)
        }
    }

    override fun getBuildTime(): Long {
        return BuildConfig.BUILD_TIME
    }

    override fun getConfigJson(): String {
        return gson.toJson(config)
    }

    override fun updateConfig(newConfig: String) {
        config = gson.fromJson(newConfig, Config::class.java)
        runMain {
            configFile.writeText(newConfig)
            Log.d(TAG, "updateConfig: $config")
        }
    }

    override fun registerOpenCountListener(iOpenCountListener: IOpenCountListener) {
        iOpenCountListenerSet.add(iOpenCountListener)
        iOpenCountListener.onUpdate(openWindowCount)
    }

    override fun unregisterOpenCountListener(iOpenCountListener: IOpenCountListener?) {
        iOpenCountListenerSet.remove(iOpenCountListener)
    }

    override fun openAppList() {
        runMain {
            Instances.iStatusBarService.collapsePanels()
            AppListWindow(
                CommonContextWrapper.createAppCompatContext(systemUiContext.createContext()),
                null
            )
        }
    }

    override fun currentToWindow() {
        runMain {
            val task = getTopRootTask(0) ?: return@runMain

            if (task.baseActivity?.packageName != "com.android.launcher3") {
                createWindow(StartCmd(taskId = task.taskId))
            }
        }
    }

    override fun resetAllWindow() {
        runMain {
            Instances.iStatusBarService.collapsePanels()
            systemContext.sendBroadcast(Intent(AppWindow.ACTION_RESET_ALL_WINDOW))
        }
    }

    override fun killSideBar() {
        if (isSideBarRun && sidebarLayout != null) {
            isSideBarRun = false
            Instances.windowManager.removeView(sidebarLayout)
        }
    }

    override fun launchSideBar() {
        runMain {
            SideBar(
                CommonContextWrapper.createAppCompatContext(systemUiContext.createContext()),
                null
            )
        }
    }

    private val OpenInYAMFBroadcastReceiver: BroadcastReceiver.(Context, Intent) -> Unit =
        { _: Context, intent: Intent ->
            val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
            val componentName = intent.getParcelableExtra<ComponentName>(EXTRA_COMPONENT_NAME)
            val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
            val source = intent.getIntExtra(EXTRA_SOURCE, SOURCE_UNSPECIFIED)
            createWindow(StartCmd(componentName, userId, taskId))

            // TODO: better way to close recents
            if (source == SOURCE_RECENT && config.recentBackHome) {
                val down = KeyEvent(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_HOME,
                    0
                ).apply {
                    this.source = InputDevice.SOURCE_KEYBOARD
                    this.invokeMethod("setDisplayId", args(0), argTypes(Integer.TYPE))
                }
                Instances.inputManager.injectInputEvent(down, 0)
                val up = KeyEvent(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_HOME,
                    0
                ).apply {
                    this.source = InputDevice.SOURCE_KEYBOARD
                    this.invokeMethod("setDisplayId", args(0), argTypes(Integer.TYPE))
                }
                Instances.inputManager.injectInputEvent(up, 0)
            }
        }
}