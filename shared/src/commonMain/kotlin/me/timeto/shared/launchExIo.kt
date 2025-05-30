package me.timeto.shared

import kotlinx.coroutines.CoroutineScope
import me.timeto.shared.misc.ioScope

fun launchExIo(block: suspend CoroutineScope.() -> Unit) =
    ioScope().launchEx(block)
