package io.github.duzhaokun123.yamf.ui.window

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.UserInfo
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAs
import io.github.duzhaokun123.androidapptemplate.bases.BaseSimpleAdapter
import io.github.duzhaokun123.yamf.databinding.ItemAppBinding
import io.github.duzhaokun123.yamf.databinding.WindowAppListBinding
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.utils.Instances

@SuppressLint("ClickableViewAccessibility")
class AppListWindow(val context: Context, val displayId: Int) {
    companion object {
        const val TAG = "YAMF_AppListWindow"
    }

    private val binding: WindowAppListBinding = WindowAppListBinding.inflate(LayoutInflater.from(context))
    val users = mutableMapOf<Int, String>()
    var userId = 0
    var apps = emptyList<PackageInfo>()
    var showApps = emptyList<PackageInfo>()

    init {
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
        binding.rv.layoutManager = GridLayoutManager(context, 5)
        binding.rv.adapter = Adapter()

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            text?:return@doOnTextChanged
            showApps = apps.filter { text in it.packageName || Instances.packageManager.getApplicationLabel(Instances.packageManager.getApplicationInfo(it.packageName, 0)).contains(text, true) }
            binding.rv.adapter = binding.rv.adapter
        }
    }

    private fun onSelectUser(userId: Int) {
        this.userId = userId
        binding.btnUser.text = users[userId]
        apps = (Instances.iPackageManager.getInstalledPackages((PackageManager.GET_META_DATA).toLong(), userId).list as List<PackageInfo>)
            .filter { it.applicationInfo != null && it.applicationInfo.uid / 100000 == userId }
            .filter { Instances.packageManager.getLaunchIntentForPackage(it.packageName) != null }
        showApps = apps
        binding.etSearch.text.clear()
        binding.rv.adapter = binding.rv.adapter
    }

    private fun close() {
        Instances.windowManager.removeView(binding.root)
    }

    inner class Adapter : BaseSimpleAdapter<ItemAppBinding>(context, ItemAppBinding::class.java) {
        override fun initViews(baseBinding: ItemAppBinding, position: Int) {
            val packageInfo = showApps[position]
            baseBinding.ll.setOnClickListener {
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