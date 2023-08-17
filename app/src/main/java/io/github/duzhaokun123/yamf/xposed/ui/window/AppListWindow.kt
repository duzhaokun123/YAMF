package io.github.duzhaokun123.yamf.xposed.ui.window

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.IPackageManagerHidden
import android.content.pm.PackageManagerHidden
import android.content.pm.UserInfo
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
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
import io.github.duzhaokun123.androidapptemplate.utils.runIO
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.databinding.ItemAppBinding
import io.github.duzhaokun123.yamf.databinding.WindowAppListBinding
import io.github.duzhaokun123.yamf.common.model.StartCmd
import io.github.duzhaokun123.yamf.common.onException
import io.github.duzhaokun123.yamf.common.resetAdapter
import io.github.duzhaokun123.yamf.xposed.utils.AppInfoCache
import io.github.duzhaokun123.yamf.xposed.services.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.componentName
import io.github.duzhaokun123.yamf.xposed.utils.getActivityInfoCompat
import io.github.duzhaokun123.yamf.xposed.utils.startActivity

@SuppressLint("ClickableViewAccessibility")
class AppListWindow(val context: Context, val displayId: Int? = null) {
    companion object {
        const val TAG = "YAMF_AppListWindow"
    }

    private lateinit var binding: WindowAppListBinding
    val users = mutableMapOf<Int, String>()
    var userId = 0
    var apps = emptyList<ActivityInfo>()
    var showApps = emptyList<ActivityInfo>()

    init {
        runCatching {
            binding = WindowAppListBinding.inflate(LayoutInflater.from(context))
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
        Instances.userManager.invokeMethodAs<List<UserInfo>>(
            "getUsers",
            args(true, true, true),
            argTypes(java.lang.Boolean.TYPE, java.lang.Boolean.TYPE, java.lang.Boolean.TYPE)
        )!!
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
            text ?: return@doOnTextChanged
            showApps = apps.filter { activityInfo ->
                text in activityInfo.packageName ||
                        AppInfoCache.getIconLabel(activityInfo).second.contains(text, true)
            }
            binding.rv.resetAdapter()
        }
    }

    private fun onSelectUser(userId: Int) {
        binding.pv.visibility = View.VISIBLE
        this.userId = userId
        binding.btnUser.text = users[userId]

        runIO {
            apps = (Instances.packageManager as PackageManagerHidden).queryIntentActivitiesAsUser(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }, 0, userId
            ).map {
                (Instances.iPackageManager as IPackageManagerHidden).getActivityInfoCompat(
                    ComponentName(it.activityInfo.packageName, it.activityInfo.name),
                    0, userId
                )
            }
            apps.forEach { activityInfo ->
                AppInfoCache.getIconLabel(activityInfo)
            }
            runMain {
                showApps = apps
                binding.etSearch.text.clear()
                binding.rv.resetAdapter()
                binding.pv.visibility = View.GONE
            }
        }
    }

    private fun close() {
        Instances.windowManager.removeView(binding.root)
    }

    inner class Adapter : BaseSimpleAdapter<ItemAppBinding>(context, ItemAppBinding::class.java) {
        override fun initViews(baseBinding: ItemAppBinding, position: Int) {
            val activityInfo = showApps[position]
            baseBinding.ll.setOnClickListener {
                if (displayId == null)
                    YAMFManager.createWindow(StartCmd(activityInfo.componentName, userId))
                else
                    startActivity(context, activityInfo.componentName, userId, displayId)
                close()
            }
        }

        override fun initData(baseBinding: ItemAppBinding, position: Int) {
            val activityInfo = showApps[position]
            val (icon, label) = AppInfoCache.getIconLabel(activityInfo)
            baseBinding.ivIcon.setImageDrawable(icon)
            baseBinding.tvLabel.text = label
        }

        override fun getItemCount() = showApps.size
    }
}