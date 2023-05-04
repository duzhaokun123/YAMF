package io.github.duzhaokun123.yamf.xposed

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyEvent
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import io.github.duzhaokun123.yamf.model.StartCmd
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil

object OpenInYAMFBroadcastReceiver : BroadcastReceiver() {
    const val TAG = "YAMF_BR"
    
    const val ACTION_OPEN_IN_YAMF = "io.github.duzhaokun123.yamf.receiver.action.ACTION_OPEN_IN_YAMF"
    const val EXTRA_TASK_ID = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_TASK_ID"
    const val EXTRA_COMPONENT_NAME = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_COMPONENT_NAME"
    const val EXTRA_USER_ID = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_USER_ID"
    const val EXTRA_SOURCE = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_SOURCE"

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_OPEN_IN_YAMF -> {
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
                val componentName = intent.getParcelableExtra<ComponentName>(EXTRA_COMPONENT_NAME)
                val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
                val source = intent.getStringExtra(EXTRA_SOURCE)
                YAMFManager.createWindowLocal(StartCmd(componentName, userId, taskId))

                if (source == "recents" && YAMFManager.config.recentsBackHome) {
                    val down = KeyEvent(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_HOME,
                        0
                    ).apply {
                        this.source = InputDevice.SOURCE_KEYBOARD
                        this.invokeMethod("setDisplayId", args(0), argTypes(Integer.TYPE))
                    }
                    Instances.inputManager.injectInputEvent(down, 0)
                    val up = KeyEvent(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_HOME,
                        0
                    ).apply {
                        this.source = InputDevice.SOURCE_KEYBOARD
                        this.invokeMethod("setDisplayId", args(0), argTypes(Integer.TYPE))
                    }
                    Instances.inputManager.injectInputEvent(up, 0)
                }
            }
        }
    }
}