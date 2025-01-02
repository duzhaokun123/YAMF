package com.mja.reyamf.manager.ui.setting

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mja.reyamf.R
import com.mja.reyamf.common.gson
import com.mja.reyamf.databinding.ActivitySettingBinding
import com.mja.reyamf.manager.services.YAMFManagerProxy
import com.mja.reyamf.common.model.Config as YAMFConfig

class SettingActivity : AppCompatActivity() {

    private var _binding: ActivitySettingBinding? = null
    private val binding get() = _binding

    companion object {
        val flags = listOf(
            "VIRTUAL_DISPLAY_FLAG_PUBLIC",                          // 1 << 0
            "VIRTUAL_DISPLAY_FLAG_PRESENTATION",                    // 1 << 1
            "VIRTUAL_DISPLAY_FLAG_SECURE",                          // 1 << 2
            "VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY",                // 1 << 3
            "VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR",                     // 1 << 4
            "VIRTUAL_DISPLAY_FLAG_CAN_SHOW_WITH_INSECURE_KEYGUARD", // 1 << 5
            "VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH",                  // 1 << 6
            "VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT",            // 1 << 7
            "VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL",      // 1 << 8
            "VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS",  // 1 << 9
            "VIRTUAL_DISPLAY_FLAG_TRUSTED",                         // 1 << 10
            "VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP",               // 1 << 11
            "VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED",                 // 1 << 12
            "VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED",         // 1 << 13
        )
    }

    lateinit var config: YAMFConfig
    private val preference: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initData()
    }

    private fun initData() {
        binding?.apply {
            config = gson.fromJson(YAMFManagerProxy.configJson, YAMFConfig::class.java)
            etReduceDPI.setText(config.reduceDPI.toString())
            btnFlags.text = config.flags.toString()
            sColoerd.isChecked = config.coloredController
            sBackHome.isChecked = config.recentBackHome
            sShowIMEinWindow.isChecked = config.showImeInWindow
            etSizeH.setText(config.defaultWindowHeight.toString())
            etSizeW.setText(config.defaultWindowWidth.toString())
            sHookLauncherHookRecents.isChecked = config.hookLauncher.hookRecents
            sHookLauncherHookTaskbar.isChecked = config.hookLauncher.hookTaskbar
            sHookLauncherHookPopup.isChecked = config.hookLauncher.hookPopup
            sHookLauncherHookTransientTaskbar.isChecked = config.hookLauncher.hookTransientTaskbar
            sUseAppList.isChecked = preference.getBoolean("useAppList", true)
            sForceShowIME.isChecked = config.showForceShowIME

            btnSurface.text = when (config.surfaceView) {
                0 -> {
                    "Texture View"
                }
                1 -> {
                    "Surface View"
                }
                else -> {
                    Log.d("reYAMF", "surfaceView: " + config.surfaceView.toString())
                    "Unavailable"
                }
            }

            btnWindowsfy.text = when (config.windowfy) {
                0 -> {
                    "Move Task"
                }
                1 -> {
                    "Start Activity"
                }
                2 -> {
                    "Hybrid"
                }
                else -> {
                    Log.d("reYAMF", "windowfy: " + config.windowfy.toString())
                    "Unavailable"
                }
            }

            btnFlags.setOnClickListener {
                val checks = BooleanArray(flags.size) { i ->
                    config.flags and (1 shl i) != 0
                }
                MaterialAlertDialogBuilder(this@SettingActivity)
                    .setMultiChoiceItems(flags.toTypedArray(), checks) { _, i, c ->
                        checks[i] = c
                        btnFlags.text = checks.foldIndexed(0) { i, f, b ->
                            if (b)
                                f + (1 shl i)
                            else
                                f
                        }.toString()
                    }
                    .setPositiveButton("about") { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = "https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/DisplayManager.java".toUri()
                        })
                    }
                    .show()
            }
            btnWindowsfy.setOnClickListener {
                PopupMenu(this@SettingActivity, btnWindowsfy).apply {
                    listOf("Move Task", "Start Activity", "Hybrid").forEach { i ->
                        menu.add(i).setOnMenuItemClickListener {
                            btnWindowsfy.text = i
                            true
                        }
                    }
                }.show()
            }
            btnSurface.setOnClickListener {
                PopupMenu(this@SettingActivity, btnSurface).apply {
                    listOf("Texture View", "Surface View").forEach { i ->
                        menu.add(i).setOnMenuItemClickListener {
                            btnSurface.text = i
                            true
                        }
                    }
                }.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.apply {
            config.reduceDPI = etReduceDPI.text.toString().toIntOrNull() ?: config.reduceDPI
            config.flags = btnFlags.text.toString().toIntOrNull() ?: config.flags
            config.surfaceView = when (val surface = btnSurface.text.toString()) {
                "Texture View" -> {
                    0
                }
                "Surface View" -> {
                    1
                }
                else -> {
                    Log.d("YAMF", "surface value: $surface")
                    0
                }
            }

            config.windowfy = when (val window = btnWindowsfy.text.toString()) {
                "Move Task" -> {
                    0
                }
                "Start Activity" -> {
                    1
                }
                "Hybrid" -> {
                    2
                }
                else -> {
                    Log.d("reYAMF", "window value: $window")
                    0
                }
            }
            config.coloredController = sColoerd.isChecked
            config.recentBackHome = sBackHome.isChecked
            config.showImeInWindow = sShowIMEinWindow.isChecked
            config.defaultWindowHeight = etSizeH.text.toString().toIntOrNull() ?: config.defaultWindowHeight
            config.defaultWindowWidth = etSizeW.text.toString().toIntOrNull() ?: config.defaultWindowWidth
            config.hookLauncher.hookRecents = sHookLauncherHookRecents.isChecked
            config.hookLauncher.hookTaskbar = sHookLauncherHookTaskbar.isChecked
            config.hookLauncher.hookPopup = sHookLauncherHookPopup.isChecked
            config.hookLauncher.hookTransientTaskbar = sHookLauncherHookTransientTaskbar.isChecked
            config.showForceShowIME = sForceShowIME.isChecked
            preference.edit().putBoolean("useAppList", sUseAppList.isChecked).apply()
            YAMFManagerProxy.updateConfig(gson.toJson(config))
        }
    }
}