package io.github.duzhaokun123.yamf.utils

import android.util.Log
import io.github.duzhaokun123.androidapptemplate.utils.runMain
import kotlinx.coroutines.CoroutineScope
import java.util.LinkedList

object RunMainThreadQueue {
    val queue = LinkedList<suspend CoroutineScope.() -> Unit>()

    @Synchronized
    fun add(run: suspend CoroutineScope.() -> Unit) {
        queue.offer(run)
        Log.d("YAMF", "add: ${queue.size}")
        if (queue.size == 1) {
            runMain {
                while (queue.isNotEmpty()) {
                    queue.poll()?.invoke(this)
                    Log.d("YAMF", "poll: ${queue.size}")
                }
            }
        }
    }
}