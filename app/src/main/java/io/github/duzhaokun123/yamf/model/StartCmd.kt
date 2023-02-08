package io.github.duzhaokun123.yamf.model

import android.content.ComponentName
import io.github.duzhaokun123.yamf.utils.startActivity
import io.github.duzhaokun123.yamf.xposed.YAMFManager
import io.github.duzhaokun123.yamf.xposed.utils.Instances
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
                moveToDisplay(YAMFManager.systemContext, taskId!!, componentName!!, userId!!, displayId)
            canMoveTask ->
                Instances.activityTaskManager.moveRootTaskToDisplay(taskId!!, displayId)
            canStartActivity ->
                startActivity(YAMFManager.systemContext, componentName!!, userId!!, displayId)
        }
    }
}
