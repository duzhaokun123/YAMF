package com.mja.reyamf.manager.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.mja.reyamf.BuildConfig
import com.mja.reyamf.R
import com.mja.reyamf.common.getAttr
import com.mja.reyamf.common.gson
import com.mja.reyamf.common.model.Config
import com.mja.reyamf.common.runMain
import com.mja.reyamf.databinding.ActivityMainBinding
import com.mja.reyamf.manager.services.YAMFManagerProxy
import com.mja.reyamf.manager.sidebar.SideBar
import com.mja.reyamf.manager.ui.setting.SettingActivity
import com.mja.reyamf.manager.utils.TipUtil
import com.mja.reyamf.xposed.IOpenCountListener
import com.mja.reyamf.xposed.utils.log

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding
    lateinit var config: Config

    companion object {
        const val TAG = "reYAMF_MainActivity"
    }

    private val openCountListener = object : IOpenCountListener.Stub() {
        override fun onUpdate(count: Int) {
            runMain {
                binding?.tvOpenCount?.text = count.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val typedValue = TypedValue()
        this@MainActivity.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)
        binding?.toolbar?.overflowIcon?.setTint(typedValue.data)

        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        YAMFManagerProxy.registerOpenCountListener(openCountListener)
        initUi()
    }

    private fun initUi() {
        config = gson.fromJson(YAMFManagerProxy.configJson, Config::class.java)
        val buildTime = YAMFManagerProxy.buildTime
        when(buildTime) {
            0L -> {
                binding?.apply {
                    ivIcon.setImageResource(R.drawable.ic_error_outline_24)
                    tvActive.setText(R.string.not_activated)
                    tvVersion.visibility = View.GONE
                    val colorError = theme.getAttr(com.google.android.material.R.attr.colorError).data
                    val colorOnError = theme.getAttr(com.google.android.material.R.attr.colorOnError).data
                    mcvStatus.setCardBackgroundColor(colorError)
                    mcvStatus.outlineAmbientShadowColor = colorError
                    mcvStatus.outlineSpotShadowColor = colorError
                    tvActive.setTextColor(colorOnError)
                    tvVersion.setTextColor(colorOnError)
                    mcvInfo.visibility = View.GONE
                    mcvSideBar.visibility = View.GONE
                }
            }
            BuildConfig.BUILD_TIME -> {
                binding?.apply {
                    ivIcon.setImageResource(R.drawable.ic_round_check_circle_24)
                    tvActive.setText(R.string.activated)
                    tvVersion.text = buildString {
                        append(YAMFManagerProxy.versionName)
                        append(" (${YAMFManagerProxy.versionCode})")
                    }
                }
            }
            else -> {
                binding?.apply {
                    ivIcon.setImageResource(R.drawable.ic_warning_amber_24)
                    tvActive.setText(R.string.need_reboot)
                    tvVersion.text = buildString {
                        append("system: ${YAMFManagerProxy.versionName} (${YAMFManagerProxy.versionCode})\n")
                        append("module: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    }
                    mcvStatus.setCardBackgroundColor(MaterialColors.harmonizeWithPrimary(this@MainActivity, getColor(R.color.color_warning)))
                    mcvStatus.setOnClickListener {
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle(R.string.need_reboot)
                            .setMessage(R.string.need_reboot_message)
                            .setPositiveButton(R.string.reboot) { _, _->
                                TipUtil.showTip(this@MainActivity, "do it yourself")
                            }
                            .show()
                    }
                    sliderTransparency.value = config.sidebarTransparency.toFloat()
                    tvTransparencyValue.text = "${config.sidebarTransparency}"
                }
            }
        }

        if (Build.VERSION.PREVIEW_SDK_INT != 0) {
            binding?.systemVersion?.text = buildString {
                append(Build.VERSION.CODENAME)
                append("Preview (API ${Build.VERSION.SDK_INT})")
            }
        } else {
            binding?.systemVersion?.text = buildString {
                append(Build.VERSION.RELEASE)
                append("(API ${Build.VERSION.SDK_INT})")
            }
        }
        binding?.tvBuildType?.text = BuildConfig.BUILD_TYPE

        binding?.apply {
            btLaunchSideBar.setOnClickListener {
                YAMFManagerProxy.launchSideBar()
            }

            msSideBar.isChecked = config.launchSideBarAtBoot
            msSideBar.setOnCheckedChangeListener { _, isChecked ->
                config.launchSideBarAtBoot = isChecked
                YAMFManagerProxy.updateConfig(gson.toJson(config))
            }

            ivDemo.let {
                Glide.with(this@MainActivity)
                    .load(R.drawable.demo)
                    .into(it)
            }

            if (config.enableSidebar) {
                innerClSidebar.visibility = View.VISIBLE
            } else {
                innerClSidebar.visibility = View.GONE
            }

            msEnableSideBar.isChecked = config.enableSidebar

            msEnableSideBar.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    innerClSidebar.visibility = View.VISIBLE
                } else {
                    innerClSidebar.visibility = View.GONE
                }

                config.enableSidebar = isChecked
                YAMFManagerProxy.updateConfig(gson.toJson(config))
            }

            sliderTransparency.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    tvTransparencyValue.text = "${slider.value.toInt()}"
                    config.sidebarTransparency = slider.value.toInt()
                    YAMFManagerProxy.updateConfig(gson.toJson(config))
                    YAMFManagerProxy.killSideBar()

                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            Log.d(SideBar.TAG, "updateConfig: restart")
                            YAMFManagerProxy.launchSideBar()
                        } catch (e: Exception) {
                            log(SideBar.TAG, "Failed restart sidebar")
                        }
                    }, 500)
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.new_window -> {
                YAMFManagerProxy.createWindow()
                true
            }
            R.id.channel -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://t.me/YAMF_channel".toUri()
                })
                true
            }
            R.id.open_app_list -> {
                YAMFManagerProxy.openAppList()
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.github -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://github.com/duzhaokun123/YAMF".toUri()
                })
                true
            }
            R.id.donate -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://duzhaokun123.github.io/donate.html".toUri()
                })
                true
            }
            R.id.current_to_window -> {
                YAMFManagerProxy.currentToWindow()
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        YAMFManagerProxy.unregisterOpenCountListener(openCountListener)
    }
}