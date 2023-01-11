package io.github.duzhaokun123.yamf.xposed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
//import io.github.duzhaokun123.yamf.ui.overlay.AppWindow
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.Instances

object OpenInYAMFBroadcastReceiver : BroadcastReceiver() {
    const val ACTION_OPEN_IN_YAMF = "io.github.duzhaokun123.yamf.receiver.action.ACTION_OPEN_IN_YAMF"
    const val EXTRA_TASK_ID = "io.github.duzhaokun123.yamf.receiver.extra.EXTRA_TASK_ID"

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_OPEN_IN_YAMF -> {
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
                if (taskId == 0) {
                    TipUtil.showToast("bad taskid 0")
                } else {
                    YAMFManager.createWindowLocal {
                        runCatching {
                            Instances.activityTaskManager.moveRootTaskToDisplay(taskId, it)
                        }.onFailure {  t ->
                            if (t is Error) throw t
                            TipUtil.showToast("can't move task $taskId")
                        }
                    }
                }
            }
        }
    }
}