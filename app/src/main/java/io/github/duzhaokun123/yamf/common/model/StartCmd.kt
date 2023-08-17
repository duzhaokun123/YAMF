package io.github.duzhaokun123.yamf.common.model

import android.content.ComponentName

data class StartCmd(
    val componentName: ComponentName? = null,
    val userId: Int? = null,
    val taskId: Int? = null
) {
    val canStartActivity
        get() = componentName != null && userId != null

    val canMoveTask
        get() = taskId != null && taskId != 0
}
