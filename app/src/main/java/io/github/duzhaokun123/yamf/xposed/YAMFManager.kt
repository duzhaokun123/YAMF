package io.github.duzhaokun123.yamf.xposed

import android.annotation.SuppressLint
import android.app.ActivityTaskManager
import android.content.Context
import android.content.IntentFilter
import android.os.Process
import de.robv.android.xposed.XSharedPreferences
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.model.StartCmd
import io.github.duzhaokun123.yamf.ui.window.AppListWindow
import io.github.duzhaokun123.yamf.ui.window.AppWindow
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.log
import io.github.qauxv.ui.CommonContextWrapper


class YAMFManager : IYAMFManager.Stub() {
    companion object {
        const val TAG = "YAMFManager"
        @SuppressLint("StaticFieldLeak")
        var instance: YAMFManager? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var systemContext: Context
        val windowList = mutableListOf<Int>()
        lateinit var config: XSharedPreferences
        var openWindowCount = 0
        val iOpenCountListenerSet = mutableSetOf<IOpenCountListener>()

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun systemReady() {
            systemContext.registerReceiver(
                OpenInYAMFBroadcastReceiver, IntentFilter(
                    OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF))
            TipUtil.init(systemContext, "[YAMF] ")
            Instances.init(systemContext)
            config = XSharedPreferences(BuildConfig.APPLICATION_ID, "yamf_config")
            log(TAG, "config: ${config.all.map { "${it.key}=${it.value}[${it.value?.javaClass?.name}]" }.joinToString() }")
//            config.registerOnSharedPreferenceChangeListener { _, _ ->
//                config.reload()
//                log(TAG, "config reload: ${config.all.map { "${it.key}=${it.value}[${it.value?.javaClass?.name}]" }.joinToString() }")
//            }
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
            AppWindow(CommonContextWrapper.createAppCompatContext(systemContext), config.getString("densityDpi", "200")!!.toInt(), config.getString("flags", "1668")!!.toInt()) { displayId ->
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

    override fun resetAllWindow() {
        runMain {
            Instances.iStatusBarService.collapsePanels()
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