package io.github.duzhaokun123.yamf.xposed.hook

import android.view.Display
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.duzhaokun123.yamf.xposed.utils.log

class HookSystemui : IXposedHookLoadPackage {
    companion object {
        const val TAG = "YAMF_HookSystemui"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return
        log(TAG, "hooking systemui")
        EzXHelperInit.initHandleLoadPackage(lpparam)
        findAllMethods("android.hardware.display.DisplayManager") {
            name == "getDisplay"
        }.hookAfter {
            if (it.result == null) {
                log(TAG, "getDisplay ${it.args[0]} return null")

            }
        }
    }
}