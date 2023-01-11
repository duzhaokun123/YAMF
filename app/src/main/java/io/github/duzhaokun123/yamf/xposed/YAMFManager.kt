package io.github.duzhaokun123.yamf.xposed

import android.content.Context
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Process
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.ui.window.AppWindow
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.utils.TipUtils
import io.github.duzhaokun123.yamf.utils.runMain
import io.github.qauxv.ui.CommonContextWrapper


class YAMFManager : IYAMFManager.Stub() {
    companion object {
        const val TAG = "YAMFManager"
        var instance: YAMFManager? = null
        lateinit var systemContext: Context
        val windowList = mutableListOf<Int>()

        fun systemReady() {
            systemContext.registerReceiver(
                OpenInYAMFBroadcastReceiver, IntentFilter(
                    OpenInYAMFBroadcastReceiver.ACTION_OPEN_IN_YAMF))
            TipUtils.init(systemContext, "[YAMF] ")
            Instances.init(systemContext)
        }

        fun addWindow(id: Int) {
            windowList.add(0, id)
        }

        fun removeWindow(id: Int) {
            windowList.remove(id)
        }

        fun isTop(id: Int) = windowList[0] == id

        fun moveToTop(id: Int) {
            windowList.remove(id)
            windowList.add(0, id)
        }

        fun createWindowLocal(densityDpi: Int, flags: Int, onVirtualDisplayCreated: (id: Int) -> Unit) {
            AppWindow(CommonContextWrapper.createAppCompatContext(systemContext), densityDpi, flags) { id ->
                addWindow(id)
                onVirtualDisplayCreated(id)
            }
        }
    }

    init {
        instance = this
        log(TAG, "YAMF service initialized")
    }

    private val displayManager: DisplayManager by lazy { systemContext.getSystemService(DisplayManager::class.java) }
    private val displayMap = mutableMapOf<Int, VirtualDisplay>()

    override fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    override fun createWindow(densityDpi: Int, flags: Int, taskId: Int): Int {
        var r = 0
        runMain {
            createWindowLocal(densityDpi, flags) {
                r = it
            }
        }
        while (r == 0) {
            Thread.yield()
        }
        return r
    }

//    override fun createVirtualDisplay(
//        name: String,
//        width: Int,
//        height: Int,
//        densityDpi: Int,
//        surface: Surface?,
//        flags: Int
//    ): Int {
//        var displayId = 0
//        runMain {
//            val virtualDisplay = displayManager.createVirtualDisplay(name, width, height, densityDpi, surface, flags)
//            Log.d(TAG, "createVirtualDisplay: displayId: ${virtualDisplay.display.displayId}")
//            displayMap[virtualDisplay.display.displayId] = virtualDisplay
//            displayId = virtualDisplay.display.displayId
//        }
//        while (displayId == 0) {
//            Thread.yield()
//        }
//        return displayId
//    }
//
//    override fun resizeVirtualDisplay(id: Int, width: Int, height: Int, densityDpi: Int): Boolean {
//        var r: Boolean? = null
//        runMain {
//            val virtualDisplay = displayMap[id]
//            r = if (virtualDisplay == null)
//                false
//            else {
//                virtualDisplay.resize(width, height, densityDpi)
//                true
//            }
//        }
//        while (r == null) {
//            Thread.yield()
//        }
//        return r!!
//    }
//
//    override fun setVirtualDisplaySurface(id: Int, surface: Surface?): Boolean {
//        var r: Boolean? = null
//        runMain {
//            val virtualDisplay = displayMap[id]
//            r = if (virtualDisplay == null)
//                false
//            else {
//                virtualDisplay.surface = surface
//                true
//            }
//        }
//        while (r == null) {
//            Thread.yield()
//        }
//        return r!!
//    }
//
//    override fun releaseVirtualDisplay(id: Int): Boolean {
//        var r: Boolean? = null
//        runMain {
//            val virtualDisplay = displayMap[id]
//            r = if (virtualDisplay == null)
//                false
//            else {
//                virtualDisplay.release()
//                displayMap.remove(id)
//                true
//            }
//        }
//        while (r == null) {
//            Thread.yield()
//        }
//        return r!!
//    }
//
//    override fun releaseAll(): Boolean {
//        runMain {
//            displayMap.values.forEach {
//                it.release()
//            }
//            displayMap.clear()
//        }
//        return true
//    }
//
//    override fun getVirtualDisplayInfoS(id: Int): String {
//        return displayMap[id]?.display.toString()
//    }
//
//    override fun getVirtualDisplayIds(): IntArray {
//        return displayMap.keys.toIntArray()
//    }

//    override fun showOverlay() {
//        runMain {
//            wc.classLoader.loadClass("androidx.cardview.widget.CardView")
//        }
//    }
//
//    override fun showOverlay() {
//        runMain {
//            val wm = systemContext.getSystemService(WindowManager::class.java)
//            val params = WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
//                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
//                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
//                PixelFormat.TRANSLUCENT
//            ).apply {
//                gravity = Gravity.START or Gravity.TOP
//                x = 0
//                y = 0
//            }
//            wm.addView(LayoutInflater.from(wc).inflate(R.layout.window_app, null), params)
//        }
//    }
}