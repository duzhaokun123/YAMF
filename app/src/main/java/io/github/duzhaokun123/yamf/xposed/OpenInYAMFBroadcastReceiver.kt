package io.github.duzhaokun123.yamf.xposed

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import io.github.duzhaokun123.yamf.utils.onException
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.moveToDisplay

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
                val componentName = intent.getParcelableExtra<ComponentName>(EXTRA_COMPONENT_NAME)?: return
                val userId = intent.getIntExtra(EXTRA_USER_ID, 0)

                if (taskId == 0) {
                    TipUtil.showToast("bad taskid 0")
                } else {
                    YAMFManager.createWindowLocal { displayId ->
                        moveToDisplay(context, taskId, componentName, userId, displayId)
                    }
                }
            }
        }
    }
}