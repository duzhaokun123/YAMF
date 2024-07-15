package com.mja.reyamf.xposed.utils

import com.mja.reyamf.common.runMain
import kotlinx.coroutines.CoroutineScope
import java.util.LinkedList

object RunMainThreadQueue {
    private val queue = LinkedList<suspend CoroutineScope.() -> Unit>()

    @Synchronized
    fun add(run: suspend CoroutineScope.() -> Unit) {
        queue.offer(run)
        if (queue.size == 1) {
            runMain {
                while (queue.isNotEmpty()) {
                    queue.poll()?.invoke(this)
                }
            }
        }
    }
}