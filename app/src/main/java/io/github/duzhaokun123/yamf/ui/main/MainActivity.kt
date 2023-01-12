package io.github.duzhaokun123.yamf.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.duzhaokun123.androidapptemplate.bases.BaseActivity
import io.github.duzhaokun123.androidapptemplate.utils.TipUtil
import io.github.duzhaokun123.androidapptemplate.utils.getAttr
import io.github.duzhaokun123.yamf.BuildConfig
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.databinding.ActivityMainBinding
import io.github.duzhaokun123.yamf.ui.SettingsActivity
import io.github.duzhaokun123.yamf.xposed.YAMFManagerHelper

class MainActivity: BaseActivity<ActivityMainBinding>(R.layout.activity_main, Config.NO_BACK, Config.LAYOUT_MATCH_HORI),
    MenuProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addMenuProvider(this, this)
    }
    @SuppressLint("SetTextI18n")
    override fun initData() {
        when(YAMFManagerHelper.buildTime) {
            0L -> {
                baseBinding.ivIcon.setImageResource(R.drawable.ic_error_outline_24)
                baseBinding.tvActive.setText(R.string.not_activated)
                baseBinding.tvVersion.text = ""
                val colorError = theme.getAttr(com.google.android.material.R.attr.colorError).data
                val colorOnError = theme.getAttr(com.google.android.material.R.attr.colorOnError).data
                baseBinding.mcvStatus.setCardBackgroundColor(colorError)
                baseBinding.mcvStatus.outlineAmbientShadowColor = colorError
                baseBinding.mcvStatus.outlineSpotShadowColor = colorError
                baseBinding.tvActive.setTextColor(colorOnError)
                baseBinding.tvVersion.setTextColor(colorOnError)
            }
            BuildConfig.BUILD_TIME -> {
                baseBinding.ivIcon.setImageResource(R.drawable.ic_round_check_circle_24)
                baseBinding.tvActive.setText(R.string.activated)
                baseBinding.tvVersion.text = "${YAMFManagerHelper.versionName} (${YAMFManagerHelper.versionCode})"
            }
            else -> {
                baseBinding.ivIcon.setImageResource(R.drawable.ic_warning_amber_24)
                baseBinding.tvActive.setText(R.string.need_reboot)
                baseBinding.tvVersion.text = "system: ${YAMFManagerHelper.versionName} (${YAMFManagerHelper.versionCode})\n" +
                        "module: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                baseBinding.mcvStatus.setCardBackgroundColor(MaterialColors.harmonizeWithPrimary(this, getColor(R.color.color_warning)))
                baseBinding.mcvStatus.setOnClickListener {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.need_reboot)
                        .setMessage(R.string.need_reboot_message)
                        .setPositiveButton(R.string.reboot) { _, _->
                            TipUtil.showTip(this, "do it yourself")
                        }
                        .show()
                }
            }
        }
    }

    override fun initViews() {
        super.initViews()
//        LayoutInflater.from(this).inflate(R.layout.window_app, baseBinding.ll, true)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId) {
            R.id.new_window -> {
                YAMFManagerHelper.createWindow()
                true
            }
            R.id.channel -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://t.me/YAMF_channel".toUri()
                })
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> false
        }
    }
}