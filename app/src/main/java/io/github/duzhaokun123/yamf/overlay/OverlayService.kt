package io.github.duzhaokun123.yamf.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class OverlayService: Service() {
    companion object {
        private const val TAG = "OverlayService"
        private const val ACTION_SHOW_WINDOW =
            "com.duzhaokun123.yamf.overlaywindow.action.ACTION_SHOW_WINDOW"
        private const val ACTION_DESTROY_WINDOW =
            "com.duzhaokun123.yamf.overlaywindow.action.ACTION_DESTROY_WINDOW"
        private const val ACTION_STOP =
            "com.duzhaokun123.yamf.overlaywindow.action.ACTION_STOP"
        private const val ACTION_GET_FOCUS =
            "com.duzhaokun123.yamf.overlaywindow.action.ACTION_GET_FOCUS"
        private const val ACTION_RELEASE_FOCUS =
            "com.duzhaokun123.yamf.overlaywindow.action.ACTION_RELEASE_FOCUS"

        private const val EXTRA_TAG = "com.duzhaokun123.overlaywindow.extra.TAG"

        private lateinit var notification: Notification
        private var notificationId by Delegates.notNull<Int>()
        private val windowMap = mutableMapOf<String, OverlayWindow>()

        fun init(notificationId: Int, notification: Notification) {
            this.notificationId = notificationId
            this.notification = notification
        }

        fun addWindow(overlayWindow: OverlayWindow, tag: String): WindowAction {
            overlayWindow.tag = tag
            windowMap[tag] = overlayWindow
            return WindowAction(overlayWindow)
        }

        fun getWindow(tag: String?): OverlayWindow? {
            return windowMap[tag]
        }

        fun stop(context: Context) {
            context.startService(Intent(context, OverlayService::class.java).apply {
                action = ACTION_STOP
            })
        }

        fun OverlayWindow.toAction() = WindowAction(this)
    }

    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SHOW_WINDOW -> showWindow(it.getStringExtra(EXTRA_TAG))
                ACTION_DESTROY_WINDOW -> destroyWindow(it.getStringExtra(EXTRA_TAG))
                ACTION_STOP -> stop()
                ACTION_GET_FOCUS -> getFocus(it.getStringExtra(EXTRA_TAG))
                ACTION_RELEASE_FOCUS -> releaseFocus(it.getStringExtra(EXTRA_TAG))
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowMap.values.forEach {
            windowManager.removeView(it.root)
        }
        windowMap.clear()
    }

    private fun showWindow(tag: String?) {
        getWindow(tag)?.let {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = 0
                y = 0
            }
            it.root.let { layout ->
                layout.setOnTouchListener(MoveOnTouchListener(it))
                windowManager.addView(layout, params)
                windowMap.values.forEach { it2 ->
                    if (it2 != it)
                        it2.isTop = false
                }
            }
        }
    }

    private fun destroyWindow(tag: String?) {
        getWindow(tag)?.let {
            windowManager.removeView(it.root)
            windowMap.remove(tag)
        }
        if (windowMap.isEmpty()) {
            stop()
        }
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun getFocus(tag: String?) {
        getWindow(tag)?.let {
            val params = it.root.layoutParams as WindowManager.LayoutParams
            params.flags = params.flags and (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv())
            windowManager.updateViewLayout(it.root, params)
        }
    }

    private fun releaseFocus(tag: String?) {
        getWindow(tag)?.let {
            val params = it.root.layoutParams as WindowManager.LayoutParams
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(it.root, params)
        }
    }

    private fun moveTop(overlayWindow: OverlayWindow) {
            windowManager.removeView(overlayWindow.root)
            windowManager.addView(overlayWindow.root, overlayWindow.root.layoutParams)
        overlayWindow.isTop = true
            windowMap.values.forEach { it2 ->
                if (it2 != overlayWindow)
                    it2.isTop = false
            }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(notificationId, notification)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    inner class MoveOnTouchListener(private val overlayWindow: OverlayWindow) :
        View.OnTouchListener {
        private var originalXPos = 0
        private var originalYPos = 0

        private var offsetX = 0f
        private var offsetY = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    overlayWindow.onIsMovingChanged(true)

                    val x = event.rawX
                    val y = event.rawY

                    val layoutParams = v.layoutParams as WindowManager.LayoutParams

                    originalXPos = layoutParams.x
                    originalYPos = layoutParams.y

                    offsetX = x - originalXPos
                    offsetY = y - originalYPos
                }
                MotionEvent.ACTION_MOVE -> {
                    val x = event.rawX
                    val y = event.rawY

                    val params: WindowManager.LayoutParams = v.layoutParams as WindowManager.LayoutParams

                    val newX = (x - offsetX).toInt()
                    val newY = (y - offsetY).toInt()

                    if (newX == originalXPos && newY == originalYPos) {
                        return true
                    }

                    params.x = newX
                    params.y = newY

                    windowManager.updateViewLayout(v, params)
                }
                MotionEvent.ACTION_UP -> {
                    overlayWindow.onIsMovingChanged(false)
                    if (overlayWindow.isTop.not()) {
                        moveTop(overlayWindow)
                    }
                }
            }
            return true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    class WindowAction(private val window: OverlayWindow) {
        fun show() {
            if (window.isCreated.not()) {
                GlobalScope.launch(Dispatchers.Main) { window.onCreate() }
                window.context.startService(
                    Intent(window.context, OverlayService::class.java).apply {
                        action = ACTION_SHOW_WINDOW
                        putExtra(EXTRA_TAG, window.tag)
                    })
            }
            if (window.isShowing.not()) {
                GlobalScope.launch(Dispatchers.Main) { window.onShow() }
            }
        }

        fun hide() {
            if (window.isShowing) {
                GlobalScope.launch(Dispatchers.Main) { window.onHide() }
            }
        }

        fun destroy() {
            if (window.isDestroyed.not()) {
                window.context.startService(
                    Intent(window.context, OverlayService::class.java).apply {
                        action = ACTION_DESTROY_WINDOW
                        putExtra(EXTRA_TAG, window.tag)
                    })
                GlobalScope.launch(Dispatchers.Main) { window.onDestroy() }
            }
        }

        fun getFocus() {
            if (window.isDestroyed.not()) {
                window.context.startService(
                    Intent(window.context, OverlayService::class.java).apply {
                        action = ACTION_GET_FOCUS
                        putExtra(EXTRA_TAG, window.tag)
                    })
            }
        }

        fun releaseFocus() {
            if (window.isDestroyed.not()) {
                window.context.startService(
                    Intent(window.context, OverlayService::class.java).apply {
                        action = ACTION_RELEASE_FOCUS
                        putExtra(EXTRA_TAG, window.tag)
                    })
            }
        }
    }
}