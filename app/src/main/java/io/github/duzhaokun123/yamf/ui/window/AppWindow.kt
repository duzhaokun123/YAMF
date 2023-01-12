package io.github.duzhaokun123.yamf.ui.window

import android.annotation.SuppressLint
import android.app.ActivityTaskManager
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.os.SystemClock
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.databinding.WindowAppBinding
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@SuppressLint("ClickableViewAccessibility")
class AppWindow(context: Context, val densityDpi: Int, flags: Int, onVirtualDisplayCreated: ((Int) -> Unit)) :
    SurfaceHolder.Callback {
    companion object {
        const val TAG = "YAMF_AppWindow"
    }

    var binding: WindowAppBinding
    lateinit var virtualDisplay: VirtualDisplay
    var taskInfoUpdateJob: Job

    init {
        binding = WindowAppBinding.inflate(LayoutInflater.from(context))
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
//            OverlayService.windowMap.values.forEach { it2 ->
//                if (it2 != it)
//                    it2.isTop = false
//            }
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
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        binding.cvApp.updateLayoutParams {
                            val targetWidth = beginWidth + offsetX.toInt()
                            if (targetWidth > 0)
                                width = targetWidth
                            val targetHeight = beginHeight + offsetY.toInt()
                            if (targetHeight > 0)
                                height = targetHeight
                        }
                        binding.vSizePreviewer.visibility = View.GONE
                    }
                }
                return true
            }
        })
        binding.surface.setOnTouchListener { _, event ->
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
            newEvent.invokeMethod("setDisplayId", args(virtualDisplay.display.displayId), argTypes(Integer.TYPE))
            Instances.inputManager.injectInputEvent(newEvent, 0)
            newEvent.recycle()
            true
        }
        binding.ibBack.setOnClickListener {
            val down = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_BACK,
                0
            ).apply {
                source = InputDevice.SOURCE_KEYBOARD
                this.invokeMethod("setDisplayId", args(virtualDisplay.display.displayId), argTypes(Integer.TYPE))
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
                this.invokeMethod("setDisplayId", args(virtualDisplay.display.displayId), argTypes(Integer.TYPE))
            }
            Instances.inputManager.injectInputEvent(up, 0)
        }
        binding.ibRotate.setOnClickListener {
            val cardHeight = binding.cvApp.height
            val surfaceWidth = binding.surface.width
            val surfaceHeight = binding.surface.height
            binding.vSizePreviewer.updateLayoutParams {
                width = surfaceHeight
                height = surfaceWidth + cardHeight - surfaceHeight
            }
            binding.cvApp.updateLayoutParams {
                width = surfaceHeight
                height = surfaceWidth + cardHeight - surfaceHeight
            }
        }
//        binding.ibInfo.setOnClickListener {
//            MaterialAlertDialogBuilder(context)
//                .setTitle("info")
//                .setMessage("${getTopRootTask()}\n$virtualDisplay")
//                .show()
//                .findViewById<TextView>(android.R.id.message)
//                ?.setTextIsSelectable(true)
//        }
        binding.ibClose.setOnClickListener {
            onDestroy()
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
        virtualDisplay = Instances.displayManager.createVirtualDisplay("yamf${System.currentTimeMillis()}", 1080, 1920, densityDpi, null, flags)
        onVirtualDisplayCreated(virtualDisplay.display.displayId)
        binding.surface.holder.addCallback(this)
        taskInfoUpdateJob = runMain {
            while (true) {
                val task = getTopRootTask()
                if (task != null) {
                    val topActivity = task.topActivity ?: return@runMain
                    val taskDescription = Instances.activityTaskManager.getTaskDescription(task.childTaskIds.last())
                    val icon = taskDescription.icon
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
                    if (YAMFManager.config.coloredController) {
                        binding.rlTop.setBackgroundColor(taskDescription.statusBarColor)
                        binding.rlButton.setBackgroundColor(taskDescription.navigationBarColor)
                        binding.cvApp.setCardBackgroundColor(taskDescription.backgroundColor)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun onDestroy() {
        YAMFManager.removeWindow(virtualDisplay.display.displayId)
        taskInfoUpdateJob.cancel()
        virtualDisplay.release()
        Instances.windowManager.removeView(binding.root)
    }

    private fun getTopRootTask(): ActivityTaskManager.RootTaskInfo? {
        Instances.activityTaskManager.getAllRootTaskInfosOnDisplay(virtualDisplay.display.displayId).forEach { task ->
            if (task.visible)
                return task
        }
        return null
    }

    private fun moveToTop() {
        Instances.windowManager.removeView(binding.root)
        Instances.windowManager.addView(binding.root, binding.root.layoutParams)
        YAMFManager.moveToTop(virtualDisplay.display.displayId)
    }

    inner class MoveOnTouchListener : View.OnTouchListener {
        private var originalXPos = 0
        private var originalYPos = 0

        private var offsetX = 0f
        private var offsetY = 0f

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
                    if (YAMFManager.isTop(virtualDisplay.display.displayId).not()) {
                        moveToTop()
                    }
                }
            }
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