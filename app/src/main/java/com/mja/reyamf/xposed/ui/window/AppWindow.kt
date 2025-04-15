package com.mja.reyamf.xposed.ui.window

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityTaskManager
import android.app.ITaskStackListener
import android.app.ITaskStackListenerProxy
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.IPackageManagerHidden
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
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
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManagerHidden
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.window.TaskSnapshot
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.flingAnimationOf
import androidx.wear.widget.RoundedDrawable
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.runOnMainThread
import com.google.android.material.color.MaterialColors
import com.mja.reyamf.R
import com.mja.reyamf.common.getAttr
import com.mja.reyamf.common.onException
import com.mja.reyamf.common.runMain
import com.mja.reyamf.databinding.WindowAppBinding
import com.mja.reyamf.xposed.services.YAMFManager
import com.mja.reyamf.xposed.services.YAMFManager.config
import com.mja.reyamf.xposed.utils.Instances
import com.mja.reyamf.xposed.utils.RunMainThreadQueue
import com.mja.reyamf.xposed.utils.TipUtil
import com.mja.reyamf.xposed.utils.animateAlpha
import com.mja.reyamf.xposed.utils.animateResize
import com.mja.reyamf.xposed.utils.animateScaleThenResize
import com.mja.reyamf.xposed.utils.byteBuddyStrategy
import com.mja.reyamf.xposed.utils.dpToPx
import com.mja.reyamf.xposed.utils.getActivityInfoCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.bytebuddy.ByteBuddy
import net.bytebuddy.android.AndroidClassLoadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.matcher.ElementMatchers
import java.io.File
import java.lang.reflect.Method
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt


@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
class AppWindow(
    val context: Context,
    private val flags: Int,
    private val onVirtualDisplayCreated: (Int) -> Unit
) :
    TextureView.SurfaceTextureListener, SurfaceHolder.Callback {
    companion object {
        const val TAG = "reYAMF_AppWindow"
        const val ACTION_RESET_ALL_WINDOW = "com.mja.reyamf.ui.window.action.ACTION_RESET_ALL_WINDOW"
    }

    lateinit var binding: WindowAppBinding
    private lateinit var virtualDisplay: VirtualDisplay
    private val taskStackListener =
        ITaskStackListenerProxy.newInstance(context.classLoader) { args, method ->
            when (method.name) {
                "onTaskMovedToFront" -> {
                    onTaskMovedToFront(args[0] as ActivityManager.RunningTaskInfo)
                }
                "onTaskDescriptionChanged" -> {
                    onTaskDescriptionChanged(args[0] as ActivityManager.RunningTaskInfo)
                }
            }
        }
    private val rotationWatcher = RotationWatcher()
    private val surfaceOnTouchListener = SurfaceOnTouchListener()
    private val surfaceOnGenericMotionListener = SurfaceOnGenericMotionListener()
    var displayId = -1
    var rotateLock = false
    var isMini = false
    var isCollapsed = false
    private var halfWidth = 0
    private var halfHeight = 0
    lateinit var surfaceView: View
    private var newDpi = calculateDpi(
        config.defaultWindowWidth, config.defaultWindowHeight,
        calculateScreenInches(config.defaultWindowWidth, config.defaultWindowHeight)
    ) - config.reduceDPI
    private var originalWidth: Int = 0
    private var originalHeight: Int = 0
    private var isResize: Boolean = true

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_RESET_ALL_WINDOW) {
                val lp = binding.root.layoutParams as WindowManager.LayoutParams
                lp.apply {
                    x = 0
                    y = 0
                }
                Instances.windowManager.updateViewLayout(binding.root, lp)
                val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, context.resources.displayMetrics).toInt()
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300F, context.resources.displayMetrics).toInt()
                binding.vSizePreviewer.updateLayoutParams {
                    this.width = width
                    this.height = height
                }
                binding.vSupporter.updateLayoutParams {
                    this.width = width
                    this.height = height
                }
                surfaceView.updateLayoutParams {
                    this.width = width
                    this.height = height
                }
            }
        }
    }

    init {
        runCatching {
            binding = WindowAppBinding.inflate(LayoutInflater.from(context))
        }.onException { e ->
            Log.e(TAG, "Failed to create new window, did you reboot?", e)
            TipUtil.showToast("Failed to create new window, did you reboot?")
        }.onSuccess {
            doInit()
        }
    }

    private fun doInit() {
        when(config.surfaceView) {
            0 -> surfaceView = TextureView(context)
            1 -> surfaceView = SurfaceView(context)
        }
        binding.rlCardRoot.addView(surfaceView.apply {
            id = R.id.surface
        }, 0, RelativeLayout.LayoutParams(binding.vSizePreviewer.layoutParams).apply {
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
//            this as WindowLayoutParamsHidden
//            privateFlags = privateFlags or WindowLayoutParamsHidden.PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY
        }
        binding.root.let { layout ->
            layout.setOnTouchListener { _, event ->
                moveGestureDetector.onTouchEvent(event)
                moveToTopIfNeed(event)
                true
            }
            Instances.windowManager.addView(layout, params)
        }

        binding.ibResize.setOnTouchListener(object : View.OnTouchListener {
            var beginX = 0F
            var beginY = 0F
            var beginWidth = 0
            var beginHeight = 0

            var offsetX = 0F
            var offsetY = 0F

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
                        surfaceView.updateLayoutParams {
                            val targetWidth = beginWidth + offsetX.toInt()
                            if (targetWidth > 0)
                                width = targetWidth
                            val targetHeight = beginHeight + offsetY.toInt()
                            if (targetHeight > 0)
                                height = targetHeight
                        }
                        binding.vSupporter.layoutParams = FrameLayout.LayoutParams(binding.vSizePreviewer.layoutParams)
                        binding.cvBackground.post {
                            originalWidth = binding.cvBackground.width
                            originalHeight = binding.cvBackground.height
                        }
                        binding.vSizePreviewer.visibility = View.GONE
                        moveToTopIfNeed(event)
                    }
                }
                return true
            }
        })
        surfaceView.setOnTouchListener(surfaceOnTouchListener)
        surfaceView.setOnGenericMotionListener(surfaceOnGenericMotionListener)
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
        binding.ibClose.setOnClickListener {
            isResize = false

            binding.cvappIcon.visibility = View.INVISIBLE
            animateAlpha(binding.ibClose, 1f, 0f)
            animateAlpha(binding.ibMinimize, 1f, 0f)
            animateAlpha(binding.ibFullscreen, 1f, 0f)
            animateAlpha(binding.ibBack, 1f, 0f)

            runBlocking {
                delay(200)
                runOnMainThread {
                    animateScaleThenResize(
                        binding.cvBackground,
                        1F, 1F,
                        0F, 0F,
                        0.5F, 0.5F,
                        0, 0
                    ) {
                        onDestroy()
                    }
                }
            }
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
        binding.ibMinimize.setOnClickListener {
            changeMini()
        }
        binding.ibMinimize.setOnLongClickListener {
            changeCollapsed()
            true
        }

        virtualDisplay = Instances.displayManager.createVirtualDisplay(
            "yamf${System.currentTimeMillis()}", config.defaultWindowWidth, config.defaultWindowHeight, newDpi-config.reduceDPI, null, flags
        )
        displayId = virtualDisplay.display.displayId
        (Instances.windowManager as WindowManagerHidden).setDisplayImePolicy(displayId, if (config.showImeInWindow) WindowManagerHidden.DISPLAY_IME_POLICY_LOCAL else WindowManagerHidden.DISPLAY_IME_POLICY_FALLBACK_DISPLAY)
        Instances.activityTaskManager.registerTaskStackListener(taskStackListener)
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
        context.registerReceiver(broadcastReceiver, IntentFilter(ACTION_RESET_ALL_WINDOW), Context.RECEIVER_EXPORTED)
        val width = config.defaultWindowWidth.dpToPx().toInt()
        val height = config.defaultWindowHeight.dpToPx().toInt()
        surfaceView.updateLayoutParams {
            this.width = width
            this.height = height
        }
        binding.vSizePreviewer.updateLayoutParams {
            this.width = width
            this.height = height
        }
        binding.vSupporter.layoutParams = FrameLayout.LayoutParams(binding.vSizePreviewer.layoutParams)
        onVirtualDisplayCreated(displayId)

        isResize = false
        binding.cvBackground.post {
            originalWidth = binding.cvBackground.width
            originalHeight = binding.cvBackground.height
            binding.cvBackground.visibility = View.VISIBLE

            binding.cvBackground.radius = config.windowRoundedCorner.dpToPx()
            binding.cvappIcon.radius = config.windowRoundedCorner.dpToPx()

            animateScaleThenResize(
                binding.cvBackground,
                0F, 0F,
                1F, 1F,
                0.5F, 0.5F,
                originalWidth, originalHeight
            ) {
                setBackgroundWrapContent()

                runBlocking {
                    delay(200)
                    runOnMainThread {
                        animateAlpha(binding.ibClose, 0f, 1f)
                        animateAlpha(binding.ibMinimize, 0f, 1f)
                        animateAlpha(binding.ibFullscreen, 0f, 1f)
                        animateAlpha(binding.ibBack, 0f, 1f)
                    }
                }

                isResize = true
            }
        }
    }

    private fun onDestroy() {
        context.unregisterReceiver(broadcastReceiver)
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

    private fun moveToTopIfNeed(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP && YAMFManager.isTop(displayId).not()) {
            moveToTop()
        }
    }

    private fun updateTask(taskInfo: ActivityManager.RunningTaskInfo) {
        RunMainThreadQueue.add {
            if (taskInfo.isVisible.not()) {
                delay(500) // fixme: use a method that directly determines visibility
            }

            var backgroundColor = 0
            var statusBarColor = 0
            var navigationBarColor = 0
            var taskDescription: ActivityManager.TaskDescription?

            if (Build.VERSION.SDK_INT < 35) {
                val topActivity = taskInfo.topActivity ?: return@add
                taskDescription = Instances.activityTaskManager.getTaskDescription(taskInfo.taskId) ?: return@add
                val activityInfo = (Instances.iPackageManager as IPackageManagerHidden).getActivityInfoCompat(topActivity, 0, taskInfo.getObjectAs("userId"))

                backgroundColor = taskDescription.backgroundColor
                statusBarColor = taskDescription.backgroundColor
                navigationBarColor = taskDescription.backgroundColor
                binding.appIcon.setImageDrawable(RoundedDrawable().apply {
                    drawable = runCatching { taskDescription.icon }.getOrNull()?.let { BitmapDrawable(it) } ?: activityInfo.loadIcon(Instances.packageManager)
                    isClipEnabled = true
                    radius = 100
                })
            } else {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val runningTasks = activityManager.getRunningTasks(5)

                for (task in runningTasks) {
                    if (task.taskId == taskInfo.taskId) {
                        val packageName = task.baseActivity?.packageName
                        try {
                            val packageManager = context.packageManager
                            backgroundColor = task.taskDescription!!.backgroundColor
                            statusBarColor = task.taskDescription!!.backgroundColor
                            navigationBarColor = task.taskDescription!!.backgroundColor
                            binding.appIcon.setImageDrawable(packageManager.getApplicationIcon(
                                packageName!!
                            ))
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            if (config.coloredController) {

                binding.cvApp.setCardBackgroundColor(backgroundColor)
                binding.rlTop.setBackgroundColor(statusBarColor)

                val onStateBar = if (MaterialColors.isColorLight(ColorUtils.compositeColors(statusBarColor, backgroundColor)) xor ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)) {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimaryContainer).data
                } else {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimary).data
                }

                binding.ibClose.imageTintList = ColorStateList.valueOf(onStateBar)
                binding.background.setBackgroundColor(navigationBarColor)
                binding.rlBottom.setBackgroundColor(navigationBarColor)

                val onNavigationBar = if (MaterialColors.isColorLight(ColorUtils.compositeColors(navigationBarColor, backgroundColor)) xor ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)) {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimaryContainer).data
                } else {
                    context.theme.getAttr(com.google.android.material.R.attr.colorOnPrimary).data
                }

                binding.ibBack.imageTintList = ColorStateList.valueOf(onNavigationBar)
                binding.ibMinimize.imageTintList = ColorStateList.valueOf(onNavigationBar)
                binding.ibFullscreen.imageTintList = ColorStateList.valueOf(onNavigationBar)
                binding.ibResize.imageTintList = ColorStateList.valueOf(onNavigationBar)
            }
        }
    }

    fun onTaskMovedToFront(taskInfo: ActivityManager.RunningTaskInfo) {
        if (taskInfo.getObject("displayId") == displayId) {
            updateTask(taskInfo)
        }
    }

    fun onTaskDescriptionChanged(taskInfo: ActivityManager.RunningTaskInfo) {
        if (taskInfo.getObject("displayId") == displayId) {
            if(!taskInfo.isVisible){
                return
            }
            updateTask(taskInfo)
        }
    }

    inner class RotationWatcher : IRotationWatcher.Stub() {
        override fun onRotationChanged(rotation: Int) {
            runMain {
                if (rotateLock.not())
                    rotate(rotation)
            }
        }
    }

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
        if (isMini.not() && isCollapsed.not()) {
            newDpi = calculateDpi(width, height, calculateScreenInches(width, height)) - config.reduceDPI
            virtualDisplay.resize(width, height, newDpi)
            surface.setDefaultBufferSize(width, height)
            halfWidth = width % 2
            halfHeight = height % 2
        } else {
            newDpi = calculateDpi(width, height, calculateScreenInches(width, height)) - config.reduceDPI
            virtualDisplay.resize(width * 2 + halfWidth, height * 2 + halfHeight, newDpi)
            surface.setDefaultBufferSize(width * 2 + halfWidth, height * 2 + halfHeight)
        }
        virtualDisplay.surface = Surface(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        if (isResize) {
            if (isMini.not()) {
                newDpi = calculateDpi(width, height, calculateScreenInches(width, height)) - config.reduceDPI
                virtualDisplay.resize(width, height, newDpi)
                surface.setDefaultBufferSize(width, height)
                halfWidth = width % 2
                halfHeight = height % 2
            } else {
                newDpi = calculateDpi(width, height, calculateScreenInches(width, height)) - config.reduceDPI
                virtualDisplay.resize(width * 2 + halfWidth, height * 2 + halfHeight, newDpi)
                surface.setDefaultBufferSize(width * 2 + halfWidth, height * 2 + halfHeight)
            }
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    // minimizes the floating window a bar-less only-content floating window
    private fun changeMini() {
        isCollapsed = false
        isResize = false

        if (isMini) {
            isMini = false
            isResize = true

            if (surfaceView is SurfaceView) {
                binding.cvBackground.updateLayoutParams {
                    width = originalWidth
                    height = originalHeight
                }
                setBackgroundWrapContent()
            } else {
                binding.cvBackground.updateLayoutParams {
                    width = originalWidth
                    height = originalHeight
                }
                animateScaleThenResize(
                    binding.cvBackground,
                    0.5F, 0.5F,
                    1F, 1F,
                    0F, 0F,
                    originalWidth, originalHeight
                ){
                    setBackgroundWrapContent()
                }
            }

            binding.rlTop.visibility = View.VISIBLE
            binding.rlBottom.visibility = View.VISIBLE
            surfaceView.visibility = View.VISIBLE
            surfaceView.setOnTouchListener(surfaceOnTouchListener)
            surfaceView.setOnGenericMotionListener(surfaceOnGenericMotionListener)

            return
        }
        else if (!isMini) {
            isMini = true

            if (config.surfaceView == 1) {
                binding.cvBackground.updateLayoutParams {
                    width = originalWidth/2
                    height = originalHeight/2
                }
            } else {
                animateResize(
                    binding.cvBackground,
                    originalWidth, originalWidth/2,
                    originalHeight, originalHeight/2
                ){
                    isResize = true
                }
            }

            binding.rlTop.visibility = View.GONE
            binding.rlBottom.visibility = View.GONE
            surfaceView.setOnTouchListener(null)
            surfaceView.setOnGenericMotionListener(null)

            return
        }
    }

    private fun setBackgroundWrapContent() {
        val layoutParams = binding.cvBackground.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.cvBackground.layoutParams = layoutParams
    }

    private fun changeCollapsed() {
        isResize = false
        if (isCollapsed) {
            expandWindow()
        } else {
            collapseWindow()
        }
    }

    private fun expandWindow() {
        isCollapsed = false
        binding.background.visibility = View.VISIBLE

        animateResize(binding.appIcon, 40.dpToPx().toInt(), 0, 40.dpToPx().toInt(), 0) {
            binding.cvappIcon.visibility = View.GONE
            animateResize(binding.cvBackground, 0, originalWidth, 0, originalHeight) {
                setBackgroundWrapContent()
                binding.cvappIcon.visibility = View.VISIBLE

                runBlocking {
                    delay(200)
                    runOnMainThread {
                        animateAlpha(binding.ibClose, 0f, 1f)
                        animateAlpha(binding.ibMinimize, 0f, 1f)
                        animateAlpha(binding.ibFullscreen, 0f, 1f)
                        animateAlpha(binding.ibBack, 0f, 1f)
                    }
                }

                binding.cvappIcon.visibility = View.GONE
                isResize = true
            }
        }
    }

    private fun collapseWindow() {
        isCollapsed = true

        animateAlpha(binding.ibClose, 1f, 0f)
        animateAlpha(binding.ibMinimize, 1f, 0f)
        animateAlpha(binding.ibFullscreen, 1f, 0f)
        animateAlpha(binding.ibBack, 1f, 0f)

        runBlocking {
            delay(200)
            runOnMainThread {
                animateResize(binding.cvBackground, binding.cvBackground.width, 0, binding.cvBackground.height, 0) {
                    binding.cvappIcon.visibility = View.VISIBLE
                    binding.background.visibility = View.GONE
                    animateResize(binding.appIcon, 0, 40.dpToPx().toInt(), 0, 40.dpToPx().toInt())

                    isResize = true
                }
            }
        }
    }

    private fun calculateScreenInches(width: Int, height: Int): Float {
        val x = (width / context.resources.displayMetrics.xdpi).pow(2)
        val y = (height / context.resources.displayMetrics.ydpi).pow(2)

        return sqrt(x + y)
    }

    private fun calculateDpi(width: Int, height: Int, screenSizeInInches: Float): Int {
        val widthSqr = width.toFloat().pow(2)
        val heightSqr = height.toFloat().pow(2)
        val diagonalPixels = sqrt(widthSqr + heightSqr)

        return floor(diagonalPixels / screenSizeInInches).toInt()
    }

    fun forwardMotionEvent(event: MotionEvent) {
        val pointerCords: Array<MotionEvent.PointerCoords?> = arrayOfNulls(event.pointerCount)
        val pointerProperties: Array<MotionEvent.PointerProperties?> =
            arrayOfNulls(event.pointerCount)
        for (i in 0 until event.pointerCount) {
            val oldCords = MotionEvent.PointerCoords()
            val pointerProperty = MotionEvent.PointerProperties()
            event.getPointerCoords(i, oldCords)
            event.getPointerProperties(i, pointerProperty)
            pointerCords[i] = oldCords
            pointerCords[i]!!.apply {
                x = oldCords.x
                y = oldCords.y
            }
            pointerProperties[i] = pointerProperty
        }

        val newEvent = MotionEvent.obtain(
            event.downTime,
            event.eventTime,
            event.action,
            event.pointerCount,
            pointerProperties,
            pointerCords,
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
    }

    inner class SurfaceOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            forwardMotionEvent(event)
            moveToTopIfNeed(event)
            return true
        }
    }

    inner class SurfaceOnGenericMotionListener : View.OnGenericMotionListener {
        override fun onGenericMotion(v: View, event: MotionEvent): Boolean {
            forwardMotionEvent(event)
            return true
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        virtualDisplay.surface = holder.surface
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        newDpi = calculateDpi(width, height, calculateScreenInches(width, height )) - config.reduceDPI
        virtualDisplay.resize(width, height, newDpi)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        virtualDisplay.surface = null
    }

    private val moveGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        var startX = 0
        var startY = 0
        var xAnimation: FlingAnimation? = null
        var yAnimation: FlingAnimation? = null
        var lastX = 0F
        var lastY = 0F
        var last2X = 0F
        var last2Y = 0F

        override fun onDown(e: MotionEvent): Boolean {
            xAnimation?.cancel()
            yAnimation?.cancel()
            val params = binding.root.layoutParams as WindowManager.LayoutParams
            startX = params.x
            startY = params.y
            return true
        }


        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            e1 ?: return false
            val params = binding.root.layoutParams as WindowManager.LayoutParams
            params.x = (startX + (e2.rawX - e1.rawX)).toInt()
            params.y = (startY + (e2.rawY - e1.rawY)).toInt()
            Instances.windowManager.updateViewLayout(binding.root, params)
            last2X = lastX
            last2Y = lastY
            lastX = e2.rawX
            lastY = e2.rawY
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            e1 ?: return false
            if (e1.source == InputDevice.SOURCE_MOUSE) return false
            val params = binding.root.layoutParams as WindowManager.LayoutParams

            runCatching {
                if (sign(velocityX) != sign(e2.rawX - last2X)) return@runCatching
                xAnimation = flingAnimationOf({
                    params.x = it.toInt()
                    Instances.windowManager.updateViewLayout(binding.root, params)
                }, {
                    params.x.toFloat()
                })
                    .setStartVelocity(velocityX)
                    .setMinValue(0F)
                    .setMaxValue(context.display.width.toFloat() - binding.root.width)
                xAnimation?.start()
            }
            runCatching {
                if (sign(velocityY) != sign(e2.rawY - last2Y)) return@runCatching
                yAnimation = flingAnimationOf({
                    params.y = it.toInt()
                    Instances.windowManager.updateViewLayout(binding.root, params)
                }, {
                    params.y.toFloat()
                })
                    .setStartVelocity(velocityY)
                    .setMinValue(0F)
                    .setMaxValue(context.display.height.toFloat() - binding.root.height)
                yAnimation?.start()
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (isMini && !isCollapsed) changeMini()
            else if (!isMini && isCollapsed) changeCollapsed()
            return true
        }
    })
}

