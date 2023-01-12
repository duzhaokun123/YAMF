package io.github.duzhaokun123.yamf.ui

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import io.github.duzhaokun123.yamf.R
import io.github.duzhaokun123.yamf.model.Config
import io.github.duzhaokun123.yamf.utils.gson
import io.github.duzhaokun123.yamf.xposed.YAMFManagerHelper

class SettingsActivity: AppCompatActivity() {
    lateinit var config: Config
    lateinit var etDensityDpi: EditText
    lateinit var etFlags: EditText
    lateinit var sColored: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        config = gson.fromJson(YAMFManagerHelper.configJson, Config::class.java)
        etDensityDpi = findViewById(R.id.et_densityDpi)
        etFlags = findViewById(R.id.et_flags)
        sColored = findViewById(R.id.s_coloerd)

        etDensityDpi.setText(config.densityDpi.toString())
        etFlags.setText(config.flags.toString())
        sColored.isChecked = config.coloredController
    }

    override fun onDestroy() {
        super.onDestroy()
        config.densityDpi = etDensityDpi.text.toString().toIntOrNull() ?: config.densityDpi
        config.flags = etFlags.text.toString().toIntOrNull() ?: config.flags
        config.coloredController = sColored.isChecked
        YAMFManagerHelper.updateConfig(gson.toJson(config))
    }
}