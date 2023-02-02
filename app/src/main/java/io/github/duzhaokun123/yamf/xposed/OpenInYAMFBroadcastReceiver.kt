package io.github.duzhaokun123.yamf.xposed

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.duzhaokun123.yamf.utils.onException
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil

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
                val componentName = intent.getParcelableExtra(EXTRA_COMPONENT_NAME, ComponentName::class.java) ?: return
                val userId = intent.getIntExtra(EXTRA_USER_ID, 0)

                if (taskId == 0) {
                    TipUtil.showToast("bad taskid 0")
                } else {
                    YAMFManager.createWindowLocal { displayId ->
                        when (YAMFManager.config.windowfy) {
                            0 -> {
                                runCatching {
                                    moveTask(taskId, displayId)
                                }.onException {
                                    TipUtil.showToast("can't move task $taskId")
                                }
                            }
                            1 -> {
                                runCatching {
                                    startActivity(context, componentName, userId, displayId)
                                }.onException {
                                    TipUtil.showToast("can't start activity $componentName")
                                }
                            }
                            2 -> {
                                runCatching {
                                    moveTask(taskId, displayId)
                                }.onException {
                                    TipUtil.showToast("can't move task $taskId")
                                    runCatching {
                                        startActivity(context, componentName, userId, displayId)
                                    }.onException {
                                        TipUtil.showToast("can't start activity $componentName")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveTask(taskId: Int, displayId: Int) {
        Instances.activityTaskManager.moveRootTaskToDisplay(taskId, displayId)
    }
}