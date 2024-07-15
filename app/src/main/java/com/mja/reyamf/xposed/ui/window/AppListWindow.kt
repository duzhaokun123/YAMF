package com.mja.reyamf.xposed.ui.window

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
import com.mja.reyamf.common.gson
import com.mja.reyamf.common.model.AppInfo
import com.mja.reyamf.common.model.FavApps
import com.mja.reyamf.common.model.StartCmd
import com.mja.reyamf.common.onException
import com.mja.reyamf.common.runIO
import com.mja.reyamf.common.runMain
import com.mja.reyamf.databinding.WindowAppListBinding
import com.mja.reyamf.manager.adapter.AppListAdapter
import com.mja.reyamf.manager.sidebar.SideBar
import com.mja.reyamf.xposed.services.YAMFManager
import com.mja.reyamf.xposed.services.YAMFManager.config
import com.mja.reyamf.xposed.services.YAMFManager.sideBarUpdateConfig
import com.mja.reyamf.xposed.utils.AppInfoCache
import com.mja.reyamf.xposed.utils.Instances
import com.mja.reyamf.xposed.utils.TipUtil
import com.mja.reyamf.xposed.utils.componentName
import com.mja.reyamf.xposed.utils.getActivityInfoCompat
import com.mja.reyamf.xposed.utils.log
import com.mja.reyamf.xposed.utils.startActivity
import com.mja.reyamf.xposed.utils.vibratePhone
import java.util.Locale

@SuppressLint("ClickableViewAccessibility")
class AppListWindow(val context: Context, private val displayId: Int? = null) {
    companion object {
        const val TAG = "reYAMF_AppListWindow"
    }

    private lateinit var binding: WindowAppListBinding
    private val users = mutableMapOf<Int, String>()
    var userId = 0
    private var apps = emptyList<ActivityInfo>()
    private var showApps: MutableList<AppInfo> = mutableListOf()
    private lateinit var rvAdapter: AppListAdapter

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

    private fun doInit() {
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

        log(SideBar.TAG, users.toString())
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
        val clickListener: (AppInfo) -> Unit = {
            if (displayId == null)
                YAMFManager.createWindow(StartCmd(it.componentName, userId))
            else
                startActivity(context, it.componentName, userId, displayId)
            close()
        }

        val longClickListener: (AppInfo) -> Unit = {
            config.favApps.add(
                FavApps(
                    it.componentName.packageName,
                    it.userId
                )
            )
            sideBarUpdateConfig(gson.toJson(config))
            vibratePhone(context)
            TipUtil.showToast("App added to sidebar")
        }

        binding.rv.layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
        rvAdapter = AppListAdapter(clickListener, arrayListOf(), longClickListener)
        binding.rv.adapter = rvAdapter
        rvAdapter.setData(showApps)
        rvAdapter.notifyDataSetChanged()

        onSelectUser(0)

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            text ?: return@doOnTextChanged
            val filteredApps = apps.filter { activityInfo ->
                text in activityInfo.packageName ||
                        AppInfoCache.getIconLabel(activityInfo).second.contains(text, true)
            }
            showApps.clear()
            filteredApps.forEach{ activityInfo ->
                val appInfoCache = AppInfoCache.getIconLabel(activityInfo)
                showApps.add(
                    AppInfo(
                        0, appInfoCache.first, appInfoCache.second, activityInfo.componentName, userId
                    )
                )
                showApps.sortBy { it.label.toString().lowercase(Locale.ROOT) }
            }
            rvAdapter.setData(showApps)
            rvAdapter.notifyDataSetChanged()
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
            showApps.clear()
            apps.forEach { activityInfo ->
                val appInfoCache = AppInfoCache.getIconLabel(activityInfo)
                showApps.add(
                    AppInfo(
                        0, appInfoCache.first, appInfoCache.second, activityInfo.componentName, userId
                    )
                )
            }

            showApps.sortBy { it.label.toString().lowercase(Locale.ROOT) }
            runMain {
                binding.etSearch.text.clear()
                rvAdapter.setData(showApps)
                rvAdapter.notifyDataSetChanged()
                binding.pv.visibility = View.GONE
            }
        }
    }

    private fun close() {
        Instances.windowManager.removeView(binding.root)
    }
}