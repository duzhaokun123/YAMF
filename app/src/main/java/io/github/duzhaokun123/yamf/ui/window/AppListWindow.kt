package io.github.duzhaokun123.yamf.ui.window

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.UserInfo
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doOnTextChanged
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAs
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import io.github.duzhaokun123.androidapptemplate.bases.BaseSimpleAdapter
import io.github.duzhaokun123.yamf.databinding.ItemAppBinding
import io.github.duzhaokun123.yamf.databinding.WindowAppListBinding
import io.github.duzhaokun123.yamf.model.StartCmd
import io.github.duzhaokun123.yamf.utils.onException
import io.github.duzhaokun123.yamf.utils.resetAdapter
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil

@SuppressLint("ClickableViewAccessibility")
class AppListWindow(val context: Context, val displayId: Int? = null) {
    companion object {
        const val TAG = "YAMF_AppListWindow"
    }

    private lateinit var binding: WindowAppListBinding
    val users = mutableMapOf<Int, String>()
    var userId = 0
    var apps = emptyList<PackageInfo>()
    var showApps = emptyList<PackageInfo>()

    init {
        runCatching {
            binding =  WindowAppListBinding.inflate(LayoutInflater.from(context))
        }.onException { e ->
            Log.e(TAG, "new app list failed: ", e)
            TipUtil.showToast("new app list failed\nmay you forget reboot")
        }.onSuccess {
            doInit()
        }
    }
    fun doInit() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
        }
        binding.root.let { layout ->
            Instances.windowManager.addView(layout, params)
        }
        binding.root.setOnClickListener {
            close()
        }
        binding.mcv.setOnTouchListener { _, _ -> true }
        Instances.userManager.invokeMethodAs<List<UserInfo>>("getUsers", args(true, true, true), argTypes(java.lang.Boolean.TYPE, java.lang.Boolean.TYPE, java.lang.Boolean.TYPE))!!
            .filter { it.isProfile || it.isPrimary }
            .forEach {
                users[it.id] = it.name
            }
        binding.btnUser.setOnClickListener {
            PopupMenu(context, binding.btnUser).apply {
                users.forEach { (t, u) ->
                    menu.add(u).setOnMenuItemClickListener {
                        onSelectUser(t)
                        true
                    }
                }
            }.show()
        }
        onSelectUser(0)
        binding.rv.layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
        binding.rv.adapter = Adapter()

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            text?:return@doOnTextChanged
            showApps = apps.filter { text in it.packageName || Instances.packageManager.getApplicationLabel(Instances.packageManager.getApplicationInfo(it.packageName, 0)).contains(text, true) }
            binding.rv.resetAdapter()
        }
    }

    private fun onSelectUser(userId: Int) {
        this.userId = userId
        binding.btnUser.text = users[userId]

        val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Instances.iPackageManager.getInstalledPackages((PackageManager.GET_META_DATA).toLong(), userId)
        } else {
            Instances.iPackageManager.getInstalledPackages((PackageManager.GET_META_DATA), userId)
        }.list as List<PackageInfo>

        apps = installedPackages
            .filter { it.applicationInfo != null && it.applicationInfo.uid / 100000 == userId }
            .filter { Instances.packageManager.getLaunchIntentForPackage(it.packageName) != null }
        showApps = apps
        binding.etSearch.text.clear()
        binding.rv.resetAdapter()
    }

    private fun close() {
        Instances.windowManager.removeView(binding.root)
    }

    inner class Adapter : BaseSimpleAdapter<ItemAppBinding>(context, ItemAppBinding::class.java) {
        override fun initViews(baseBinding: ItemAppBinding, position: Int) {
            val packageInfo = showApps[position]
            baseBinding.ll.setOnClickListener {
                if (displayId == null)
                    YAMFManager.createWindowLocal(StartCmd(Instances.packageManager.getLaunchIntentForPackage(packageInfo.packageName)!!.component!!, userId))
                else
                    startActivity(context, Instances.packageManager.getLaunchIntentForPackage(packageInfo.packageName)!!.component!!, userId, displayId)
                close()
            }
        }

        override fun initData(baseBinding: ItemAppBinding, position: Int) {
            val packageInfo = showApps[position]
            baseBinding.ivIcon.setImageDrawable(Instances.packageManager.getApplicationIcon(packageInfo.packageName))
            baseBinding.tvLabel.text = Instances.packageManager.getApplicationLabel(Instances.packageManager.getApplicationInfo(packageInfo.packageName, 0))
        }

        override fun getItemCount() = showApps.size
    }
}