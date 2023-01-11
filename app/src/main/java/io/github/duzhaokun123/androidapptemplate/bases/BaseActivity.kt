package io.github.duzhaokun123.androidapptemplate.bases

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.duzhaokun123.androidapptemplate.utils.TipUtil
import io.github.duzhaokun123.androidapptemplate.utils.maxSystemBarsDisplayCutout
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.databinding.ActivityBaseRoot2Binding

abstract class BaseActivity<BaseBinding : ViewDataBinding>(
    @LayoutRes val layoutId: Int, vararg val configs: Config, @StyleRes val themeId: Int = R.style.Theme_YAMF
) : AppCompatActivity() {
    enum class Config {
        NO_TOOL_BAR,
        LAYOUT_NO_TOOL_BAR,
        TRANSPARENT_TOOL_BAR,
        NO_BACK,
        LAYOUT_MATCH_HORI,
    }

    val className by lazy { this::class.simpleName }
    val startIntent by lazy { intent }

    lateinit var rootBinding: ActivityBaseRoot2Binding
    lateinit var baseBinding: BaseBinding
        private set
    var isFirstCreate = true

    private val windowInsetsCompatModel by viewModels<WindowInsetsCompatModel>()

    class WindowInsetsCompatModel : ViewModel() {
        val windowInsetsCompat = MutableLiveData<WindowInsetsCompat>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isFirstCreate = savedInstanceState == null
        setTheme(themeId)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.attributes.layoutInDisplayCutoutMode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            else
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        rootBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_root_2)
        if (Config.NO_TOOL_BAR in configs) rootBinding.rootTb.visibility = View.GONE
        if (Config.LAYOUT_NO_TOOL_BAR in configs)
            rootBinding.rootFl.updateLayoutParams<RelativeLayout.LayoutParams> {
                removeRule(RelativeLayout.BELOW)
            }
        if (Config.TRANSPARENT_TOOL_BAR in configs) {
            rootBinding.rootAbl.outlineProvider = null
            rootBinding.rootAbl.background = null
        }
        ViewCompat.setOnApplyWindowInsetsListener(rootBinding.root) { _, insets ->
            windowInsetsCompatModel.windowInsetsCompat.value = insets
            insets
        }

        baseBinding = DataBindingUtil.inflate(layoutInflater, layoutId, rootBinding.rootFl, true)

        findViews()
        setSupportActionBar(initActionBar())
        if (Config.NO_BACK !in configs) supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        }
        initViews()
        initEvents()
        initData()

        TipUtil.registerCoordinatorLayout(this, registerCoordinatorLayout())
        windowInsetsCompatModel.windowInsetsCompat.observe(this, ::onApplyWindowInsetsCompat)
    }

    override fun onDestroy() {
        super.onDestroy()
        TipUtil.unregisterCoordinatorLayout(this)
    }

    override fun setTitle(title: CharSequence?) {
        supportActionBar?.title = title
    }

    fun setSubtitle(subtitle: CharSequence?) {
        supportActionBar?.subtitle = subtitle
    }

    fun setSubtitle(@StringRes subtitleId: Int) = setSubtitle(getText(subtitleId))

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else
            super.onOptionsItemSelected(item)
    }

    @CallSuper
    open fun onApplyWindowInsetsCompat(insets: WindowInsetsCompat) {
        with(insets.maxSystemBarsDisplayCutout) {
            if (Config.LAYOUT_MATCH_HORI !in configs) {
                rootBinding.rootAbl.updatePadding(left = left, right = right)
                rootBinding.rootFl.updatePadding(left = left, right = right)
            }
            if (Config.NO_TOOL_BAR !in configs)
                rootBinding.rootAbl.updatePadding(top = top)
        }
    }

    open fun registerCoordinatorLayout(): CoordinatorLayout? = rootBinding.rootCl

    open fun findViews() {}
    open fun initActionBar() =
        if (Config.NO_TOOL_BAR in configs) null else rootBinding.rootTb

    open fun initViews() {}
    open fun initEvents() {}
    open fun initData() {}
}