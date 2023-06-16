package io.github.duzhaokun123.yamf.model

import android.content.ComponentName
import io.github.duzhaokun123.yamf.utils.onException
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.Instances
import io.github.duzhaokun123.yamf.xposed.utils.TipUtil
import io.github.duzhaokun123.yamf.xposed.utils.moveTask
import io.github.duzhaokun123.yamf.xposed.utils.moveToDisplay

data class StartCmd(
    val componentName: ComponentName? = null,
    val userId: Int? = null,
    val taskId: Int? = null
) {
    val canStartActivity
        get() = componentName != null && userId != null

    val canMoveTask
        get() = taskId != null && taskId != 0

    fun startAuto(displayId: Int) {
        when {
            canStartActivity && canMoveTask ->
                moveToDisplay(Instances.systemContext, taskId!!, componentName!!, userId!!, displayId)
            canMoveTask -> {
                runCatching {
                    moveTask(taskId!!, displayId)
                }.onException {
                    TipUtil.showToast("can't move task $taskId")
                }
            }
            canStartActivity -> {
                runCatching {
                    startActivity(Instances.systemContext, componentName!!, userId!!, displayId)
                }.onException {
                    TipUtil.showToast("can't start activity $componentName")
                }
            }
        }
    }
}
