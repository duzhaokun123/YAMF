package io.github.duzhaokun123.yamf.overlay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.get
import io.github.duzhaokun123.yamf.overlay.OverlayService.Companion.toAction

open class OverlayWindow(val context: Context) {
    val root by lazy { FrameLayout(context) }

    lateinit var tag: String

    var isCreated = false
        private set

    var isDestroyed = false
        private set

    var isShowing = false
        private set

    var hasFocus = false
        private set

    var isTop = true

    @CallSuper
    open fun onCreate() {
        isDestroyed = false
        isCreated = true
    }

    @CallSuper
    open fun onShow() {
        root.visibility = View.VISIBLE
        isShowing = true
    }

    @CallSuper
    open fun onHide() {
        root.visibility = View.GONE
        isShowing = false
    }

    @CallSuper
    open fun onDestroy() {
        isDestroyed = true
    }

    open fun onIsMovingChanged(isMoving: Boolean) {}

    var contentView
        get() = root[0]
        set(value) {
            root.addView(value, 0)
        }

    fun setContentView(@LayoutRes resource: Int) {
        root.addView(LayoutInflater.from(context).inflate(resource, null), 0)
    }

    fun selfDestroy() {
        toAction().destroy()
    }

    fun getFocus() {
        toAction().getFocus()
        hasFocus = true
    }

    fun releaseFocus() {
        toAction().releaseFocus()
        hasFocus = false
    }
}