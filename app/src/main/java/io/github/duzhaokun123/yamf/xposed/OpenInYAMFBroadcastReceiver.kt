package io.github.duzhaokun123.yamf.xposed

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.newInstance
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import io.github.duzhaokun123.yamf.utils.runShell
//import io.github.duzhaokun123.yamf.ui.overlay.AppWindow
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import kotlinx.coroutines.delay

object OpenInYAMFBroadcastReceiver : BroadcastReceiver() {
    const val TAG = "YAMF_BR"
    
    const val ACTION_OPEN_IN_YAMF = "io.github.duzhaokun123.yamf.receiver.action.ACTION_OPEN_IN_YAMF"
    const val EXTRA_TASK_ID = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_TASK_ID"
    const val EXTRA_COMPONENT_NAME = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_COMPONENT_NAME"
    const val EXTRA_USER_ID = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_USER_ID"

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_OPEN_IN_YAMF -> {
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
                if (taskId == 0) {
                    TipUtil.showToast("bad taskid 0")
                } else {
                    YAMFManager.createWindowLocal { displayId ->
                        runCatching {
                            Instances.activityTaskManager.moveRootTaskToDisplay(taskId, displayId)
                        }.onFailure { t ->
                            if (t is Error) throw t
                            TipUtil.showToast("can't move task $taskId")
                            if (YAMFManager.config.tryStartActivity) {
                                TipUtil.showToast("try start activity")
                                    runCatching {
                                        startActivity(context, intent, displayId)
                                    }.onFailure {
                                        Log.e(TAG, "onReceive: start activity", it)
                                        TipUtil.showToast("can't start activity ${intent.getParcelableExtra(
                                            EXTRA_COMPONENT_NAME, ComponentName::class.java)}")
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startActivity(context: Context, intent: Intent, displayId: Int) {
        context.invokeMethod(
            "startActivityAsUser",
            args(
                Intent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    component =
                        intent.getParcelableExtra(EXTRA_COMPONENT_NAME, ComponentName::class.java)
                    `package` = component!!.packageName
                    action = Intent.ACTION_VIEW
                },
                ActivityOptions.makeBasic().apply {
                    launchDisplayId = displayId
                    this.invokeMethod("setCallerDisplayId", args(displayId), argTypes(Integer.TYPE))
                }.toBundle(),
                UserHandle::class.java.newInstance(
                    args(intent.getIntExtra(EXTRA_USER_ID, 0)),
                    argTypes(Integer.TYPE)
                )
            ), argTypes(Intent::class.java, Bundle::class.java, UserHandle::class.java)
        )
    }
}