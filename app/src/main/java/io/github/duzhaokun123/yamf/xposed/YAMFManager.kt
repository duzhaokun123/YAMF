package io.github.duzhaokun123.yamf.xposed

import android.annotation.SuppressLint
import android.app.ActivityTaskManager
import android.content.Context
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
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.log
import io.github.qauxv.ui.CommonContextWrapper
import java.io.File


class YAMFManager : IYAMFManager.Stub() {
    companion object {
        const val TAG = "YAMFManager"
        @SuppressLint("StaticFieldLeak")
        var instance: YAMFManager? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var systemContext: Context
        val windowList = mutableListOf<Int>()
        lateinit var config: Config
        val configFile = File("/data/system/yamf.json")
        var openWindowCount = 0
        val iOpenCountListenerSet = mutableSetOf<IOpenCountListener>()

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun systemReady() {
            systemContext.registerReceiver(
                OpenInYAMFBroadcastReceiver, IntentFilter(
                    OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF))
            TipUtil.init(systemContext, "[YAMF] ")
            Instances.init(systemContext)
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
            AppWindow(CommonContextWrapper.createAppCompatContext(systemContext), config.densityDpi, config.flags) { displayId ->
                addWindow(displayId)
                startCmd?.startAuto(displayId)
            }
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
            AppListWindow(CommonContextWrapper.createAppCompatContext(systemContext), null)
        }
    }

    override fun currentToWindow() {
        runMain {
            val task = getTopRootTask() ?: return@runMain
            createWindowLocal(StartCmd(taskId = task.taskId))
        }
    }

    private fun getTopRootTask(): ActivityTaskManager.RootTaskInfo? {
        Instances.activityTaskManager.getAllRootTaskInfosOnDisplay(0).forEach { task ->
            if (task.visible)
                return task
        }
        return null
    }
}