package io.github.duzhaokun123.yamf.ui.window

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityTaskManager
import android.app.ITaskStackListener
import android.content.ComponentName
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.IRotationWatcher
import android.view.InputDevice
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.window.TaskSnapshot
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateLayoutParams
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.google.android.material.color.MaterialColors
import io.github.duzhaokun123.androidapptemplate.utils.getAttr
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.databinding.WindowAppBinding
import io.github.duzhaokun123.yamf.utils.RunMainThreadQueue
import io.github.duzhaokun123.yamf.xposed.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.log
import kotlinx.coroutines.delay

@SuppressLint("ClickableViewAccessibility")
class AppWindow(val context: Context, val densityDpi: Int, flags: Int, onVirtualDisplayCreated: ((Int) -> Unit)) :
    TextureView.SurfaceTextureListener, SurfaceHolder.Callback {
    companion object {
        const val TAG = "YAMF_AppWindow"
    }

    var binding: WindowAppBinding
    lateinit var virtualDisplay: VirtualDisplay
    val taskStackListener = TaskStackListener()
    val rotationWatcher = RotationWatcher()
    val surfaceOnTouchListener = SurfaceOnTouchListener()
    var displayId = -1
    var rotateLock = false
    var isMini = false
    var halfWidth = 0
    var halfHeight = 0
    lateinit var surfaceView: View

    init {
        when(YAMFManager.config.surfaceView) {
            0 -> surfaceView = TextureView(context)
            1 -> surfaceView = SurfaceView(context)
        }
        binding = WindowAppBinding.inflate(LayoutInflater.from(context))
        binding.rlCardRoot.addView(surfaceView.apply {
            id = R.id.surface
        }, RelativeLayout.LayoutParams(binding.vSizePreviewer.layoutParams).apply {
            addRule(RelativeLayout.BELOW, R.id.rl_top)
        })
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
        binding.root.let { layout ->
            layout.setOnTouchListener(MoveOnTouchListener())
            Instances.windowManager.addView(layout, params)
        }
        binding.ibResize.setOnTouchListener(object : View.OnTouchListener {
            var beginX = 0F
            var beginY = 0F
            var beginWidth = 0
            var beginHeight = 0

            var offsetX = 0F
            var offsetY = 0F

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        beginX = event.rawX
                        beginY = event.rawY
                        binding.vSizePreviewer.layoutParams.let {
                            beginWidth = it.width
                            beginHeight = it.height
                        }
                        binding.vSizePreviewer.visibility = View.VISIBLE
                    }
                    MotionEvent.ACTION_MOVE -> {
                        offsetX = event.rawX - beginX
                        offsetY = event.rawY - beginY
                        binding.vSizePreviewer.updateLayoutParams {
                            val targetWidth = beginWidth + offsetX.toInt()
                            if (targetWidth > 0)
                                width = targetWidth
                            val targetHeight = beginHeight + offsetY.toInt()
                            if (targetHeight > 0)
                                height = targetHeight
                            binding.tvSize.text = "${targetWidth}x$targetHeight"
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        surfaceView.updateLayoutParams {
                            val targetWidth = beginWidth + offsetX.toInt()
                            if (targetWidth > 0)
                                width = targetWidth
                            val targetHeight = beginHeight + offsetY.toInt()
                            if (targetHeight > 0)
                                height = targetHeight
                        }
                        binding.vSupporter.layoutParams = FrameLayout.LayoutParams(binding.vSizePreviewer.layoutParams)
                        binding.vSizePreviewer.visibility = View.GONE
                    }
                }
                return true
            }
        })
        surfaceView.setOnTouchListener(surfaceOnTouchListener)
        binding.ibBack.setOnClickListener {
            val down = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_BACK,
                0
            ).apply {
                source = InputDevice.SOURCE_KEYBOARD
                this.invokeMethod("setDisplayId", args(displayId), argTypes(Integer.TYPE))
            }
            Instances.inputManager.injectInputEvent(down, 0)
            val up = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_BACK,
                0
            ).apply {
                source = InputDevice.SOURCE_KEYBOARD
                this.invokeMethod("setDisplayId", args(displayId), argTypes(Integer.TYPE))
            }
            Instances.inputManager.injectInputEvent(up, 0)
        }
        binding.ibBack.setOnLongClickListener {
            val down = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_HOME,
                0
            ).apply {
                source = InputDevice.SOURCE_KEYBOARD
                this.invokeMethod("setDisplayId", args(displayId), argTypes(Integer.TYPE))
            }
            Instances.inputManager.injectInputEvent(down, 0)
            val up = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_HOME,
                0
            ).apply {
                source = InputDevice.SOURCE_KEYBOARD
                this.invokeMethod("setDisplayId", args(displayId), argTypes(Integer.TYPE))
            }
            Instances.inputManager.injectInputEvent(up, 0)
            true
        }
        binding.ibRotate.setOnClickListener {
            rotate(Surface.ROTATION_90)
        }
        binding.ibRotate.setOnLongClickListener {
            rotateLock = rotateLock.not()
            TipUtil.showToast("rotateLock: $rotateLock")
            true
        }
        binding.ibClose.setOnClickListener {
            onDestroy()
        }
        binding.ibClose.setOnLongClickListener {
            AppListWindow(context, displayId)
            true
        }
        binding.ibFullscreen.setOnClickListener {
            getTopRootTask()?.runCatching {
                Instances.activityTaskManager.moveRootTaskToDisplay(taskId, 0)
            }?.onFailure { t ->
                if (t is Error) throw t
                TipUtil.showToast("${t.message}")
            }?.onSuccess {
                binding.ibClose.callOnClick()
            }
        }
        binding.ibFullscreen.setOnLongClickListener {
            changeMini()
            true
        }
        virtualDisplay = Instances.displayManager.createVirtualDisplay("yamf${System.currentTimeMillis()}", 1080, 1920, densityDpi, null, flags)
        displayId = virtualDisplay.display.displayId
        Instances.activityTaskManager.registerTaskStackListener(taskStackListener)
        onVirtualDisplayCreated(displayId)
        (surfaceView as? TextureView)?.surfaceTextureListener = this
        (surfaceView as? SurfaceView)?.holder?.addCallback(this)
        var failCount = 0
        fun watchRotation() {
            runCatching {
                Instances.iWindowManager.watchRotation(rotationWatcher, displayId)
            }.onFailure {
                failCount++
                Log.d(TAG, "watchRotation: fail $failCount")
                watchRotation()
            }
        }
        watchRotation()
    }

    private fun onDestroy() {
        Instances.iWindowManager.removeRotationWatcher(rotationWatcher)
        Instances.activityTaskManager.unregisterTaskStackListener(taskStackListener)
        YAMFManager.removeWindow(displayId)
        virtualDisplay.release()
        Instances.windowManager.removeView(binding.root)
    }

    private fun getTopRootTask(): ActivityTaskManager.RootTaskInfo? {
        Instances.activityTaskManager.getAllRootTaskInfosOnDisplay(displayId).forEach { task ->
            if (task.visible)
                return task
        }
        return null
    }

    private fun moveToTop() {
        Instances.windowManager.removeView(binding.root)
        Instances.windowManager.addView(binding.root, binding.root.layoutParams)
        YAMFManager.moveToTop(displayId)
    }

    inner class MoveOnTouchListener : View.OnTouchListener {
        private var originalXPos = 0
        private var originalYPos = 0

        private var offsetX = 0f
        private var offsetY = 0f

        private var lastUp = 0L

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
//                    overlayWindow.onIsMovingChanged(true)

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

                    Instances.windowManager.updateViewLayout(v, params)
                }
                MotionEvent.ACTION_UP -> {
                    if (YAMFManager.isTop(displayId).not()) {
                        moveToTop()
                    }

                    if (isMini) {
                        if (System.currentTimeMillis() - lastUp < 500) {
                            changeMini()
                        }
                        lastUp = System.currentTimeMillis()
                    }
                }
            }
            return true
        }
    }

    private fun updateTask(taskInfo: ActivityManager.RunningTaskInfo) {
        RunMainThreadQueue.add {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 && taskInfo.isVisible.not()) {
                delay(500) // fixme: 使用能直接确定可见性的方法
            }
            val topActivity = taskInfo.topActivity ?: return@add
            val taskDescription = Instances.activityTaskManager.getTaskDescription(taskInfo.taskId) ?: return@add
            val icon = runCatching { taskDescription.icon }.getOrNull()
            if (icon == null) {
                binding.ivIcon.setImageDrawable(Instances.packageManager.getActivityIcon(topActivity))
            } else {
                binding.ivIcon.setImageBitmap(taskDescription.icon)
            }
            val label = taskDescription.label
            if (label == null) {
                binding.tvLabel.text = Instances.packageManager.getActivityInfo(topActivity, 0).loadLabel(Instances.packageManager)
            } else {
                binding.tvLabel.text = taskDescription.label
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && YAMFManager.config.coloredController) {
                val backgroundColor = taskDescription.backgroundColor
                binding.cvApp.setCardBackgroundColor(backgroundColor)

                val statusBarColor = taskDescription.statusBarColor
                binding.rlTop.setBackgroundColor(statusBarColor)
                val onStateBar = if (MaterialColors.isColorLight(ColorUtils.compositeColors(statusBarColor, backgroundColor)) xor ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)) {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimaryContainer).data
                } else {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimary).data
                }
                binding.tvLabel.setTextColor(onStateBar)
                binding.ibClose.imageTintList = ColorStateList.valueOf(onStateBar)

                val navigationBarColor = taskDescription.navigationBarColor
                binding.rlButton.setBackgroundColor(navigationBarColor)
                val onNavigationBar = if (MaterialColors.isColorLight(ColorUtils.compositeColors(navigationBarColor, backgroundColor)) xor ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)) {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimaryContainer).data
                } else {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimary).data
                }
                binding.ibBack.imageTintList = ColorStateList.valueOf(onNavigationBar)
                binding.ibRotate.imageTintList = ColorStateList.valueOf(onNavigationBar)
                binding.ibFullscreen.imageTintList = ColorStateList.valueOf(onNavigationBar)
                binding.ibResize.imageTintList = ColorStateList.valueOf(onNavigationBar)
            }
        }
    }

    inner class TaskStackListener : ITaskStackListener.Stub() {
        override fun onTaskStackChanged() {}
        override fun onActivityPinned(packageName: String?, userId: Int, taskId: Int, stackId: Int) {}
        override fun onActivityUnpinned() {}
        override fun onActivityRestartAttempt(task: ActivityManager.RunningTaskInfo?, homeTaskVisible: Boolean, clearedTask: Boolean, wasVisible: Boolean) {}
        override fun onActivityForcedResizable(packageName: String?, taskId: Int, reason: Int) {}
        override fun onActivityDismissingDockedTask() {}
        override fun onActivityLaunchOnSecondaryDisplayFailed(taskInfo: ActivityManager.RunningTaskInfo?, requestedDisplayId: Int) {}
        override fun onActivityLaunchOnSecondaryDisplayRerouted(taskInfo: ActivityManager.RunningTaskInfo?, requestedDisplayId: Int) {}
        override fun onTaskCreated(taskId: Int, componentName: ComponentName?) {}
        override fun onTaskRemoved(taskId: Int) {}
        override fun onTaskMovedToFront(taskInfo: ActivityManager.RunningTaskInfo) {
            if (taskInfo.getObject("displayId") == displayId) {
                updateTask(taskInfo)
            }
        }
        override fun onTaskDescriptionChanged(taskInfo: ActivityManager.RunningTaskInfo) {
            if (taskInfo.getObject("displayId") == displayId) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 && !taskInfo.isVisible){
                    return
                }
                updateTask(taskInfo)
            }
        }
        override fun onActivityRequestedOrientationChanged(taskId: Int, requestedOrientation: Int) {}
        override fun onTaskRemovalStarted(taskInfo: ActivityManager.RunningTaskInfo?) {}
        override fun onTaskProfileLocked(taskInfo: ActivityManager.RunningTaskInfo?) {}
        override fun onTaskSnapshotChanged(taskId: Int, snapshot: TaskSnapshot?) {}
        override fun onBackPressedOnTaskRoot(taskInfo: ActivityManager.RunningTaskInfo?) {}
        override fun onTaskDisplayChanged(taskId: Int, newDisplayId: Int) {}
        override fun onRecentTaskListUpdated() {}
        override fun onRecentTaskListFrozenChanged(frozen: Boolean) {}
        override fun onTaskFocusChanged(taskId: Int, focused: Boolean) {}
        override fun onTaskRequestedOrientationChanged(taskId: Int, requestedOrientation: Int) {}
        override fun onActivityRotation(displayId: Int) {}
        override fun onTaskMovedToBack(taskInfo: ActivityManager.RunningTaskInfo?) {}
        override fun onLockTaskModeChanged(mode: Int) {}
    }

    inner class RotationWatcher : IRotationWatcher.Stub() {
        override fun onRotationChanged(rotation: Int) {
            runMain {
                if (rotateLock.not())
                    rotate(rotation)
            }
        }
    }

    /**
     * 进行一个 rotation 度的旋转 而不是旋转到
     */
    fun rotate(rotation: Int) {
        if (rotation == 1 || rotation == 3) {
            val t = halfHeight
            halfHeight = halfWidth
            halfWidth = t
            val surfaceWidth = surfaceView.width
            val surfaceHeight = surfaceView.height
            binding.vSizePreviewer.updateLayoutParams {
                width = surfaceHeight
                height = surfaceWidth
            }
            surfaceView.updateLayoutParams {
                width = surfaceHeight
                height = surfaceWidth
            }
            binding.vSupporter.layoutParams = FrameLayout.LayoutParams(binding.vSizePreviewer.layoutParams)
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (isMini.not()) {
            virtualDisplay.resize(width, height, densityDpi)
            surface.setDefaultBufferSize(width, height)
            halfWidth = width % 2
            halfHeight = height % 2
        } else {
            virtualDisplay.resize(width * 2 + halfWidth, height * 2 + halfHeight, densityDpi)
            surface.setDefaultBufferSize(width * 2 + halfWidth, height * 2 + halfHeight)
        }
        virtualDisplay.surface = Surface(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        if (isMini.not()) {
            virtualDisplay.resize(width, height, densityDpi)
            surface.setDefaultBufferSize(width, height)
            halfWidth = width % 2
            halfHeight = height % 2
        } else {
            virtualDisplay.resize(width * 2 + halfWidth, height * 2 + halfHeight, densityDpi)
            surface.setDefaultBufferSize(width * 2 + halfWidth, height * 2 + halfHeight)
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    private fun changeMini() {
        if (surfaceView is SurfaceView) {
            TipUtil.showToast("can't scale SurfaceView")
            return
        }
        if (isMini) {
            isMini = false
            surfaceView.updateLayoutParams {
                width = virtualDisplay.display.width
                height = virtualDisplay.display.height
            }
            binding.vSupporter.updateLayoutParams {
                width = virtualDisplay.display.width
                height = virtualDisplay.display.height
            }
            binding.rlTop.visibility = View.VISIBLE
            binding.rlButton.visibility = View.VISIBLE
            surfaceView.setOnTouchListener(surfaceOnTouchListener)
        } else {
            isMini = true
            surfaceView.updateLayoutParams {
                width = virtualDisplay.display.width / 2
                height = virtualDisplay.display.height / 2
            }
            binding.vSupporter.updateLayoutParams {
                width = virtualDisplay.display.width / 2
                height = virtualDisplay.display.height / 2
            }
            binding.rlTop.visibility = View.GONE
            binding.rlButton.visibility = View.GONE
            surfaceView.setOnTouchListener(null)
        }
    }

    inner class SurfaceOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val pointerCoords: Array<MotionEvent.PointerCoords?> = arrayOfNulls(event.pointerCount)
            val pointerProperties: Array<MotionEvent.PointerProperties?> =
                arrayOfNulls(event.pointerCount)
            for (i in 0 until event.pointerCount) {
                val oldCoords = MotionEvent.PointerCoords()
                val pointerProperty = MotionEvent.PointerProperties()
                event.getPointerCoords(i, oldCoords)
                event.getPointerProperties(i, pointerProperty)
                pointerCoords[i] = oldCoords
                pointerCoords[i]!!.apply {
                    x = oldCoords.x
                    y = oldCoords.y
                }
                pointerProperties[i] = pointerProperty
            }

            val newEvent = MotionEvent.obtain(
                event.downTime,
                event.eventTime,
                event.action,
                event.pointerCount,
                pointerProperties,
                pointerCoords,
                event.metaState,
                event.buttonState,
                event.xPrecision,
                event.yPrecision,
                event.deviceId,
                event.edgeFlags,
                event.source,
                event.flags
            )
            newEvent.invokeMethod("setDisplayId", args(displayId), argTypes(Integer.TYPE))
            Instances.inputManager.injectInputEvent(newEvent, 0)
            newEvent.recycle()
            return true
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        virtualDisplay.surface = holder.surface
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        virtualDisplay.resize(width, height, densityDpi)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        virtualDisplay.surface = null
    }
}