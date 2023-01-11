package io.github.duzhaokun123.androidapptemplate.bases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

abstract class BaseFragment<BaseBinding : ViewDataBinding>(@LayoutRes val layoutId: Int) : Fragment() {
    val className by lazy { this::class.simpleName }

    lateinit var baseBinding: BaseBinding
        private set
    var isFirstCreate = true
        private set

    private val windowInsetsCompatModel by activityViewModels<BaseActivity.WindowInsetsCompatModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        isFirstCreate = savedInstanceState == null
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        baseBinding = DataBindingUtil.inflate(layoutInflater, layoutId, null, false)
        findViews()
        initViews()
        initEvents()
        initData()
        windowInsetsCompatModel.windowInsetsCompat.observe(viewLifecycleOwner,::onApplyWindowInsetsCompat)
        return baseBinding.root
    }

    val baseActivity
        get() = activity as? BaseActivity<*>

    fun requireBaseActivity() =
        baseActivity
            ?: throw IllegalStateException("Fragment $this not attached to an baseActivity.")


    open fun findViews() {}
    open fun initViews() {}
    open fun initEvents() {}
    open fun initData() {}
    open fun onApplyWindowInsetsCompat(insets: WindowInsetsCompat) {}
}