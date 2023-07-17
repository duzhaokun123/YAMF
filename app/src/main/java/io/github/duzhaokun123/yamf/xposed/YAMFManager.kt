package io.github.duzhaokun123.yamf.xposed

import android.annotation.SuppressLint
import android.app.ActivityTaskManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Process
import android.util.Log
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.model.Config
import io.github.duzhaokun123.yamf.model.StartCmd
import io.github.duzhaokun123.yamf.ui.window.AppListWindow
import io.github.duzhaokun123.yamf.ui.window.AppWindow
import io.github.duzhaokun123.yamf.utils.gson
import io.github.duzhaokun123.yamf.xposed.hook.HookLauncher
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.createContext
import io.github.duzhaokun123.yamf.xposed.utils.emptyContextParams
import io.github.duzhaokun123.yamf.xposed.utils.log
import io.github.qauxv.ui.CommonContextWrapper
import rikka.hidden.compat.ActivityManagerApis
import java.io.File


class YAMFManager : IYAMFManager.Stub() {
    companion object {
        const val TAG = "YAMFManager"
        const val ACTION_GET_LAUNCHER_CONFIG = "io.github.duzhaokun123.yamf.ACTION_GET_LAUNCHER_CONFIG"

        @SuppressLint("StaticFieldLeak")
        var instance: YAMFManager? = null
        val windowList = mutableListOf<Int>()
        lateinit var config: Config
        val configFile = File("/data/system/yamf.json")
        var openWindowCount = 0
        val iOpenCountListenerSet = mutableSetOf<IOpenCountListener>()
        lateinit var activityManagerService: Any

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun systemReady() {
            Instances.init(activityManagerService)
            Instances.systemContext.registerReceiver(
                OpenInYAMFBroadcastReceiver, IntentFilter(
                    OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF))
            Instances.systemContext.registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val task = getTopRootTask() ?: return
                        createWindowLocal(StartCmd(taskId = task.taskId))
                    }
                }, IntentFilter("io.github.duzhaokun123.yamf.action.CURRENT_TO_WINDOW")
            )
            Instances.systemContext.registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        AppListWindow(CommonContextWrapper.createAppCompatContext(Instances.systemUiContext.createContext()), null)
                    }
                }, IntentFilter("io.github.duzhaokun123.yamf.action.OPEN_APP_LIST")
            )
            Instances.systemContext.registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        ActivityManagerApis.broadcastIntent(Intent(HookLauncher.ACTION_RECEIVE_LAUNCHER_CONFIG).apply {
                            log(TAG, "send config: ${config.hookLauncher}")
                            putExtra("hookRecents", config.hookLauncher.hookRecents)
                            putExtra("hookTaskbar", config.hookLauncher.hookTaskbar)
                            `package` = intent.getStringExtra("sender")
                        }, 0)
                    }
                }, IntentFilter(ACTION_GET_LAUNCHER_CONFIG)
            )
            if (configFile.exists().not()) {
                configFile.parentFile!!.mkdirs()
                configFile.createNewFile()
            }
            runCatching {
                config = gson.fromJson(configFile.readText(), Config::class.java)
            }.onFailure {
                config = Config()
            }
            log(TAG, "config: $config")
        }

        fun addWindow(id: Int) {
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

        fun createWindowLocal(startCmd: StartCmd?) {
            Instances.iStatusBarService.collapsePanels()
            AppWindow(CommonContextWrapper.createAppCompatContext(Instances.systemUiContext.createContext()), config.densityDpi, config.flags) { displayId ->
                addWindow(displayId)
                startCmd?.startAuto(displayId)
            }
        }

        fun getTopRootTask(): ActivityTaskManager.RootTaskInfo? {
            Instances.activityTaskManager.getAllRootTaskInfosOnDisplay(0).forEach { task ->
                if (task.visible)
                    return task
            }
            return null
        }
    }

    init {
        instance = this
        log(TAG, "YAMF service initialized")
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
            createWindowLocal(null)
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
            AppListWindow(CommonContextWrapper.createAppCompatContext(Instances.systemUiContext.createContext()), null)
        }
    }

    override fun currentToWindow() {
        runMain {
            val task = getTopRootTask() ?: return@runMain
            createWindowLocal(StartCmd(taskId = task.taskId))
        }
    }

    override fun resetAllWindow() {
        runMain {
            Instances.iStatusBarService.collapsePanels()
        }
    }
}