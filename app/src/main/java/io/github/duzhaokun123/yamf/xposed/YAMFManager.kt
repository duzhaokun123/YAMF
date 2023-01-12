package io.github.duzhaokun123.yamf.xposed

import android.content.Context
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Process
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.model.Config
import io.github.duzhaokun123.yamf.ui.window.AppWindow
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.utils.gson
import io.github.qauxv.ui.CommonContextWrapper
import java.io.File


class YAMFManager : IYAMFManager.Stub() {
    companion object {
        const val TAG = "YAMFManager"
        var instance: YAMFManager? = null
        lateinit var systemContext: Context
        val windowList = mutableListOf<Int>()
        lateinit var config: Config
        val configFile = File("/data/system/yamf.json")
        var openWindowCount = 0

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
        }

        fun removeWindow(id: Int) {
            windowList.remove(id)
        }

        fun isTop(id: Int) = windowList[0] == id

        fun moveToTop(id: Int) {
            windowList.remove(id)
            windowList.add(0, id)
        }

        fun createWindowLocal(onVirtualDisplayCreated: (id: Int) -> Unit) {
            AppWindow(CommonContextWrapper.createAppCompatContext(systemContext), config.densityDpi, config.flags) { id ->
                addWindow(id)
                onVirtualDisplayCreated(id)
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

    override fun createWindow(): Int {
        var r = 0
        runMain {
            createWindowLocal { r = it }
        }
        while (r == 0) {
            Thread.yield()
        }
        return r
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
        }
    }

    override fun getOpenCount(): Int {
        return openWindowCount
    }
}