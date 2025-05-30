package me.timeto.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.launchEx(
    block: suspend CoroutineScope.() -> Unit,
) {
    launch {
        try {
            block()
        } catch (e: Throwable) {
            reportApi("launchEx $e\n${e.stackTraceToString()}")
        }
    }
}
